package dtk.oceansurvival.gameplay;

import dtk.oceansurvival.DTKOceanSurvival;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.*;

import static org.bukkit.Registry.LOOT_TABLES;
import static org.joml.Math.lerp;

public class FlotsamSpawner implements Listener {
    private Random random;
    private Location spawnOriginLocation; //the origin use to determine the spawn. (aka the reverse current pos offset by player
    private Location spawnLocation; //the actual spawn location
    private LootTable lootTable;
    private Boat.Type type;
    private EntityType mount = null;
    private int maxRange = 64;
    public int seaLevel = 64;
    String flotsamID = DTKOceanSurvival.plugin.getName()+":"+"flotsam";

    private List<EntityType> commonMounts =  Arrays.asList(
            EntityType.SKELETON,EntityType.PILLAGER,EntityType.DROWNED,EntityType.ZOMBIE,EntityType.CREEPER,
            EntityType.ZOMBIE_VILLAGER,EntityType.SPIDER,EntityType.PIG,EntityType.COW,EntityType.CHICKEN,
            EntityType.SHEEP,EntityType.VILLAGER,EntityType.EVOKER);

    private LinkedList<Entity> activeFlotsam = new LinkedList();

    public int maxFlotsam = 12;


    //TODO: maybe call this Flotsam handler? and have it handle water currents too?
    public FlotsamSpawner(JavaPlugin owner, World world, Random new_random) {
        random = new_random;
        spawnOriginLocation = new Location(world, 0, 0, 0);
        Bukkit.getServer().getPluginManager().registerEvents(this, DTKOceanSurvival.plugin);

    }

    public void updateSpawner(LootTable new_lootTable, Boat.Type new_type){ ;

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

        Vector spawnDirection = direction.clone();
        maxRange = range;
        spawnOriginLocation.setX(spawnDirection.getX());
        spawnOriginLocation.setY(spawnDirection.getY());
        spawnOriginLocation.setZ(spawnDirection.getZ());
        spawnOriginLocation.multiply(maxRange);


        //get a x and y
        //then from the sky, or a define sea level, find vaild blocks
    }
    public void findRandomPoint(){
        double randomX = random.nextDouble()*2-1;
        double randomZ = random.nextDouble()*2-1;
        spawnLocation = spawnOriginLocation.clone();

        spawnLocation.add(randomX*(maxRange*0.25),0,randomZ*(maxRange*1.5));
        //todo: aline the box base on min and max direction axies. max should have less range than min
        //todo: also break the range into groups. less should spawn near the player and less far away.
        //could check the roll and any below 10% have a chance to spawn away. similar with distance.


        //spawnLocation.add(randomX*(maxRange*0.25),0,randomZ*maxRange);//need to get the adj vector to get a random point on
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
                    }
                    Bukkit.getServer().getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("DTKOceanSurvival"), ()->onSpawn(spawnLocation.clone()), 10);
                }
            }
        }
    }

    private void onSpawn(Location location){
        ChestBoat flotsamBoat = ((ChestBoat) location.getWorld().spawnEntity(location, EntityType.CHEST_BOAT));
        if(mount != null) {
            Entity spawnedMount = location.getWorld().spawnEntity(location, mount);
            spawnedMount.setPersistent(false);
            spawnedMount.setMetadata(flotsamID,new FixedMetadataValue(DTKOceanSurvival.plugin,true));
            if(spawnedMount.getType().equals(EntityType.SKELETON) ||
                    spawnedMount.getType().equals(EntityType.STRAY) ||
                    spawnedMount.getType().equals(EntityType.ZOMBIE) ||
                    spawnedMount.getType().equals(EntityType.ZOMBIE_VILLAGER)){
                ((LivingEntity)spawnedMount).getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            }

            flotsamBoat.addPassenger(spawnedMount);
        }
        flotsamBoat.setMetadata(flotsamID,new FixedMetadataValue(DTKOceanSurvival.plugin,true));
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
        if (event.getMount().hasMetadata(flotsamID)){
            if(!event.getEntity().hasMetadata(flotsamID)) {
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
        if (event.getVehicle().hasMetadata(flotsamID)){
            event.setCancelled(true);
            event.getVehicle().remove();
            //trying to disable it dropping itself
        }
        else{
            event.setCancelled(false);
        }
    }
    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        try {
            Entity entity = (Entity) event.getInventory().getHolder();
            if (entity != null) {
                if (entity.hasMetadata(flotsamID)) {
                    event.setCancelled(true);
                    entity.remove();
                }
            }
        }
        catch(Exception e){
            //this will happen if holder is not an entity and is here to keep the log from being filled with red
        }
    }

}
