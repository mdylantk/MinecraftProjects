package dtk.oceansurvival.gameplay;

import dtk.oceansurvival.DTKOceanSurvival;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.*;


class CurrentData {
    //store data about a current like the current direction, magnitude, and noise modification
    SimplexOctaveGenerator noise;
    private Vector direction;
    //max range is the max roll before offset
    Vector directionMaxRange = new Vector(2,0,2);
    //This is the offset to max range. max of 2 with an offset of -1 will be a range of -1 to 1
    Vector directionRangeOffset = new Vector(0,0,0);
    //double maxSpeed = 0.1;//this should be handle in the main class that deals with world and velocity
    double magnitude = 1;
    double frequency = 0.01;
    double amplitude = 0.01;

    public CurrentData(Vector new_direction, SimplexOctaveGenerator new_noise){
        setDirection(new_direction);
        noise = new_noise;
    }

    void setDirection(Vector new_direction){
        //cloning to prevent unwanted references
        direction = new_direction.clone();
    }
    Vector getDirection(){
        //not returning a clone so other sources can observer it.
        return direction;
    }
    Vector getForce(double time, double strength){
        double noiseModifier = (noise.noise(time,frequency,amplitude)+1)/2;
        return direction.clone().multiply(magnitude*noiseModifier*strength);
    }
    //offsets are just a 2d point in the noise to allow different generation form noise generated for magnitude
    void changeDirection(double time, double offsetX, double offsetZ){
        double x = noise.noise(offsetX,time,frequency,amplitude) + directionRangeOffset.getX();
        double z = noise.noise(offsetZ,time,frequency,amplitude) + directionRangeOffset.getZ();
        direction.setX(x);
        direction.setZ(z);
        direction.normalize();
    }


}

public class Currents implements Listener {
    public static Currents currentHandler;
    Map<String,CurrentData> activeCurrents = new HashMap();
    private BukkitTask currentUpdateTimer = null;
    private World world;
    double time;
    //moving this here since most entities effected will be the same
    List<EntityType> effectedEntities = Arrays.asList(EntityType.BOAT,EntityType.CHEST_BOAT, EntityType.DROPPED_ITEM);

    public Currents(World new_world){
        currentHandler = this;
        world = new_world;
        time = -new Random(world.getSeed()).nextInt(9999);
        if(!activeCurrents.containsKey("water")) {
            activeCurrents.put("water", new CurrentData(new Vector(1, 0, 0.5), new SimplexOctaveGenerator(world, 1)));
            activeCurrents.get("water").noise.setScale(0.01);
            activeCurrents.get("water").directionRangeOffset.setX(1);
            activeCurrents.get("water").directionRangeOffset.setZ(0.25);
        }
        if(!activeCurrents.containsKey("air")) {
            activeCurrents.put("air", new CurrentData(new Vector(0.5, 0, 0.5), new SimplexOctaveGenerator(world, 4)));
            activeCurrents.get("air").magnitude = 0.3;
            activeCurrents.get("air").noise.setScale(0.001);
        }

        if (currentUpdateTimer == null) {
            currentUpdateTimer = Bukkit.getServer().getScheduler().runTaskTimer(DTKOceanSurvival.plugin, this::currentUpdate, 600, 10);
        }
    }
    public Vector getCurrentDirection(String name){
        CurrentData data = activeCurrents.get(name);
        if(data != null){
            return data.getDirection();
        }
        return null;
    }

    void currentUpdate(){
        time++;
        //todo: add velocity to all effected entity and randomly change the direction of the currents
        activeCurrents.get("water").changeDirection(time,500, -800);
        activeCurrents.get("air").changeDirection(time,900, -300);
        double weatherModifier = 0.015;
        if(world.isThundering()){
            weatherModifier = 0.02;
        }
        else if (world.isClearWeather()){
            weatherModifier = 0.01;
        }
        //Todo: maybe add a time var so time is the amount of times this is called.
        Vector waterCurrentForce = activeCurrents.get("water").getForce(time, weatherModifier);
        Vector airCurrentForce = activeCurrents.get("air").getForce(time, weatherModifier);
        for(EntityType type : effectedEntities){
            for (Entity entity: world.getEntitiesByClass(type.getEntityClass())){

                if (entity.isInWater()) {
                    if (entity.getPassengers().isEmpty() || !entity.getPassengers().get(0).getType().equals(EntityType.PLAYER) ) {
                        if (entity.getVelocity().length() <= 1) {
                            if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
                                entity.setVelocity(((entity.getVelocity().add(waterCurrentForce)).add(airCurrentForce)).multiply(0.25));
                            } else {
                                entity.setVelocity((entity.getVelocity().add(waterCurrentForce)).add(airCurrentForce));
                            }

                            //NOTE: the random enity could be noise, but should be diffrent from the octave generator used forcurrent
                            //as it now it add suddle diviation
                        }
                    }
                }
                else if (entity.getLocation().clone().add(0,-1,0).getBlock().getType().equals(Material.ICE)){
                    if (entity.getPassengers().isEmpty() || !entity.getPassengers().get(0).getType().equals(EntityType.PLAYER) ) {
                        if (entity.getVelocity().length() <= 1) {
                            if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
                                entity.setVelocity((entity.getVelocity().add(airCurrentForce)).multiply(0.25));
                            } else {
                                entity.setVelocity((entity.getVelocity().add(airCurrentForce)));
                            }
                        }
                    }

                }

            }
        }
    }


}
