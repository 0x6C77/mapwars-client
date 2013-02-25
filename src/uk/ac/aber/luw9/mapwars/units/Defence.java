package uk.ac.aber.luw9.mapwars.units;

import org.osmdroid.util.GeoPoint;

public class Defence extends Unit {
	
	public Defence(String id, String owner, GeoPoint location) {
		super(id, owner, UnitType.DEFENCE, location);
	}

}
