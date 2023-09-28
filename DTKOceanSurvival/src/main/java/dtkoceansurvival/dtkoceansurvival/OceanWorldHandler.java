package dtkoceansurvival.dtkoceansurvival;


import dtkoceansurvival.dtkoceansurvival.gameplay.Currents;
import dtkoceansurvival.dtkoceansurvival.gameplay.FlotsamSpawner;
import dtkoceansurvival.dtkoceansurvival.generators.FloodedOverworld;
import dtkoceansurvival.dtkoceansurvival.populators.OceanFloorPopulator;
import dtkoceansurvival.dtkoceansurvival.populators.RuinPopulator;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import java.util.*;

public class OceanWorldHandler implements Listener {
    //This handles and control logic that happens in ocean world.
    //private JavaPlugin owner;
    private World oceanWorld;
    private int seaLevel = 128;
    private BukkitTask flotsamTimer;
    private Random oceanRandom = new Random();


    //TODO:: move current to a diffrent class and make vectors a static ref to the object so flotsam spawner can acess it

    private FlotsamSpawner flotsamSpawner;
    private Currents currents;
    OceanWorldHandler(JavaPlugin new_owner){
        if (new_owner == null){return;}
        Bukkit.getServer().getPluginManager().registerEvents(this,DTKOceanSurvival.plugin);
        if (Bukkit.getServer().getWorld("world_endless_ocean") == null) {
            WorldCreator wc = new WorldCreator("world_endless_ocean");
            wc.generator(new FloodedOverworld(seaLevel));

            Bukkit.getLogger().info("GEN SET!: " +wc.generatorSettings());
            wc.createWorld();

            Bukkit.getLogger().info("creating new WEO");
            oceanWorld = Bukkit.getServer().getWorld("world_endless_ocean");
            oceanWorld.getPopulators().add(new OceanFloorPopulator());
            oceanWorld.getPopulators().add(new RuinPopulator());

        }
        else{
            oceanWorld = Bukkit.getServer().getWorld("world_endless_ocean");
        }

        currents = new Currents(oceanWorld);
        if (flotsamTimer == null) {
            Bukkit.getLogger().info("creating timer");
            flotsamTimer = Bukkit.getServer().getScheduler().runTaskTimer(DTKOceanSurvival.plugin, this::spawnFlotsam, 600, 900);
        }

        flotsamSpawner = new FlotsamSpawner(DTKOceanSurvival.plugin, oceanWorld,oceanRandom);
        flotsamSpawner.seaLevel = seaLevel;

    }
    //TODO: change flotsam data object into a handler for flotsam. use list to store ref and remove entries once full
    //boats should live enough as long as limit large enough.
    private void spawnFlotsam(){
        if (oceanWorld.getPlayers().isEmpty()){
            return;
        }
        Bukkit.getLogger().info("trying to spawn flotsam");
        //flotsamSpawner.findValidLocation(waterCurrentVector, oceanWorld.getSimulationDistance()*8);
        flotsamSpawner.findValidLocation(currents.getCurrentDirection("water").multiply(-1).add(new Vector(-1,0,0)), oceanWorld.getSimulationDistance()*4);
        flotsamSpawner.updateSpawner(
                DTKOceanSurvival.plugin.getServer().getLootTable(new NamespacedKey(DTKOceanSurvival.plugin, "chests/flotsam")),
                Boat.Type.values()[oceanRandom.nextInt(Boat.Type.values().length)]
        );
        flotsamSpawner.spawnFlotsam();
    }


    @EventHandler
    public void onPlayerSpawnLocationEvent(PlayerSpawnLocationEvent event) {
        if (!event.getPlayer().getWorld().equals(oceanWorld)) {
            Location location = new Location(oceanWorld, 0, seaLevel+1, 0);
            if(location.getBlock().getType().isSolid()){
                for(int y = seaLevel; y < oceanWorld.getMaxHeight(); y++){
                    if (!location.getBlock().getType().isSolid()){
                        location.setY(y);
                        break;
                    }
                }
            }
            event.setSpawnLocation(location);
            event.getPlayer().setBedSpawnLocation(location);
        }
    }

    //TODO: try to override the generation datapack for this world structure
    //making road and such generate at sea floor and not on to of liquids or in the middles of the ocean

    /*
    @EventHandler
    public void onEntitiesUnloadEvent(EntitiesUnloadEvent event){

    }
    */
    @EventHandler
    public void onEntityAirChangeEvent(EntityAirChangeEvent event){

        if (((LivingEntity)event.getEntity()).getEyeLocation().getBlock().getType().isAir()){
            int amount = event.getAmount();
            //the idea is to reduce air regen base on depth
            if(amount < 300){
                if(event.getEntity().getWorld().equals(oceanWorld)){
                    double roll = oceanRandom.nextDouble()*seaLevel;
                    double y = event.getEntity().getLocation().getY();
                    if(y < roll ){ //is player is below 0 or sea level(base on roll) override breath
                        if(y + 64 <= oceanRandom.nextDouble()*roll*4) { //0-192 with y being 0-160
                            event.setCancelled(true);
                        }
                    }

                    //will only override it for this world
                }

            }
        }


        //Bukkit.getLogger().info("air change trigger : " + event.getAmount());
    }


    //TODO: to have structure from datapacks, all aspects may need to be added(or inlcuded)

}
