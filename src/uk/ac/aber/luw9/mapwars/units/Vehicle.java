package uk.ac.aber.luw9.mapwars.units;

import org.osmdroid.util.GeoPoint;

import android.util.Log;

/**
 * Extends unit class adding the ability to move
 * 
 * @author Luke Ward
 */
public class Vehicle extends Unit {

	private GeoPoint location, targetLocation;
	
	public Vehicle(int id, int owner, GeoPoint location) {
		super(id, owner, UnitType.VEHICLE, location);
		this.targetLocation = location;
	}

	public void changeLocation(GeoPoint location) {
		this.location = location;
	}
	
	public void setTargetLocation(GeoPoint location) {
		this.targetLocation = location;
		Log.i("UnitSetTarget", location.toString());
	}
	
	public GeoPoint getTargetLocation() {
		return targetLocation;
	}
}
