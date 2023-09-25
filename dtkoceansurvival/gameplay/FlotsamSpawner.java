package dtkoceansurvival.dtkoceansurvival.gameplay;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.*;

import static org.bukkit.Registry.LOOT_TABLES;

public class FlotsamSpawner implements Listener {
    private Random random;
    private Location spawnOriginLocation; //the origin use to determine the spawn. (aka the reverse current pos offset by player
    private Location spawnLocation; //the actual spawn location
    private LootTable lootTable;
    private Boat.Type type;
    private EntityType mount = null;
    private int maxRange = 64;
    public int seaLevel = 64; //

    private List<EntityType> commonMounts =  Arrays.asList(
            EntityType.SKELETON,EntityType.PILLAGER,EntityType.DROWNED,EntityType.ZOMBIE,EntityType.CREEPER,
            EntityType.ZOMBIE_VILLAGER,EntityType.SPIDER,EntityType.PIG,EntityType.COW,EntityType.CHICKEN,
            EntityType.SHEEP,EntityType.VILLAGER,EntityType.EVOKER);

    private LinkedList<Entity> activeFlotsam = new LinkedList();

    public int maxFlotsam = 8;


    //TODO: maybe call this Flotsam handler? and have it handle water currents too?
    public FlotsamSpawner(JavaPlugin owner, World world, Random new_random) {
        random = new_random;
        spawnOriginLocation = new Location(world, 0, 0, 0);
        Bukkit.getServer().getPluginManager().registerEvents(this,owner);

    }

    //FlotsamSpawner

    //this holds the state of a currently generating flotsam object
    public void updateSpawner(LootTable new_lootTable, Boat.Type new_type){

        //if (world.getPlayers().isEmpty()) { return; }
        //Player player = world.getPlayers().get(random.nextInt(world.getPlayers().size()));
        //spawnLocation = spawnOriginLocation.clone().add(player.getLocation());
        //spawnOriginLocation.add(player.getLocation().getX(),0,player.getLocation().getZ());

        //TODO:automate the random stuff mostly the boat type. everytime spawn is call, should run a function to pick stuff
        if (new_lootTable == null) {
            Bukkit.getServer().getLogger().info("plugin datapack missing? no flotsam loot table");
            lootTable = LOOT_TABLES.get(LootTables.SPAWN_BONUS_CHEST.getKey()).getLootTable();
        }
        else{ lootTable = new_lootTable; }

        if(new_type == null){ type = Boat.Type.OAK; }
        else{ type = new_type;}
    }

    public void findValidLocation(Vector direction, int range){
        maxRange = range;
        spawnOriginLocation.setDirection(direction.clone());
        spawnOriginLocation.multiply(maxRange);

        //get a x and y
        //then from the sky, or a define sea level, find vaild blocks
    }
    public void findRandomPoint(){
        double randomX = random.nextDouble()*2-1;
        double randomZ = random.nextDouble()*2-2;
        spawnLocation = spawnOriginLocation.clone();

        spawnLocation.add(randomX*(maxRange*0.25),0,randomZ*maxRange);//need to get the adj vector to get a random point on
        //while(!spawnLocation.getBlock().isLiquid()&& spawnLocation.getBlockY() < spawnLocation.getWorld().getMaxHeight()){
        //    spawnLocation.add(0,1,0);
        //}
        //while(!(spawnLocation.getBlock().isLiquid() || spawnLocation.getBlock().getType().equals(Material.ICE))&& spawnLocation.getBlockY() < spawnLocation.getWorld().getMaxHeight()){
        //    spawnLocation.add(0,1,0);
        //}

    }

    //should check from air untill water/ice is found
    public void findSeaLevel(){
        World world = spawnLocation.getWorld();
        int x = spawnLocation.getBlockX();
        int z = spawnLocation.getBlockZ();
        for(int y = world.getMaxHeight(); y > 0; y--){
            if(world.getBlockAt(x,y,z).getType().isAir()){
                if(world.getBlockAt(x,y-1,z).isLiquid() || world.getBlockAt(x,y-1,z).getType().equals(Material.ICE)){
                    spawnLocation.setY(y-1);
                    break;
                }
            }
        }
    }


