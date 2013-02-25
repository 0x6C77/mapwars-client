package uk.ac.aber.luw9.mapwars.units;

import org.osmdroid.util.GeoPoint;

public abstract class Unit {
	private String id;
	private String owner;
	private GeoPoint location;
	private UnitType type;
	private boolean selected;
	private float bearing;
	
	public Unit(String id, String owner, UnitType type, GeoPoint location) {
		this.id = id;
		this.owner = owner;
		this.location = location;
		this.type = type; 
	}
	
	public String getId() {
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
	
	public String getOwner() {
		if (owner == null)
			return "You";
		return owner;
	}
	
	public boolean amOwner() {
		return (owner == null);
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

}
