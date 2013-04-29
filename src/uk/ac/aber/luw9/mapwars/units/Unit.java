package uk.ac.aber.luw9.mapwars.units;

import org.osmdroid.util.GeoPoint;

/**
 * Abstract unit class stores details about each unit, including its location,
 * bearing and health
 * 
 * @author Luke Ward
 */
public abstract class Unit {

	private int id;
	private int owner;
	private GeoPoint location;
	private UnitType type;
	private boolean selected;
	private float bearing;
	private int health;
	private long lastAttacked;
	
	public Unit(int id, int owner, UnitType type, GeoPoint location) {
		this.id = id;
		this.owner = owner;
		this.location = location;
		this.type = type; 
	}
	
	public int getId() {
		return id;
	}
	
	public GeoPoint getLocation() {
		return location;
	}
	
	public UnitType getType() {
		return type;
	}
	
	public void setType(UnitType type) {
		this.type = type;
	}
	
	public void select() {
		selected = true;
	}
	
	public void unselect() {
		selected = false;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public int getOwner() {
		return owner;
	}
	
	public boolean amOwner() {
		return (owner == 0);
	}

	public float getBearing() {
		return bearing;
	}
	
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	
	@Override
	public String toString() {
		return this.type.toString() + " [" + this.owner + "]";
	}

	public void setHealth(int health) {
		if (health < this.health)
			lastAttacked = System.currentTimeMillis();
		this.health = health;
	}
	
	public int getHealth() {
		return health;
	}
	
	/**
	 * Determine if the unit is still under attack
	 * @return unit is under attack
	 */
	public boolean isUnderAttack() {
		return ((System.currentTimeMillis() - lastAttacked) < 5000);
	}

}
