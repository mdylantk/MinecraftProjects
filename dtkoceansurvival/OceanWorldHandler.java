package dtkoceansurvival.dtkoceansurvival;


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
    private JavaPlugin owner;
    private World oceanWorld;
    private int seaLevel = 128;
    private BukkitTask waterCurrentTimer;
    private BukkitTask flotsamTimer;
    private Random oceanRandom = new Random();

    private Vector waterCurrentVector = new Vector(0.0, 0.0, 0.0);
    private Vector windCurrentVector = new Vector(0.0, 0.0, 0.0);
    private SimplexOctaveGenerator waterCurrentNoise; //to help smooth the vector
    private SimplexOctaveGenerator windCurrentNoise;
    private double waterCurrentMaxSpeed = 0.1;//0.08;
    private double windCurrentMaxSpeed = 0.1;

    private static List <Material> defaultFlotsam = Arrays.asList(
            Material.GLASS_BOTTLE, Material.STRING,Material.STICK, Material.BAMBOO,
            Material.OAK_PLANKS, Material.OAK_WOOD, Material.APPLE, Material.LILY_PAD);
    private int flotsamBoatLimit = 16;
    private FlotsamSpawner flotsamSpawner;

    OceanWorldHandler(JavaPlugin new_owner){
        if (new_owner == null){return;}
        owner = new_owner;
        Bukkit.getServer().getPluginManager().registerEvents(this,owner);
        //Bukkit.getLogger().info( " WEO STATS: " + Bukkit.getServer().getWorld("world_endless_ocean").toString());
        if (Bukkit.getServer().getWorld("world_endless_ocean") == null) {
            WorldCreator wc = new WorldCreator("world_endless_ocean");
            //todo: copy a level.dat modify it to allow plugins datatable modifications
            //wc.environment(World.Environment.NORMAL);
            //wc.type(WorldType.NORMAL);
            wc.generator(new FloodedOverworld(seaLevel));

            Bukkit.getLogger().info("GEN SET!: " +wc.generatorSettings());
            wc.createWorld();

            Bukkit.getLogger().info("creating new WEO");
            oceanWorld = Bukkit.getServer().getWorld("world_endless_ocean");
            oceanWorld.getPopulators().add(new OceanFloorPopulator());
            oceanWorld.getPopulators().add(new RuinPopulator());
            int spawnRadius = oceanWorld.getSimulationDistance()/2;
            Bukkit.getLogger().info("pregen endless ocean spawn");
            //for (int x = -spawnRadius; x <= spawnRadius; x++) {
            //    for (int z = -spawnRadius; z <= spawnRadius; z++) {
            //        oceanWorld.loadChunk(x,z);
            //    }
            //}
            //for (FeatureFlag test : oceanWorld.getFeatureFlags()){
            //for (World.Environment test : World.Environment.values()) {
            //    Bukkit.getLogger().info("has world type: " + test.toString());
            //}
            if(63 != oceanWorld.getSeaLevel()) {
                Bukkit.getLogger().info("sea level?: " + oceanWorld.getSeaLevel());
                seaLevel = oceanWorld.getSeaLevel();
            }

        }
        else{
            oceanWorld = Bukkit.getServer().getWorld("world_endless_ocean");
        }

        if (waterCurrentTimer == null) {
            waterCurrentTimer = Bukkit.getServer().getScheduler().runTaskTimer(owner, this::waterCurrent, 600, 20);
        }
        if (flotsamTimer == null) {
            Bukkit.getLogger().info("creating timer");
            flotsamTimer = Bukkit.getServer().getScheduler().runTaskTimer(owner, this::spawnFlotsam, 600, 900);
        }

        flotsamSpawner = new FlotsamSpawner(owner, oceanWorld,oceanRandom);
        flotsamSpawner.seaLevel = seaLevel;

        //below is an example of a way to maybe abstractly reg list form abstract list
        //may be more difficult since list be an object and would need to check the true class
        //if (flotsamSpawner.getClass().isAssignableFrom(Listener.class)){
        //    Bukkit.getServer().getPluginManager().registerEvents(flotsamSpawner,owner);
        //}




    }
    //TODO: change flotsam data object into a handler for flotsam. use list to store ref and remove entries once full
    //boats should live enough as long as limit large enough.
    private void spawnFlotsam(){
        if (oceanWorld.getPlayers().isEmpty()){
            return;
        }
        Bukkit.getLogger().info("trying to spawn flotsam");
        flotsamSpawner.findValidLocation(waterCurrentVector, oceanWorld.getSimulationDistance()*8);
        flotsamSpawner.updateSpawner(
                owner.getServer().getLootTable(new NamespacedKey(owner, "chests/flotsam")),
                Boat.Type.values()[oceanRandom.nextInt(Boat.Type.values().length)]
        );
        flotsamSpawner.spawnFlotsam();

        //Player player = oceanWorld.getPlayers().get(oceanRandom.nextInt(oceanWorld.getPlayers().size()));
        //if (player.getWorld().equals(oceanWorld)) {
        //    int spawnSegmentMinSize = oceanWorld.getSimulationDistance()*4;
        //    int spawnSegmentMaxSize = oceanWorld.getSimulationDistance()*8;
            //clone the water current vector
        //    Vector newPoint = waterCurrentVector.clone();
            //normalize it so it a direction not a velocity
         //   newPoint.normalize();
            //invert it so it located in the opposit direction
         //   newPoint.multiply(-1);
            //extends it to a point half of the sim distance

         //   newPoint.multiply(spawnSegmentMaxSize);
            //get the point related to the player
            //newPoint.add(player.getLocation().toVector()); //TODO: need to redeisgn to add this after. location pass should be the spawn point
         //   newPoint.setY(seaLevel);//+ 1);
        //    int flotsamSpawnLength = max(64,oceanWorld.getSimulationDistance() * 8);
        //    newPoint.setX(oceanRandom.nextInt(spawnSegmentMinSize)-(spawnSegmentMinSize*0.25) + newPoint.getX()); //less negativve since we want the boats to spawn farther away. could probably save part of sim distance(like half) as the base caculation.
        //    newPoint.setZ(oceanRandom.nextInt(spawnSegmentMaxSize) - (spawnSegmentMaxSize*0.5) + newPoint.getZ());
        //    if (oceanWorld.getChunkAt((int) newPoint.getX(), (int) newPoint.getZ()).isLoaded()) {

                //if (oceanWorld.getBlockData(newPoint.getBlockX(), newPoint.getBlockY() - 1, newPoint.getBlockZ()).getMaterial().equals(Material.WATER) &&
                        //oceanWorld.getBlockData(newPoint.getBlockX(), newPoint.getBlockY(), newPoint.getBlockZ()).getMaterial().isAir()) {

         //           flotsamSpawner.updateSpawner(
         //                   owner.getServer().getLootTable(new NamespacedKey(owner, "chests/flotsam")),
         //                   Boat.Type.values()[oceanRandom.nextInt(Boat.Type.values().length)]
         //           );
                    //Bukkit.getLogger().info("reading item spawn for " + player.getName());
         //           Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(owner, () -> flotsamSpawner.spawnFlotsam(), 100);
                //}
                //else{Bukkit.getLogger().info("but  no water below");}

          //  }
         //   else{Bukkit.getLogger().info("but chunk not loaded");}
            //Bukkit.getLogger().info("player:"+player.getLocation().toVector()+" item:"+newPoint+" SD:"+oceanWorld.getSimulationDistance());
        //}
    }

    private void waterCurrent(){
        //Bukkit.getLogger().info("time : " + oceanWorld.getTime());
        //Bukkit.getLogger().info("game time : " + oceanWorld.getGameTime()); //is bigger and may be useful, but not sure what happens after 2b
        double maxWaterSpeedModifier;
        double maxWindSpeedModifier;
        double amplitudeModifier;
        double frequencyModifier;
        if (!oceanWorld.isClearWeather()){
            if (oceanWorld.isThundering()){
                maxWaterSpeedModifier = waterCurrentMaxSpeed * 0.5;
                maxWindSpeedModifier = windCurrentMaxSpeed *0.4;

                amplitudeModifier = 1;
                frequencyModifier = .005;
            }
            else {
                maxWaterSpeedModifier = waterCurrentMaxSpeed * 0.4;
                maxWindSpeedModifier = windCurrentMaxSpeed *0.2;

                amplitudeModifier = 0.5;
                frequencyModifier = .001;
            }
        }
        else {
            maxWaterSpeedModifier = waterCurrentMaxSpeed * 0.3;
            maxWindSpeedModifier = windCurrentMaxSpeed *0.1;

            amplitudeModifier = 0.1;
            frequencyModifier = .0001;
        }
        if (waterCurrentNoise == null){
            waterCurrentNoise = new SimplexOctaveGenerator(oceanWorld, 2);
            waterCurrentNoise.setScale(.001D);
        }
        if(windCurrentNoise == null ){
            windCurrentNoise = new SimplexOctaveGenerator(oceanWorld, 4);
            windCurrentNoise.setScale(.0001D);
        }
        maxWaterSpeedModifier *= (waterCurrentNoise.noise(oceanWorld.getTime(), frequencyModifier,amplitudeModifier)+1);
        maxWindSpeedModifier *= (windCurrentNoise.noise(oceanWorld.getTime(), frequencyModifier,amplitudeModifier)+1);

        waterCurrentVector.setX(( waterCurrentNoise.noise(oceanWorld.getTime(), 0, frequencyModifier, amplitudeModifier)/2)+1);
        waterCurrentVector.setZ( waterCurrentNoise.noise(0,oceanWorld.getTime(), frequencyModifier, amplitudeModifier));

        windCurrentVector.setX( windCurrentNoise.noise(oceanWorld.getTime(), 0, frequencyModifier, amplitudeModifier));
        windCurrentVector.setZ( windCurrentNoise.noise(0,oceanWorld.getTime(), frequencyModifier, amplitudeModifier));

        waterCurrentVector.multiply(maxWaterSpeedModifier); //Might be able to modify water current vector directly, but only of it pos is not used in noise pos for changing it coord
        windCurrentVector.multiply(maxWindSpeedModifier);
        //Bukkit.getLogger().info("vector "+waterCurrentVector.toString());

        List<Entity> entityList = oceanWorld.getEntities();
        if (!entityList.isEmpty()) {
            for (Entity entity : entityList) {
                //TODO: probably add a wind vector for ice. it can also change how watercurrent work. current could be location base and wind is time
                if (entity.isInWater()) {
                    if (entity.getType().equals(EntityType.BOAT) || entity.getType().equals(EntityType.CHEST_BOAT) || entity.getType().equals(EntityType.DROPPED_ITEM)
                    ) {
                        if (entity.getPassengers().isEmpty() || !entity.getPassengers().get(0).getType().equals(EntityType.PLAYER) ) {
                            if (entity.getVelocity().length() <= waterCurrentMaxSpeed + windCurrentMaxSpeed ) {
                                if (entity.getType().equals(EntityType.DROPPED_ITEM)){
                                    entity.setVelocity(entity.getVelocity().add(waterCurrentVector).add(windCurrentVector).multiply(0.25));
                                }
                                else{
                                    entity.setVelocity(entity.getVelocity().add(waterCurrentVector).add(windCurrentVector));//.multiply(oceanRandom.nextDouble()/2+0.5));
                                }

                                //NOTE: the random enity could be noise, but should be diffrent from the octave generator used forcurrent
                                //as it now it add suddle diviation
                            }
                        }
                    }
                }
                else if (entity.getLocation().clone().add(0,-1,0).getBlock().getType().equals(Material.ICE)){
                    if (entity.getType().equals(EntityType.BOAT) || entity.getType().equals(EntityType.CHEST_BOAT) || entity.getType().equals(EntityType.DROPPED_ITEM)
                    ) {
                        if (entity.getPassengers().isEmpty() || !entity.getPassengers().get(0).getType().equals(EntityType.PLAYER) ) {
                            if (entity.getVelocity().length() <=  windCurrentMaxSpeed ) {
                                if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
                                    entity.setVelocity(entity.getVelocity().add(windCurrentVector).multiply(0.25));
                                } else {
                                    entity.setVelocity(entity.getVelocity().add(windCurrentVector));//.multiply(oceanRandom.nextDouble()/2+0.5));
                                }
                            }
                        }
                    }
                }
            }
        }
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

    //@EventHandler
   // public void onAsyncStructureSpawnEvent(AsyncStructureSpawnEvent event) {
    //    event.getStructure().toString();
        //dirty way to try to block gen. village roads and strongholds is an issues (woodland mansion is unknown) due to floating
        //if (event.getWorld().equals(oceanWorld)) {
            //if (event.getStructure().getStructureType().equals(StructureType.OCEAN_MONUMENT)) {
            //    Bukkit.getLogger().info("blocking OCEAN_MONUMENT");
            //    event.setCancelled(true);
        //    if (event.getStructure().getStructureType().equals(StructureType.STRONGHOLD)) {
                //Bukkit.getLogger().info("blocking STRONGHOLD");
         //       event.setCancelled(true);
         //   } //else if (event.getStructure().getStructureType().equals(StructureType.WOODLAND_MANSION)) {
            //    Bukkit.getLogger().info("blocking WOODLAND_MANSION");
            //    event.setCancelled(true);
            // } else if (event.getStructure().getStructureType().equals(StructureType.DESERT_PYRAMID)) {
            //    Bukkit.getLogger().info("blocking DESERT_PYRAMID");
            //    event.setCancelled(true);
            //}
            //else
              //  if (event.getStructure().getStructureType().equals(StructureType.JIGSAW)) {
                  //  if (event.getStructure().getKey().toString().contains("village")) {
                   //     Bukkit.getLogger().info("checking village gen");

                       // if(event.getBoundingBox().getMinY() < seaLevel-3 ||
                                //issue is snowy plains may be a spot they spawn than anything with frozen
                       //         event.getWorld().getBiome(event.getBoundingBox().getCenter().toLocation(event.getWorld())).getKey().toString().contains("snowy")) {
                       //     Bukkit.getLogger().info("blocking village gen");
                       //     event.setCancelled(true);
                       // }else {
                            //still not the best, most likly no villages will spawn
                           // Bukkit.getLogger().info("allowed village gen: " + event.getBoundingBox().getCenter().toString());
                        //}
                    //}
                //}
                //Bukkit.getLogger().info("blocking JIGSAW : " + event.getStructure().toString());
                //TODO: /locate may crash if trying to locate one of these ban builds
          //      event.setCancelled(true);
           // } else {
          //      event.setCancelled(false);
          //  }
       // }
   //}


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
