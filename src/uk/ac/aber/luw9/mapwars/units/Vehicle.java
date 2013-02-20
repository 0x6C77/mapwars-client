package uk.ac.aber.luw9.mapwars.units;

import org.osmdroid.util.GeoPoint;

import android.util.Log;

public class Vehicle extends Unit {

	private GeoPoint location, targetLocation;
	private float bearing;
	private VehicleType vehicleType;
	
	public Vehicle(String id, String owner, VehicleType type, GeoPoint location) {
		super(id, owner, UnitType.VEHICLE, location);
		this.targetLocation = location;
		this.vehicleType = type;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public void setTargetLocation(GeoPoint location) {
		this.targetLocation = location;
		Log.i("UnitSetTarget", location.toString());
	}
	
	public GeoPoint getTargetLocation() {
		return targetLocation;
	}

	public float getBearing() {
		return bearing;
	}
	
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	
	public VehicleType getVehicleType() {
		return vehicleType;
	}
}
