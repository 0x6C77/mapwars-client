package uk.ac.aber.luw9.mapwars.units;

import org.osmdroid.util.GeoPoint;

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
	
	public boolean underAttack() {
		return ((System.currentTimeMillis() - lastAttacked) < 5000);
	}

}