    //this will try to spawn the boat. probably should pass stuff as parametters to prvent issues
    public void spawnFlotsam(){
        if (spawnOriginLocation.getWorld().getPlayers().isEmpty()) { return; }
        Player player = spawnOriginLocation.getWorld().getPlayers().get(random.nextInt(spawnOriginLocation.getWorld().getPlayers().size()));
        findRandomPoint();
        spawnLocation.add(player.getLocation().getX(),spawnLocation.getWorld().getSeaLevel(),player.getLocation().getZ());
        findSeaLevel();
        if (spawnLocation.getChunk().isLoaded()) {
            //above a failsafe incase the player is moving too fast
            Bukkit.getLogger().info("checking: "+ spawnLocation);
            if (spawnLocation.getBlock().isLiquid() || spawnLocation.getBlock().getType().equals(Material.ICE)) { //Issue is the y is getting overriden by player location
                if (spawnLocation.getBlock().getType().equals(Material.ICE) && random.nextInt(20) >= 8){
                    mount = EntityType.STRAY;
                }
                else if(random.nextInt(20) >= 16){
                    mount = EntityType.SKELETON;
                }
                else if(random.nextInt(100) <= 5){
                    mount = commonMounts.get(random.nextInt(commonMounts.size()));
                }
                else{
                    mount = null;
                }
                if (spawnLocation.add(0, 1, 0).getBlock().getType().isAir()) { //adding +1 since it need to spawn in the air
                    if (activeFlotsam.size() > maxFlotsam) {
                        Entity oldFlotsamBoat = activeFlotsam.removeFirst();
                        if (oldFlotsamBoat != null) {
                            ((ChestBoat) oldFlotsamBoat).setLootTable(null);
                            oldFlotsamBoat.remove();
                        }
                        Bukkit.getLogger().info("removing boat");
                    }

                    //ChestBoat flotsamBoat = ((ChestBoat) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.CHEST_BOAT));
                    //flotsamBoat.setBoatType(type);
                    //flotsamBoat.setLootTable(lootTable);
                    //flotsamBoat.setVelocity(new Vector());
                    //flotsamBoat.setPersistent(false);
                    //flotsamBoat.setCustomName("Seaworn boat");
                    //flotsamBoat.setCustomNameVisible(true);
                    //activeFlotsam.add(flotsamBoat);
                    Bukkit.getServer().getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("DTKOceanSurvival"), ()->onSpawn(spawnLocation.clone()), 10);

                }
                else{
                    Bukkit.getLogger().info("incorrect block(not air?)" );
                }
            }
            else{
                Bukkit.getLogger().info("incorrect block(not water)");
            }
        }
        else {
            Bukkit.getLogger().info("invaild chunk");

        }
    }

    private void onSpawn(Location location){
        Bukkit.getLogger().info("spawning at: "+ location);
        ChestBoat flotsamBoat = ((ChestBoat) location.getWorld().spawnEntity(location, EntityType.CHEST_BOAT));
        if(mount != null) {
            Entity spawnedMount = location.getWorld().spawnEntity(location, mount);
            spawnedMount.setPersistent(false);
            if(spawnedMount.getType().equals(EntityType.SKELETON) ||
                    spawnedMount.getType().equals(EntityType.STRAY) ||
                    spawnedMount.getType().equals(EntityType.ZOMBIE) ||
                    spawnedMount.getType().equals(EntityType.ZOMBIE_VILLAGER)){
                ((LivingEntity)spawnedMount).getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            }

            flotsamBoat.addPassenger(spawnedMount);
        }
        flotsamBoat.setBoatType(type);
        flotsamBoat.setLootTable(lootTable);
        flotsamBoat.setVelocity(new Vector());
        flotsamBoat.setPersistent(false);
        flotsamBoat.setCustomName("Worn boat");
        flotsamBoat.setCustomNameVisible(true);
        activeFlotsam.add(flotsamBoat);
    }

    @EventHandler
    public void onEntityMountEvent(EntityMountEvent event){
        if (!event.getMount().isPersistent()){
            if(event.getEntity().isPersistent()) {
                event.setCancelled(true);
                event.getMount().remove();
            }
        }
        else{
            event.setCancelled(false);
        }
    }

    //just need to check for meta to make this easier
    @EventHandler
    public void onVehicleDestroyEvent(VehicleDestroyEvent event){
        //TODO: use this to help manage flotsam boats
        //event.getVehicle().isPersistent()
        if (!event.getVehicle().isPersistent()){
            event.setCancelled(true);
            event.getVehicle().remove();
            //trying to disable it dropping itself
        }
        else{
            event.setCancelled(false);
        }
    }

}
