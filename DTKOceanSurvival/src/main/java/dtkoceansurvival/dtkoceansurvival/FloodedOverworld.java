package dtkoceansurvival.dtkoceansurvival;

import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.lang.invoke.SwitchPoint;
import java.util.Random;

public class FloodedOverworld extends ChunkGenerator {

    public boolean shouldGenerateNoise() {return true;}
    public boolean shouldGenerateSurface() {return true;}//or shouldGenerateSurface(WorldInfo, Random, int, int)
    public boolean shouldGenerateDecorations() { return true; }
    public boolean shouldGenerateMobs() { return true; }
    public boolean shouldGenerateStructures() { return true; }
    public boolean shouldGenerateCaves() { return true; }
    int seaLevel = 88;
    boolean floodAll = true; //this overrides the flooding incase world is using sealevel

    FloodedOverworld(){}
    FloodedOverworld(int new_seaLevel){
        seaLevel = new_seaLevel;
    }


    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunkData.getMaxHeight(); y > chunkData.getMinHeight(); y--) {
                    if(y < seaLevel && y >= 62 ){
                        if(chunkData.getType(x, y, z).isAir()){
                            chunkData.setBlock(x, y, z, Material.WATER);
                        }
                    }
                }
            }
        }
    }
    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        if (floodAll) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = seaLevel; y >= -3; y--) {
                        if (y > 3) {
                            if (chunkData.getType(x, y, z).isAir()) {
                                chunkData.setBlock(x, y, z, Material.WATER);
                            } else if (chunkData.getType(x, y, z).equals(Material.LAVA)) {
                                chunkData.setBlock(x, y, z, Material.OBSIDIAN);
                            }
                        } else {
                            if (chunkData.getType(x, y, z).isAir()) {
                                chunkData.setBlock(x, y, z, Material.DEEPSLATE);
                            }
                        }

                    }
                }
            }
        }
    }

    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap){
        //WG is max ground + height and non is +7 or 8 on top of that

        if (heightMap == HeightMap.WORLD_SURFACE_WG ){
            return seaLevel + 1;
        }
        if (heightMap == HeightMap.WORLD_SURFACE){
            return seaLevel;
        }
        if (heightMap == HeightMap.OCEAN_FLOOR){
            return seaLevel;
        }
        if (heightMap == HeightMap.OCEAN_FLOOR_WG) {
            return seaLevel + 1;
        }
        return worldInfo.getMaxHeight();
    }
}


