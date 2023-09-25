package dtkoceansurvival.dtkoceansurvival.gameplay;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class FloatyBlocks implements Listener {
    //NOTE: the goal of this class is to allow other blocks to be place in water like lilypads
    //but it low on the to-do list

    private JavaPlugin owner;

    public FloatyBlocks(JavaPlugin new_owner){
        owner = new_owner;
        owner.getServer().getPluginManager().registerEvents(this,owner);
        owner.getLogger().info("FloatyBlocks loaded");
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getItem() != null) {
            if (Tag.LOGS.isTagged(event.getItem().getType())){
                if(event.getAction().equals(Action.RIGHT_CLICK_AIR)){
                    Block targetBlock = null;
                    for(int i=1; i < 6;i++) {
                        //Location blockLoc = event.getPlayer().getEyeLocation().add(event.getPlayer().getFacing().getDirection().multiply(i));
                        Location blockLoc = event.getPlayer().getEyeLocation().add(event.getPlayer().getEyeLocation().getDirection().multiply(i));
                        Block checkBlock = event.getPlayer().getWorld().getBlockAt(blockLoc);
                        if (checkBlock.isLiquid()){
                            //owner.getLogger().info("found vaild liquid block ");
                            boolean floatUp = true;
                            Block topBlock = event.getPlayer().getWorld().getBlockAt(blockLoc.clone().add(0,1,0));
                            if (floatUp && topBlock.isLiquid()){
                                //owner.getLogger().info("float up ");
                                for(int y=1; y < 5;y++){
                                    topBlock = event.getPlayer().getWorld().getBlockAt(blockLoc.clone().add(0,y+1,0));
                                    checkBlock = event.getPlayer().getWorld().getBlockAt(blockLoc.clone().add(0,y,0));
                                    if (topBlock.getType().isSolid() || topBlock.getType().isAir()){
                                        targetBlock = checkBlock;
                                        //owner.getLogger().info("found vaild surface block ");
                                        break;
                                    }

                                }
                            }else {
                                targetBlock = checkBlock;
                            }
                            break;
                        }
                        if(checkBlock.getType().isSolid()){
                            //owner.getLogger().info("Soild block, breaking check");
                            break;
                        }
                    }


                    if (targetBlock != null) {

                        BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(null, targetBlock.getState(), null, event.getItem(), event.getPlayer(), true, event.getHand());
                        //TODO figure out how to properly use this event. current using item for mat which can be correct if checks are made.
                        Bukkit.getPluginManager().callEvent(blockPlaceEvent);
                        //TODO: may need to delay the logic? may need time for other things to modify it
                        if (!blockPlaceEvent.isCancelled()) {

                            String blockDataAppend = "[axis=z]";
                            if (blockPlaceEvent.getPlayer() != null){
                                if (blockPlaceEvent.getPlayer().getFacing().equals(BlockFace.EAST)){
                                    blockDataAppend = "[axis=x]";
                                }
                                else if (blockPlaceEvent.getPlayer().getFacing().equals(BlockFace.WEST)){
                                    blockDataAppend = "[axis=x]";
                                }
                            }

                            blockPlaceEvent.getBlockReplacedState().getBlock().setBlockData(Bukkit.getServer().createBlockData(blockPlaceEvent.getItemInHand().getType().getKey().toString()+blockDataAppend));
                            if (blockPlaceEvent.getPlayer().getGameMode() != GameMode.CREATIVE) {
                                //"block minecraft:birch_log[axis=y]"
                                blockPlaceEvent.getItemInHand().setAmount(blockPlaceEvent.getItemInHand().getAmount() - 1);
                            }
                        }

                    }
                }
            }
        }
    }
}
