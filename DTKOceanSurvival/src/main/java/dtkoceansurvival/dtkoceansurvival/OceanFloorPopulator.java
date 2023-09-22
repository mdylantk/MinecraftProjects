package dtkoceansurvival.dtkoceansurvival;

import org.bukkit.Material;

import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OceanFloorPopulator extends BlockPopulator {

    private int depth = 0;

    private static List<Material> coralBlockMaterials = Arrays.asList(Material.TUBE_CORAL_BLOCK,Material.BRAIN_CORAL_BLOCK,
            Material.FIRE_CORAL_BLOCK,Material.HORN_CORAL_BLOCK,Material.BUBBLE_CORAL_BLOCK);

    private static List<Material> coralMaterials = Arrays.asList(Material.TUBE_CORAL,Material.BRAIN_CORAL,
            Material.FIRE_CORAL,Material.HORN_CORAL,Material.BUBBLE_CORAL, Material.TUBE_CORAL_FAN,
            Material.BRAIN_CORAL_FAN, Material.FIRE_CORAL_FAN,Material.HORN_CORAL_FAN,Material.BUBBLE_CORAL_FAN);

    private static List<Material> gravelReplacerMaterials = Arrays.asList(Material.SAND,Material.MUD,
            Material.DIRT,Material.COBBLESTONE,Material.MOSSY_COBBLESTONE,Material.CLAY,Material.SANDSTONE,
            Material.COARSE_DIRT,Material.ROOTED_DIRT);

    private static List<Material> coldMaterials = Arrays.asList(Material.ICE,Material.ICE,Material.ICE,
            Material.ICE,Material.BLUE_ICE,Material.ICE,Material.ICE,Material.ICE,Material.ICE,Material.ICE,
            Material.SNOW_BLOCK, Material.POWDER_SNOW,Material.FROSTED_ICE,Material.PACKED_ICE,Material.PACKED_ICE);

    private static List<Material> commonFoliageMaterials = Arrays.asList(Material.SEAGRASS,Material.SEAGRASS,
            Material.SEAGRASS,Material.SEAGRASS,Material.KELP_PLANT,Material.KELP_PLANT,Material.KELP_PLANT,
            Material.SEA_PICKLE,Material.KELP_PLANT,Material.KELP_PLANT,Material.SEAGRASS,Material.SEAGRASS);


    OceanFloorPopulator(){}
    OceanFloorPopulator(int new_depth){
    }

    void spawnBlocks(Random random, int x, int y, int z, LimitedRegion limitedRegion,
                     List<Material> materials, List<Material> foliageMaterials){
        for(int lx = -4; lx <= 4;lx++ ){
            for(int lz = -4; lz <= 4;lz++ ){
                for(int ly = 1; ly >= -6;ly-- ) {
                    if ((random.nextInt(20) > 5)) {
                        if (limitedRegion.isInRegion(x + lx, y+ly, z + lz)) {
                            Material soil = limitedRegion.getType(x + lx, y+ly, z + lz);
                            Material newMaterial = Material.SAND;
                            if (soil.equals(Material.GRAVEL) || soil.equals(Material.GRASS_BLOCK) || soil.equals(Material.STONE)) {
                                if(materials != null){
                                    newMaterial = materials.get(random.nextInt(materials.size()));
                                }
                                limitedRegion.setType(x + lx, y + ly, z + lz, newMaterial);
                                if(ly >= 0){
                                    if(random.nextInt(20)>15) {
                                        if (limitedRegion.getType(x + lx, y + ly + 1, z + lz).equals(Material.WATER)) {
                                            if (foliageMaterials != null) {
                                                newMaterial = foliageMaterials.get(random.nextInt(foliageMaterials.size()));
                                            }
                                            else{
                                                newMaterial = Material.SEAGRASS;
                                            }
                                        }
                                        limitedRegion.setType(x + lx, y + ly + 1, z + lz, newMaterial);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {

        for(int iteration = 0; iteration < random.nextInt(48) + 16; iteration++) {
            int x = random.nextInt(16) + chunkX * 16;
            int z = random.nextInt(16) + chunkZ * 16;
            Material surfaceMaterial = Material.AIR;
            for (int y = limitedRegion.getHighestBlockYAt(x,z)-1; y > depth; y--) {
                if (limitedRegion.isInRegion(x, y, z)) {
                    Material block = limitedRegion.getType(x, y, z);
                    if(surfaceMaterial.isAir()){
                        if(block.equals(Material.WATER) ||block.equals(Material.ICE)){
                            surfaceMaterial= block;
                        }
                    }
                    if (block.isOccluding()) {
                        if (limitedRegion.isInRegion(x, y + 1, z)) {
                            if (limitedRegion.getType(x, y + 1, z).equals(Material.WATER)) {
                                //we know plant can be placed, just need to know which
                                if (block.equals(Material.GRAVEL) || block.equals(Material.GRASS_BLOCK) || block.equals(Material.STONE)){
                                    List<Material> materialList = null;
                                    if(surfaceMaterial.equals(Material.ICE)){
                                        spawnBlocks(random, x, y, z, limitedRegion, coldMaterials,coldMaterials);
                                    }
                                    else if(random.nextInt(20) > 8) {
                                        //materialList = gravelReplacerMaterial;
                                        spawnBlocks(random, x, y, z, limitedRegion, gravelReplacerMaterials,null);
                                    }
                                    else if(random.nextInt(100)> 90) {
                                        //materialList = coralBlockMaterial;
                                        spawnBlocks(random, x, y, z, limitedRegion, coralBlockMaterials, coralMaterials);
                                    }
                                    else {
                                        spawnBlocks(random, x, y, z, limitedRegion, null, commonFoliageMaterials);
                                    }
                                }
                                //spawnPlant(random, x, y, z, limitedRegion);
                            }
                        }
                    }
                }
            }
        }
    }
}
