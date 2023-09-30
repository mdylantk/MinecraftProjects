package dtk.oceansurvival;

import dtk.oceansurvival.items.BottleOfAir;
import dtk.oceansurvival.items.ScrollOfRecall;
import dtk.oceansurvival.utility.FileSystemLib;
import dtk.oceansurvival.gameplay.FloatyBlocks;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.packs.DataPack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;


public final class DTKOceanSurvival extends JavaPlugin implements Listener {

    private FloatyBlocks floatyBlocks;
    private OceanWorldHandler oceanWorldHandler;
    private BottleOfAir bottleOfAir;
    private ScrollOfRecall scrollOfRecall;
    public static JavaPlugin plugin;

    public static void log(String message){
        //a short cut to the logger as long as this plugin been enabled
        DTKOceanSurvival.plugin.getLogger().info(message);
    }

    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info(this.getName() + " plugin starting");
        // Plugin startup logic


        //TODO: make this a common way to load stuff in, seems to only work in world atm
        //need gen dir if missing. then copy each file, but they need not exist(else need to remove)
        String worldID="world";
        FileSystemLib.copyFilesFromStream(this,
                "datapacks/dtkoceansurvival",
                getServer().getWorldContainer() + "/"+worldID+"/datapacks/dtkoceansurvival",
                Arrays.asList("data/dtkoceansurvival/loot_tables/chests/flotsam.json",
                        "pack.mcmeta")
        );
        //copyFilesFromStream(this,
        //        "DIM/endless_ocean",
        //        getServer().getWorldContainer() + "/world_endless_ocean",
        //        Arrays.asList("level.dat"));
        //NOTE: this breaks the dat file

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, this::onReady);

        //dtkoceansurvival:endless_ocean
    }

    @Override
    public void onDisable() {
        getLogger().info(this.getName() + " plugin ending");
        // Plugin shutdown logic
        floatyBlocks = null;
        oceanWorldHandler = null;

        bottleOfAir.remove();
        bottleOfAir = null;
        scrollOfRecall.remove();
        scrollOfRecall = null;
    }

    private void onReady(){
        //TODO:figure out the proper way to reload or restart the server when a new plugin is added during start
        if(!getServer().getDataPackManager().getDataPacks().isEmpty()) {
            boolean needRestart = false;
            for (DataPack datapack : getServer().getDataPackManager().getDataPacks()) {
                //getLogger().info("checking: " + datapack.toString() + "as:" + datapack.getKey());
                if(datapack.getTitle().equals("dtkoceansurvival")){
                    if(!datapack.isEnabled()) {
                        getLogger().info("is disable, will try to enable: " + datapack.getTitle() + "as:" + datapack.getKey());
                        getServer().dispatchCommand(Bukkit.getConsoleSender(), "datapack enable " + datapack.getKey());
                        needRestart = true;
                    }
                }
            }
            if (needRestart) {
                getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");
            }
        }
        //note: need to load datapack to world before gen if structure are changed/added


        //this is delayed to run after. most init and checks should happen here
        if (oceanWorldHandler == null){
            oceanWorldHandler = new OceanWorldHandler(this);
        }
        if (floatyBlocks == null){
            floatyBlocks = new FloatyBlocks(this);
        }
        if (bottleOfAir == null){
            bottleOfAir = new BottleOfAir();
        }
        if (scrollOfRecall == null){
            //need to run after any custom items in its recipe
            scrollOfRecall = new ScrollOfRecall();
        }
    }

}
