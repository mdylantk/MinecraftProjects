package dtk.oceansurvival.populators;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.Barrel;
import org.bukkit.block.Biome;

import org.bukkit.block.BlockState;

import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import org.bukkit.loot.LootTables;
import org.bukkit.util.Vector;


import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.Integer.max;

public class RuinPopulator extends BlockPopulator {

    private List<Material> wallMaterials =  Arrays.asList(Material.STONE_BRICKS,Material.BRICKS,
            Material.DEEPSLATE_BRICKS,Material.CRACKED_STONE_BRICKS, Material.MOSSY_STONE_BRICKS,
            Material.MUD_BRICKS, Material.POLISHED_BLACKSTONE_BRICKS, Material.NETHER_BRICKS,
            Material.CRACKED_NETHER_BRICKS, Material.QUARTZ_BRICKS, Material.RED_NETHER_BRICKS,
            Material.BLACK_CONCRETE,Material.BLUE_CONCRETE,Material.BROWN_CONCRETE,Material.CYAN_CONCRETE,
            Material.GRAY_CONCRETE,Material.GREEN_CONCRETE,Material.LIGHT_BLUE_CONCRETE, Material.LIGHT_GRAY_CONCRETE,
            Material.LIME_CONCRETE,Material.MAGENTA_CONCRETE,Material.ORANGE_CONCRETE,Material.PURPLE_CONCRETE,
            Material.PINK_CONCRETE,Material.WHITE_CONCRETE,Material.YELLOW_CONCRETE);
    private List<Material> floorMaterials = Arrays.asList(Material.OAK_PLANKS,Material.BIRCH_PLANKS,
            Material.DARK_OAK_PLANKS, Material.SPRUCE_PLANKS, Material.ACACIA_PLANKS, Material.BAMBOO_PLANKS,
            Material.CHERRY_PLANKS, Material.CRIMSON_PLANKS, Material.JUNGLE_PLANKS, Material.MANGROVE_PLANKS,
            Material.WARPED_PLANKS,Material.TERRACOTTA,Material.BLACK_TERRACOTTA,
            Material.WHITE_TERRACOTTA, Material.GRAY_TERRACOTTA, Material.LIGHT_GRAY_TERRACOTTA,
            Material.BROWN_TERRACOTTA, Material.ORANGE_TERRACOTTA,Material.BLUE_TERRACOTTA,
            Material.CYAN_TERRACOTTA,Material.GREEN_TERRACOTTA,Material.LIME_TERRACOTTA,Material.MAGENTA_TERRACOTTA,
            Material.PINK_TERRACOTTA,Material.PURPLE_TERRACOTTA,Material.RED_TERRACOTTA,Material.YELLOW_TERRACOTTA);
    private List<Material> foundationMaterials = Arrays.asList(Material.COBBLESTONE,
            Material.STONE,Material.MOSSY_COBBLESTONE,Material.INFESTED_COBBLESTONE,Material.COBBLED_DEEPSLATE,
            Material.INFESTED_STONE,Material.ANDESITE,Material.DIORITE,Material.GRANITE,Material.BLACKSTONE,
            Material.CALCITE);
    private List<Material> roofMaterials = Arrays.asList(Material.TERRACOTTA,Material.BLACK_TERRACOTTA,
            Material.WHITE_TERRACOTTA, Material.GRAY_TERRACOTTA, Material.LIGHT_GRAY_TERRACOTTA,
            Material.BROWN_TERRACOTTA, Material.ORANGE_TERRACOTTA);
    private List<Material> damagedMaterials = Arrays.asList(Material.DIRT,Material.GRAVEL, Material.SAND,
            Material.COBBLESTONE,Material.INFESTED_COBBLESTONE,Material.COARSE_DIRT,Material.DIRT,
            Material.GRAVEL,Material.GRAVEL,Material.GRAVEL,Material.GRAVEL,Material.SAND,Material.SAND);
    private List<Material> glassMaterials = Arrays.asList(Material.BLACK_STAINED_GLASS,Material.BLUE_STAINED_GLASS,
            Material.BROWN_STAINED_GLASS, Material.GRAY_STAINED_GLASS,Material.GREEN_STAINED_GLASS,
            Material.CYAN_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS,
            Material.LIME_STAINED_GLASS,Material.LIME_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS,
            Material.ORANGE_STAINED_GLASS,Material.PINK_STAINED_GLASS,Material.PURPLE_STAINED_GLASS,
            Material.RED_STAINED_GLASS,Material.YELLOW_STAINED_GLASS,Material.WHITE_STAINED_GLASS);
    private List<Material> naturalMaterials = Arrays.asList(Material.DIRT,Material.GRAVEL, Material.SAND,
            Material.COBBLESTONE,Material.STONE,Material.COARSE_DIRT,Material.ICE,
            Material.PACKED_ICE,Material.MUD,Material.BLUE_ICE,Material.SNOW,Material.TERRACOTTA,
            Material.ORANGE_TERRACOTTA, Material.WHITE_TERRACOTTA,Material.RED_TERRACOTTA,Material.BROWN_TERRACOTTA,
            Material.YELLOW_TERRACOTTA,Material.LIGHT_GRAY_TERRACOTTA,Material.RED_SAND,Material.RED_SANDSTONE,
            Material.SANDSTONE,Material.GRASS_BLOCK, Material.DIORITE, Material.GRANITE, Material.ANDESITE,
            Material.DEEPSLATE,Material.WATER,Material.AIR, Material.CAVE_AIR, Material.LAVA, Material.CLAY,
            Material.MOSSY_COBBLESTONE,Material.SEAGRASS,Material.SEA_PICKLE,Material.KELP_PLANT);

