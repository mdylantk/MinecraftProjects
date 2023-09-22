package dtkoceansurvival.dtkoceansurvival;

import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Map;
import java.util.Random;

import static java.awt.SystemColor.info;

public class EndlessOcean extends ChunkGenerator {


    private SimplexOctaveGenerator noiseGenerator;
    private SimplexOctaveGenerator surfaceGenerator;
    private int maxGroundHeight = 0;
    private int bedrockLevel = -56;
    private int seaLevel = 64;

    EndlessOcean(){}
    EndlessOcean(int new_sealevel,int new_MaxGroundHeight){
        seaLevel = new_sealevel;
        maxGroundHeight = new_MaxGroundHeight;
    }

    private int getHeightFormGenerator(WorldInfo worldInfo, int x, int z) {
        if (noiseGenerator == null){
            noiseGenerator = new SimplexOctaveGenerator(new Random(worldInfo.getSeed()), 8);
            noiseGenerator.setScale(.0008D);
        }
        double noiseDepth = (x*0.35) + (z*0.2);
        return (int) (noiseGenerator.noise(x, z ,noiseDepth,.0001D,0.1D) * 64D + (double) maxGroundHeight);
    }

    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunkData){

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = getBaseHeight(worldInfo,random,chunkX*16 + x,chunkZ*16 + z,HeightMap.OCEAN_FLOOR_WG);
                for(int y = chunkData.getMinHeight(); y <= bedrockLevel && y < chunkData.getMaxHeight(); y++) {
                    if (y < chunkData.getMinHeight()) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                        //will fill it fully for now until a cavern world and logic is added
                    }
                    else if (y < height ){
                        int randomRoll = random.nextInt(32) + (bedrockLevel - y)*4;
                        if (randomRoll < 10) {
                            chunkData.setBlock(x, y, z, Material.DEEPSLATE);
                        } else if (randomRoll < 12) {
                            chunkData.setBlock(x, y, z, Material.LAVA);
                        } else if (randomRoll < 16) {
                            chunkData.setBlock(x, y, z, Material.MAGMA_BLOCK);
                        } else if (randomRoll <= 32) {
                            chunkData.setBlock(x, y, z, Material.BASALT);
                        }
                        else if (randomRoll == 48) {
                            chunkData.setBlock(x, y, z, Material.MAGMA_BLOCK);
                        }
                        else{
                            chunkData.setBlock(x, y, z, Material.BEDROCK);
                        }
                    }
                    else //if (chunkData.getBlockData(x,y,z).getMaterial().isAir())
                    {
                        chunkData.setBlock(x, y, z, Material.WATER);
                    }
                }
            }
        }
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        //super.generateNoise(worldInfo,random,chunkX,chunkZ,chunkData);


        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = getBaseHeight(worldInfo,random,chunkX*16 + x,chunkZ*16 + z,HeightMap.OCEAN_FLOOR);
                for(int y = bedrockLevel + 1; y < height && y < chunkData.getMaxHeight(); y++) {
                    if (chunkData.getBiome(chunkX * 16 + x, y, chunkZ * 16 + z).equals(Biome.DRIPSTONE_CAVES)) {
                        chunkData.setBlock(x, y, z, Material.DRIPSTONE_BLOCK);
                    } else {
                        if (y <= maxGroundHeight) {
                            chunkData.setBlock(x, y, z, Material.DEEPSLATE);
                        } else {
                            chunkData.setBlock(x, y, z, Material.STONE);
                        }

                    }
                }
                for(int y = height + 7; y < seaLevel && y < chunkData.getMaxHeight(); y++) {
                    chunkData.setBlock(x, y, z, Material.WATER);
                }
            }
        }
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunkData){
        Material baseMaterial = Material.WATER;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int height = getBaseHeight(worldInfo,random,chunkX*16 + x,chunkZ*16 + z,HeightMap.OCEAN_FLOOR_WG);
                int minHeight = getBaseHeight(worldInfo,random,chunkX*16 + x,chunkZ*16 + z,HeightMap.OCEAN_FLOOR);
                int LayerHeight = (height - minHeight)/2;
                for(int y = minHeight; y <= height && y < chunkData.getMaxHeight(); y++) {
                    Biome currentBiome = chunkData.getBiome(chunkX*16 + x, y,chunkZ*16 + z);
                    boolean isRedSand =
                            currentBiome.equals(Biome.BADLANDS) ||
                            currentBiome.equals(Biome.ERODED_BADLANDS) ||
                            currentBiome.equals(Biome.WOODED_BADLANDS);
                    boolean isClaySoil = currentBiome.equals(Biome.RIVER) || currentBiome.equals(Biome.LUSH_CAVES);
                    boolean isRocky =
                            currentBiome.equals(Biome.FROZEN_PEAKS) ||
                            currentBiome.equals(Biome.JAGGED_PEAKS) ||
                            currentBiome.equals(Biome.STONY_SHORE) ||
                            currentBiome.equals(Biome.STONY_PEAKS);
                    boolean isMuddy =
                            currentBiome.equals(Biome.SWAMP) ||
                            currentBiome.equals(Biome.MANGROVE_SWAMP) ||
                            currentBiome.equals(Biome.JUNGLE);
                    if(y <= minHeight + LayerHeight) {
                        if (isRedSand) {
                            baseMaterial = Material.RED_SANDSTONE;
                        }
                        else if (isRocky || isClaySoil) {
                            baseMaterial = Material.STONE;
                        }
                        else if (isMuddy){
                            baseMaterial = Material.DIRT;
                        }
                        else{
                            baseMaterial = Material.SANDSTONE;
                        }
                    }
                    else if (y <= minHeight +(LayerHeight*2)) {
                        if(y > seaLevel + 1){
                            if(y >= height -1){
                                baseMaterial = Material.GRASS_BLOCK;
                            }
                            else {
                                baseMaterial = Material.DIRT;
                            }
                        }
                        else if (isRedSand) {
                            baseMaterial = Material.RED_SAND;
                        }
                        else if (isRocky) {
                            baseMaterial = Material.GRAVEL;
                        }
                        else if (isClaySoil) {
                            if (y > seaLevel -2){
                                baseMaterial = Material.SAND;
                            }
                            else {
                                baseMaterial = Material.CLAY;
                            }
                        }
                        else if (isMuddy){
                            baseMaterial = Material.MUD;
                        }
                        else{
                            baseMaterial = Material.SAND;
                        }
                    }
                    chunkData.setBlock(x, y, z, baseMaterial);
                }
            }
        }
    }
    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        if (!shouldGenerateCaves()){
            return;
        }
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = getBaseHeight(worldInfo, random, chunkX * 16 + x, chunkZ * 16 + z, HeightMap.OCEAN_FLOOR);
                int maxBaseHeight = getBaseHeight(worldInfo, random, chunkX * 16 + x, chunkZ * 16 + z, HeightMap.OCEAN_FLOOR_WG);
                for (int y = seaLevel; y >= height - 6 && y > chunkData.getMinHeight(); y--) {
                    Material blockMaterial = chunkData.getBlockData(x,y,z).getMaterial();
                    if (blockMaterial.isAir() || blockMaterial.equals(Material.LAVA)){
                        if (y <= maxBaseHeight ){

                            if (y < height ) {
                                if(random.nextInt(8) >= 2){
                                    chunkData.setBlock(x, y, z, Material.COBBLESTONE);
                                }
                                else{
                                    chunkData.setBlock(x, y, z, Material.COBBLESTONE);
                                    break;
                                }
                            }
                            else if (y == height){
                                chunkData.setBlock(x, y, z, Material.COBBLESTONE);
                            }
                            else{
                                chunkData.setBlock(x, y, z, Material.GRAVEL);
                            }
                        }
                        else {
                            chunkData.setBlock(x, y, z, Material.WATER);
                        }
                    }
                    else if (blockMaterial.isOccluding() && y < maxBaseHeight){
                        //tries to break gen below a soild surface.
                        break;
                    }
                }
            }
        }
    }

    //OCEAN_FLOOR -16 height
    //OCEAN_FLOOR_WG -9 maxBaseHeight
    //low -22

    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap){
        //WG is max ground + height and non is +7 or 8 on top of that
        int minSurfaceHeight = 4;
        int base_height = getHeightFormGenerator(worldInfo, x, z);
        if (surfaceGenerator == null){
            surfaceGenerator = new SimplexOctaveGenerator(new Random(worldInfo.getSeed()), 6);
            surfaceGenerator.setScale(.016D);
        }

        int surfaceHeight = (int)(surfaceGenerator.noise(x, z, 0.0008D,0.1D) * 12D);
        if (surfaceHeight < 0) {
            surfaceHeight *= -1;
        }

        if (heightMap == HeightMap.WORLD_SURFACE_WG ){
            return base_height + surfaceHeight + minSurfaceHeight;
        }
        if (heightMap == HeightMap.WORLD_SURFACE){
            return base_height ;
        }
        if (heightMap == HeightMap.OCEAN_FLOOR){
            return base_height;
        }
        if (heightMap == HeightMap.OCEAN_FLOOR_WG) {
            return base_height + surfaceHeight + minSurfaceHeight;
        }
        return base_height + 124;
    }

    public boolean shouldGenerateNoise() {return false;}
    public boolean shouldGenerateDecorations() { return true; }
    public boolean shouldGenerateMobs() { return true; }
    public boolean shouldGenerateStructures() { return true; }
    //lot of air tubes if true
    public boolean shouldGenerateCaves() { return true; }

}
