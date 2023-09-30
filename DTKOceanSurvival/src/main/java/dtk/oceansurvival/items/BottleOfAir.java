package dtk.oceansurvival.items;

import dtk.oceansurvival.DTKOceanSurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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



public class BottleOfAir implements Listener {
    static NamespacedKey giveAirKey;

    public BottleOfAir(){
        giveAirKey = new NamespacedKey(DTKOceanSurvival.plugin,"giveAir");

        Bukkit.addRecipe(bottleOfAirRecipe(DTKOceanSurvival.plugin));
        Bukkit.getServer().getPluginManager().registerEvents(this, DTKOceanSurvival.plugin);

    }
    public void remove(){
        Bukkit.removeRecipe(giveAirKey);
    }

    ShapedRecipe bottleOfAirRecipe(Plugin plugin){
        ShapedRecipe recipe = new ShapedRecipe(giveAirKey,getBottleOfAir(4));
        recipe.shape(" B ","BCB"," B ");
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(new ItemStack(Material.GLASS_BOTTLE)));
        recipe.setIngredient('C', Material.HONEYCOMB);
        return recipe;
    }

    public static ItemStack getBottleOfAir(int amount){
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE,amount);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Bottle of Air");
        meta.setLore(Arrays.asList("An air tight bottle.","Who knows when spare","air will come in use."));
        meta.getPersistentDataContainer().set(giveAirKey, PersistentDataType.INTEGER,100);
        item.setItemMeta(meta);

        //Bukkit.getLogger().info("TEST:" + item.getItemMeta());

        return item;
    }


    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        //offhand action or well any other action will still happen so may need a way to ignore it
        //TODO: set the meta so it can be updatesd
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(event.getItem() != null) {
                ItemMeta meta = event.getItem().getItemMeta();
                if(meta.getPersistentDataContainer().has(giveAirKey, PersistentDataType.INTEGER)) {
                    int amount = event.getItem().getAmount();
                    int airAmount = meta.getPersistentDataContainer().get(giveAirKey, PersistentDataType.INTEGER);
                    int air = event.getPlayer().getRemainingAir();
                    int maxAir = event.getPlayer().getMaximumAir();
                    if (air < maxAir - airAmount) {
                        event.getItem().setAmount(amount - 1);
                        event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), new ItemStack(Material.GLASS_BOTTLE, 1));
                        event.getPlayer().setRemainingAir(air + airAmount);
                    }
                    event.setCancelled(true);
                }
            }
        }
    }


}
