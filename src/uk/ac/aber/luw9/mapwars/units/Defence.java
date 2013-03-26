package uk.ac.aber.luw9.mapwars.units;

import org.osmdroid.util.GeoPoint;

public class Defence extends Unit {
	
	public Defence(int id, int owner, GeoPoint location) {
		super(id, owner, UnitType.DEFENCE, location);
	}

}
