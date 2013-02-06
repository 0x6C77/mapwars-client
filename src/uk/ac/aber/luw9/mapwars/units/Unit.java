package uk.ac.aber.luw9.mapwars.units;

import org.osmdroid.util.GeoPoint;

public class Unit {
	private String id;
	private String owner;
	private GeoPoint location, targetLocation;
	private UnitType type;
	private boolean selected;
	private float bearing;
	
	public Unit(String id, String owner, UnitType type, GeoPoint location) {
		this.id = id;
		this.owner = owner;
		this.location = location;
		this.targetLocation = location;
		this.type = type;
	}
	
	public String getId() {
		return id;
	}
	
	public GeoPoint getLocation() {
		return location;
	}
	
	public GeoPoint getTargetLocation() {
		return targetLocation;
	}
	
	public void setLocation(GeoPoint location) {			
		this.location = location;
	}
	
	public void setTargetLocation(GeoPoint location) {
		this.targetLocation = location;
	}
	
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	
	public float getBearing() {
		return bearing;
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
		return owner;
	}

}
