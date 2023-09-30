package dtk.oceansurvival.items;

import dtk.oceansurvival.DTKOceanSurvival;
import dtk.oceansurvival.OceanWorldHandler;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class ScrollOfRecall implements Listener {
    //this item currently a debug or failsafe way to get back to the endless_ocean world
    static NamespacedKey recallKey;

    public ScrollOfRecall(){
        recallKey = new NamespacedKey(DTKOceanSurvival.plugin,"recall");

        Bukkit.addRecipe(oceanScrollOfRecallRecipe(DTKOceanSurvival.plugin));
        Bukkit.getServer().getPluginManager().registerEvents(this, DTKOceanSurvival.plugin);

    }
    public void remove(){
        Bukkit.removeRecipe(recallKey);
    }

    public static ItemStack getScrollOfRecall(int amount,String worldName){
        ItemStack item = new ItemStack(Material.PAPER,amount);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Scroll of recall");
        meta.setLore(Arrays.asList("A magical scroll.","It is signed with","the word:", worldName));
        meta.getPersistentDataContainer().set(recallKey, PersistentDataType.STRING,worldName);
        item.setItemMeta(meta);

        return item;
    }

    ShapedRecipe oceanScrollOfRecallRecipe(Plugin plugin){
        ShapedRecipe recipe = new ShapedRecipe(recallKey,getScrollOfRecall(4,OceanWorldHandler.oceanWorld.getName()));
        recipe.shape(" G ","BCB"," L ");
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(BottleOfAir.getBottleOfAir(1)));
        recipe.setIngredient('G', new RecipeChoice.ExactChoice(new ItemStack(Material.GOLD_INGOT)));
        recipe.setIngredient('L',new RecipeChoice.ExactChoice(new ItemStack(Material.LAPIS_LAZULI)));
        recipe.setIngredient('C', new RecipeChoice.ExactChoice(new ItemStack(Material.PAPER)));
        return recipe;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        //offhand action or well any other action will still happen so may need a way to ignore it
        //TODO: set the meta so it can be updatesd
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(event.getItem() != null) {
                ItemMeta meta = event.getItem().getItemMeta();
                if(meta.getPersistentDataContainer().has(recallKey, PersistentDataType.STRING)) {
                    String worldName = meta.getPersistentDataContainer().get(recallKey, PersistentDataType.STRING);
                    World world = event.getPlayer().getServer().getWorld(worldName);
                    if(world != null) {
                        event.getItem().setAmount(event.getItem().getAmount() - 1);

                        event.getPlayer().teleport(world.getSpawnLocation());
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

}
