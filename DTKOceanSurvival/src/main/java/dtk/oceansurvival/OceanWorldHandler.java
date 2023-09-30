package dtk.oceansurvival;


import dtk.oceansurvival.gameplay.FlotsamSpawner;
import dtk.oceansurvival.generators.FloodedOverworld;
import dtk.oceansurvival.populators.OceanFloorPopulator;
import dtk.oceansurvival.populators.RuinPopulator;
import dtk.oceansurvival.gameplay.Currents;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import java.util.*;

public class OceanWorldHandler implements Listener {
    //This handles and control logic that happens in ocean world.
    //private JavaPlugin owner;
    public static World oceanWorld;
    public static int seaLevel = 128;
    private BukkitTask flotsamTimer;
    private Random oceanRandom = new Random();


    //TODO:: maybe add a water/hydration system. more hydraded means less satiration drain. also water world will give
    //bottle of salt water unless from a cauldren with fire below or sag above it otherwise would need to be cooked
    //if cauldren have state, could have it be salt water if filled by salt water. state be removed when empty
    //Also move air regen rate stuff to its own class and have it check world name. world depth modifier and staring y
    //also clean code up more
    //TODO: try to find any load chunks and either remove or put them in a switch

    private FlotsamSpawner flotsamSpawner;
    private Currents currents;
    OceanWorldHandler(JavaPlugin new_owner){
        if (new_owner == null){return;}
        Bukkit.getServer().getPluginManager().registerEvents(this,DTKOceanSurvival.plugin);
        if (Bukkit.getServer().getWorld("world_endless_ocean") == null) {
            WorldCreator wc = new WorldCreator("world_endless_ocean");
            wc.generator(new FloodedOverworld(seaLevel));

            DTKOceanSurvival.log("GEN SET!: " +wc.generatorSettings());
            wc.createWorld();

            DTKOceanSurvival.log("creating new WEO");
            oceanWorld = Bukkit.getServer().getWorld("world_endless_ocean");
            oceanWorld.getPopulators().add(new OceanFloorPopulator());
            oceanWorld.getPopulators().add(new RuinPopulator());
            oceanWorld.setSpawnLocation(0,seaLevel,0);
        }
        else{
            oceanWorld = Bukkit.getServer().getWorld("world_endless_ocean");
            if(oceanWorld.getSpawnLocation().getY() != seaLevel) {
                oceanWorld.setSpawnLocation(0, seaLevel, 0);
            }
        }
        loadSpawn(1, 1);

        currents = new Currents(oceanWorld);
        if (flotsamTimer == null) {
            DTKOceanSurvival.log("creating timer");
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
        flotsamSpawner.findValidLocation(currents.getCurrentDirection("water").multiply(-1).add(new Vector(-1,0,0)), oceanWorld.getSimulationDistance()*4);
        flotsamSpawner.updateSpawner(
                DTKOceanSurvival.plugin.getServer().getLootTable(new NamespacedKey(DTKOceanSurvival.plugin, "chests/flotsam")),
                Boat.Type.values()[oceanRandom.nextInt(Boat.Type.values().length)]
        );
        flotsamSpawner.spawnFlotsam();
    }

    public void loadSpawn(int sizeX, int sizeZ){
        for(int x = sizeX; x >= -sizeX; x--){
            for(int z = sizeZ; z >= -sizeZ; z--){
                oceanWorld.loadChunk(x,z);
                DTKOceanSurvival.log("loading " + oceanWorld.getName() + " chunk at x:" + x + " z:" + z);
            }
        }
    }
    public void unloadChunks(){
        for( Chunk chunk : oceanWorld.getLoadedChunks()) {
            //may not be the best. is meant to be called if no player is in world
            //a brute unload all so may not be kept or modified later
            oceanWorld.unloadChunk(chunk);
        }
    }

    @EventHandler
    public void onPlayerSpawnLocationEvent(PlayerSpawnLocationEvent event) {
        //need a way to check if new player. could use metadata or achivement and require the player to visit one of
        //the other dim to be able to save their cords there and not be recall to ocean. also could store a meta
        //called last location that can be used in a similar way
        if (event.getPlayer().getBedSpawnLocation() == null && !event.getPlayer().getWorld().equals(oceanWorld)) {
            event.setSpawnLocation(oceanWorld.getSpawnLocation());
            //overrides world spawn to endless ocean
        }
    }


    @EventHandler
    public void onEntityAirChangeEvent(EntityAirChangeEvent event){
        //may see if world meta could work. then can use that to get data
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
                }
            }
        }


        //DTKOceanSurvival.log("air change trigger : " + event.getAmount());
    }


    //TODO: to have structure from datapacks, all aspects may need to be added(or inlcuded)

    @EventHandler
    public void onPortalCreateEvent(PortalCreateEvent event){
        if(event.getWorld().equals(oceanWorld)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event){
        if(event.getCause().equals(EntityDamageEvent.DamageCause.VOID)){
            if(event.getEntity().getWorld().equals(oceanWorld)){
                World otherWorld = event.getEntity().getServer().getWorld("world_nether");
                if (otherWorld != null) {
                    //should add a flag to remove blocks or spawn in air. issue with later is that it probably kill player
                    Location location = new Location(otherWorld, Math.floor(event.getEntity().getLocation().getX())+0.5, otherWorld.getLogicalHeight()-3, Math.floor(event.getEntity().getLocation().getZ())+0.5);
                    //otherWorld.loadChunk(location.getBlockX(),location.getBlockZ());
                    for(int y = otherWorld.getLogicalHeight(); y > otherWorld.getLogicalHeight() - 6; y--){
                        Block block = otherWorld.getBlockAt(location.getBlockX(),y,location.getBlockZ());
                        if (block.getType().equals(Material.BEDROCK) || block.getType().equals(Material.NETHERRACK)){
                            block.setType(Material.CAVE_AIR);
                        }
                    }
                    event.getEntity().teleport(location,PlayerTeleportEvent.TeleportCause.PLUGIN);
                    otherWorld.getBlockAt(location.getBlockX(),otherWorld.getLogicalHeight()-1,location.getBlockZ()).setType(Material.BEDROCK);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        if(event.getPlayer().getWorld().getName().equals("world_nether")) {
            Block block = event.getClickedBlock();
            if (block != null) {
                if(block.getType().equals(Material.BEDROCK)){
                    if(block.getY() >= event.getPlayer().getWorld().getLogicalHeight()-3){ //reducing the range so it more likly to use the block where someone spawned
                        Location location = new Location(oceanWorld,Math.floor(event.getPlayer().getLocation().getBlockX())+0.5,oceanWorld.getMinHeight(),Math.floor(event.getPlayer().getLocation().getBlockZ())+0.5);
                        //oceanWorld.loadChunk(location.getBlockX(),location.getBlockZ());
                        Block bottomBlock = oceanWorld.getBlockAt(location.getBlockX(),oceanWorld.getMinHeight(),location.getBlockZ());
                        if(bottomBlock.getType().isAir() || bottomBlock.isLiquid()){
                            bottomBlock.setType(Material.ICE);
                        }
                        event.setCancelled(true);
                        event.getPlayer().teleport(location,PlayerTeleportEvent.TeleportCause.PLUGIN);
                    }
                }
            }

        }
    }


}
