package dtkoceansurvival.dtkoceansurvival;

import dtkoceansurvival.dtkoceansurvival.gameplay.FloatyBlocks;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.packs.DataPack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import static dtkoceansurvival.dtkoceansurvival.utility.FileSystemLib.copyFilesFromStream;


public final class DTKOceanSurvival extends JavaPlugin implements Listener {

    private FloatyBlocks floatyBlocks;
    private OceanWorldHandler oceanWorldHandler ;
    public static JavaPlugin plugin;


    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info(this.getName() + " plugin starting");
        // Plugin startup logic


        //TODO: make this a common way to load stuff in, seems to only work in world atm
        //need gen dir if missing. then copy each file, but they need not exist(else need to remove)
        String worldID="world";
        copyFilesFromStream(this,
                "datapacks/dtkoceansurvival",
                getServer().getWorldContainer() + "/"+worldID+"/datapacks/dtkoceansurvival",
                Arrays.asList("data/dtkoceansurvival/loot_tables/chests/flotsam.json",
                        "pack.mcmeta",
                        "data/dtkoceansurvival/dimension_type/endless_ocean.json",
                        "data/dtkoceansurvival/worldgen/world_preset/endless_ocean.json",
                        "data/dtkoceansurvival/tags/worldgen/biome/has_structure/buried_treasure.json",
                        "data/dtkoceansurvival/tags/worldgen/biome/has_structure/ocean_ruin_cold.json",
                        "data/dtkoceansurvival/tags/worldgen/biome/has_structure/ocean_ruin_warm.json",
                        "data/dtkoceansurvival/tags/worldgen/biome/has_structure/shipwreck.json",
                        "data/dtkoceansurvival/tags/worldgen/biome/has_structure/shipwreck_beached.json",
                        "data/dtkoceansurvival/worldgen/structure_set/ruins.json",
                        "data/dtkoceansurvival/worldgen/noise_settings/endless_ocean.json")
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
    }

    //@EventHandler
    //public void onWorldInitEvent(WorldInitEvent event){
        //event.getWorld().getPopulators().add(new FloodPopulator());
    //    event.getWorld().getPopulators().add(new OceanFloorPopulator());
        //REENABLE TO TEST POPULators
        //lists are ref, so this just add a populator to the world and it just runs
        //but when the plugin loads is importaint. if ut dose not load at start, spawn gen may be skipped
        //if (event.getWorld().getName().equals("world")) {
            //getLogger().info("loading SeaFloorPopulator");
            //List<BlockPopulator> pops = event.getWorld().getPopulators();
            //pops.add(new SeaFloorPopulator());
        //}
        //below just need to be ran once but not at start up. since it a world, init at world int seem logical

    //}


    //@Override
    //public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
    //    return new FloodedOverworld();
    //}

    //TODO the next part require spawning of object
    //ideally using the reverse of the current direction
    //randomly in that region in vaild water sources exposed to air
    //1,0,1 vector would spawn in -1,0-1.  the min and max distance would be base
    //on simulated chuck distance as well as a min distance from player
    //spawns would be on a per player basis and spawn at sea level.
    //so really a 2d plane check vs 3d
    //and could randomly pick player if need to be slower (like a limit of player the logic will run per update)


}
