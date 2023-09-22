package dtkoceansurvival.dtkoceansurvival;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class FloodPopulator extends BlockPopulator {

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = localX + chunkX * 16 ;
                int z = localZ + chunkZ * 16 ;
                for (int y = limitedRegion.getHighestBlockYAt(x,z); y > worldInfo.getMinHeight(); y--) {
                    if (limitedRegion.isInRegion(x, y, z) && limitedRegion.isInRegion(x, y+1, z)) {
                        if (limitedRegion.getType(x, y, z).isAir()) {
                            if (limitedRegion.getType(x, y + 1, z).equals(Material.WATER)) {
                                limitedRegion.setType(x, y, z, Material.WATER);
                            } else if (limitedRegion.isInRegion(x + 1, y, z)) {
                                if (limitedRegion.getType(x + 1, y, z).equals(Material.WATER)) {
                                    limitedRegion.setType(x, y, z, Material.WATER);
                                }
                            } else if (limitedRegion.isInRegion(x - 1, y, z)) {
                                if (limitedRegion.getType(x - 1, y, z).equals(Material.WATER)) {
                                    limitedRegion.setType(x, y, z, Material.WATER);
                                }
                            } else if (limitedRegion.isInRegion(x, y, z + 1)) {
                                if (limitedRegion.getType(x, y, z + 1).equals(Material.WATER)) {
                                    limitedRegion.setType(x, y, z, Material.WATER);
                                }
                            } else if (limitedRegion.isInRegion(x, y, z - 1)) {
                                if (limitedRegion.getType(x, y, z - 1).equals(Material.WATER)) {
                                    limitedRegion.setType(x, y, z, Material.WATER);
                                }
                            }
                        }
                    }




                }
            }
        }
    }
}