    private List<Biome> highPopulationBiomes = Arrays.asList(Biome.SAVANNA,Biome.BADLANDS,Biome.DESERT,
            Biome.MEADOW,Biome.PLAINS,Biome.TAIGA,Biome.SNOWY_TAIGA,Biome.SNOWY_PLAINS);


    public int minHeight = 58;
    public int maxHeight = 96;
    public double decayModifier = 2;
    public double decayOffest = 0.5;

    //the idea is to fine a spot at seafloor or surface, offset a few blocks, then generate up(or even down)
    //mostly for simple shapes. foundation, a few floors, and maybe roof
    //these structure are going to be buried. may need to use barrels to bypass waterlogging issues

    //need 4 points to check if in vaild region else fails. could run a few checks to see how big it can be to reduce failure
    //then pick materials use to build, randomy spawp out some with damaged blocks. as it grows, more fails(no blocks changes)
    //will happen. things like windows and such may need to be plan, but that for later

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        //random chance to spawn.
        int x = random.nextInt(6)+5 + chunkX * 16;
        int z = random.nextInt(6)+5 + chunkZ * 16;
        int startingY = 0;
        int sizeX = random.nextInt(4) + 3;
        int sizeZ = random.nextInt(4) + 3;
        int maxFloorRange = 9;
        Biome biome;


        for (int y = maxHeight; y > minHeight; y--) {
            if (limitedRegion.getType(x, y, z).isOccluding()) {
                startingY = y - (random.nextInt(14)+2);
                break;
            }
            else if(y <= minHeight ){
                return;
            }
        }
        biome = limitedRegion.getBiome(x,startingY,z);
        if(highPopulationBiomes.contains(biome)){
            if(random.nextInt(100)<66){return;}
        }
        else if (biome.getKey().toString().contains("ocean")){
            return;
        }
        else{
            if(random.nextInt(100)<98){ return;}
            else{maxFloorRange = 5;}
        }

        if(limitedRegion.isInRegion(x+sizeX, startingY,z+sizeZ)&&
                limitedRegion.isInRegion(x-sizeX, startingY,z-sizeZ) &&
                limitedRegion.isInRegion(x+sizeX, startingY,z-sizeZ) &&
                limitedRegion.isInRegion(x-sizeX, startingY,z+sizeZ)
        ) {
            int floors = random.nextInt(maxFloorRange) + 1;
            int roomHeight = random.nextInt(3)+4;
            int windowHeight = random.nextInt(max(roomHeight/2,1))+1;
            int foundationDepth = random.nextInt(3)+7;
            double damageFactor = random.nextDouble()*decayModifier+decayOffest;   //*0.19 + 0.01;
            Material foundation = foundationMaterials.get(random.nextInt(foundationMaterials.size()));
            Material wall = wallMaterials.get(random.nextInt(wallMaterials.size()));
            Material floor = floorMaterials.get(random.nextInt(floorMaterials.size()));
            Material roof = roofMaterials.get(random.nextInt(roofMaterials.size()));
            Material glass = glassMaterials.get(random.nextInt(glassMaterials.size()));

            Vector door;
            if (random.nextInt(1)==1) {
                int randomX = random.nextInt(1)/2-1;
                door = new Vector(x+(randomX*sizeX), startingY + 1, z);
            }
            else{
                int randomZ = random.nextInt(1)/2-1;
                door = new Vector(x, startingY + 1, z+(randomZ*sizeZ));
            }
            boolean isFloor;
            int maxHeight = roomHeight*floors+startingY+2;

            for(int roomX = x-sizeX ; roomX <= x+sizeX; roomX++ ) {
                for (int roomZ = z-sizeZ; roomZ <= z+sizeZ; roomZ++) {
                    for (int roomY = (startingY - foundationDepth); roomY <= maxHeight; roomY++) {

                        int currentHeight = roomY - startingY;
                        double heightDecayRate = ((double)currentHeight)/(maxHeight+roomHeight);

                        if (limitedRegion.isInRegion(roomX, roomY, roomZ)) {
                            int blockStability = hasSupport(roomX, roomY, roomZ, limitedRegion, true);
                            int roomHeightID = currentHeight % (roomHeight + 1);
                            if (blockStability > 0) {
                                if (currentHeight > 0) {
                                    isFloor = roomHeightID == 0;
                                } else {
                                    isFloor = false;
                                }
                                if (currentHeight <= 0) {
                                    spawnBlock(random, roomX, roomY, roomZ, limitedRegion, foundation, damageFactor * 0.01);
                                } else if (roomX == x + sizeX || roomZ == z + sizeZ ||
                                        roomX == x - sizeX || roomZ == z - sizeZ) {

                                    if (!(roomX == door.getBlockX() && roomZ == door.getBlockZ() && currentHeight <= 2)) {

                                        if (((roomX > (x - sizeX) + 2 && roomX < (x + sizeX) - 2) || (roomZ > (z - sizeZ) + 2 && roomZ < (z + sizeZ) - 2)) &&
                                                (roomHeightID >= 2 && roomHeightID <= windowHeight + 2) && roomY < maxHeight - 2
                                        ) {
                                            spawnBlock(random, roomX, roomY, roomZ, limitedRegion, glass, damageFactor * heightDecayRate);
                                        } else {
                                            spawnBlock(random, roomX, roomY, roomZ, limitedRegion, wall, damageFactor * heightDecayRate);
                                        }
                                    }
                                } else if (isFloor && roomY < maxHeight - 2) {
                                    spawnBlock(random, roomX, roomY, roomZ, limitedRegion, floor, damageFactor * heightDecayRate);
                                } else if (roomY == maxHeight - 1) {
                                    spawnBlock(random, roomX, roomY, roomZ, limitedRegion, roof, damageFactor * heightDecayRate);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    int hasSupport(int x, int y, int z, LimitedRegion limitedRegion,Boolean returnAtFirst){
        int stability = 0;
        List<Vector> points = Arrays.asList(
                new Vector(0,-1,0), new Vector(-1,0,0), new Vector(1,0,0),
                new Vector(0,0,-1), new Vector(0,0,1), new Vector(0,1,0)

        );
        for (Vector blockPoint : points) {
            if (limitedRegion.isInRegion(x + blockPoint.getBlockX(), y + blockPoint.getBlockY(), z + blockPoint.getBlockZ())) {
                if (limitedRegion.getType(x + blockPoint.getBlockX(), y + blockPoint.getBlockY(), z + blockPoint.getBlockZ()).isSolid()) {
                    stability += 1;
                    if (returnAtFirst) {
                        return stability;
                    }
                }
            }
        }
        return stability;
    }
    void spawnBlock(Random random, int x, int y, int z, LimitedRegion limitedRegion, Material material, double decayWeight){
        if (naturalMaterials.contains(limitedRegion.getType(x,y,z))) {
            if (random.nextDouble() <= decayWeight) {
                if (random.nextDouble() <= 0.33) {
                    limitedRegion.setType(x, y, z, damagedMaterials.get(random.nextInt(damagedMaterials.size())));
                }
            } else {
                limitedRegion.setType(x, y, z, material);
            }
        }
    }
}
