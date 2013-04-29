package uk.ac.aber.luw9.mapwars.controllers;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import uk.ac.aber.luw9.mapwars.GameMap;
import uk.ac.aber.luw9.mapwars.Utils;
import uk.ac.aber.luw9.mapwars.units.Structure;
import uk.ac.aber.luw9.mapwars.units.Unit;
import uk.ac.aber.luw9.mapwars.units.UnitOverlay;
import uk.ac.aber.luw9.mapwars.units.UnitType;
import uk.ac.aber.luw9.mapwars.units.Vehicle;
import android.location.Location;
import android.util.Log;

public class UnitController implements Runnable {
	private ArrayList<Unit> units = new ArrayList<Unit>();
	private UnitOverlay unitOverlay;
	private MainController mainController;
	private ScheduledExecutorService exec;
	private Boolean threadRunning = false;
	private GameMap map;

	public UnitController(GameMap map) {
		this.map = map;
		unitOverlay = new UnitOverlay(this, map);
		mainController = MainController.getController();
	}
	
	/**
	 * Gets a list of all known units
	 * 
	 * @return List of units
	 */
	public ArrayList<Unit> getUnits() {
		return getUnits(false);
	}
	
	/**
	 * Gets a list of units
	 * 
	 * @param userOnly list just users units or all units
	 * @return List of units
	 */
	public ArrayList<Unit> getUnits(boolean userOnly) {
		ArrayList<Unit> response = new ArrayList<Unit>();
		for (Unit unit : units) {
			if (!userOnly || unit.amOwner()) {
				response.add(unit);
			}
		}	
		return response;
	}
	
	/**
	 * Gets a single units
	 * 
	 * @param userOnly list just users units or all units
	 * @return List of units
	 */
	public Unit getUnit(int id) {
		for (Unit unit : units) {
			if (unit.getId() == id) {
				return unit;
			}
		}
		return null;
	}
	
	/**
	 * Add unit to list
	 * 
	 * @param unit unit to be added
	 */
	public void addUnit(Unit unit) {
		units.add(unit);
		
		//If thread is not running to track units, start it
		if (!threadRunning) {
			threadRunning = true; 
			exec = Executors.newSingleThreadScheduledExecutor();
			exec.scheduleAtFixedRate(this, 0, 20, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * Check if unit id exists
	 * 
	 * @param id unit id to check
	 * @return unit exists
	 */
	public boolean unitExists(int id) {
		for (Unit unit : units) {
			if (unit.getId() == id) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set units new target location with the option to update
	 * server of movement
	 * 
	 * @param id unit id
	 * @param pt new location
	 * @param report inform server of update
	 */
	public void moveVehicle(int id, GeoPoint pt, boolean report) {
		for (Unit unit : units) {
			if (unit.getId() == id && unit.getType() == UnitType.VEHICLE) {
				if (report) {
					mainController.getInternetService().moveUnit(id, pt);
				} else {
					Vehicle vehicle = (Vehicle) unit;
					vehicle.setTargetLocation(pt);
				}
				break;
			}
		}
	}
	
	/**
	 * Update or create a unit
	 * 
	 * @param id unit id
	 * @param owner owner id
	 * @param type unit type
	 * @param current_pt current location
	 * @param target_pt target location
	 * @param health current health
	 */
	public void updateUnits(int id, int owner, String type, GeoPoint current_pt, GeoPoint target_pt, int health) {
		UnitType unitType = UnitType.valueOf(type);
		
		if (owner == mainController.getUser().getUserId())
			owner = 0;
		
 		if (unitExists(id)) {
			if (health > 0)
				getUnit(id).setHealth(health);
	 			if (unitType == UnitType.VEHICLE)
	 				moveVehicle(id, target_pt, false);
			else
				units.remove(getUnit(id));
 			
 		} else {
 			Log.i("UnitController", "Adding unit " + id + " [" + owner + "," + mainController.getUser().getUserId() + "] " + health);
 			Unit tmpUnit;
 			if (unitType == UnitType.VEHICLE) {
	 			tmpUnit = new Vehicle(id, owner, current_pt);
	 			tmpUnit.setHealth(health);
	 			addUnit(tmpUnit);
	 			moveVehicle(id, target_pt, false);
 			} else if (unitType == UnitType.STRUCTURE) {
 				tmpUnit = new Structure(id, owner, current_pt);
 				tmpUnit.setHealth(health);
 				addUnit(tmpUnit);
 			}
 		}
	}
	
	/**
	 * Parse and handle updates passed from the server
	 * 
	 * @param json updates
	 * @throws JSONException
	 */
	public void handleUpdates(JSONObject json) throws JSONException {
		String action = json.getString("action");
		if (action.equals("unit.attack")) {
			int health = json.getInt("health");
			int target = json.getInt("target");
			for (Unit unit : units) {
				if (unit.getId() == target) {
					if (health > 0)
						unit.setHealth(health);
					else
						units.remove(unit);
				}
			}
		} else {
			JSONArray units = json.getJSONArray("units");
			for (int i = 0 ; i < units.length(); i++){
				JSONObject unit = new JSONObject(units.getString(i));
				int id = unit.getInt("unitID");
				int owner = unit.getInt("userID");
				String type = unit.getString("type");
	
				//use current location
				JSONObject current_location = unit.getJSONObject("location");
				String current_lat = current_location.getString("lat");
				String current_lon = current_location.getString("lon");
				GeoPoint current_pt = Utils.createGeoPoint(Double.valueOf(current_lat), Double.valueOf(current_lon));
				
				//use current location
				JSONObject target_location = unit.getJSONObject("target");
				String target_lat = target_location.getString("lat");
				String target_lon = target_location.getString("lon");
				GeoPoint target_pt = Utils.createGeoPoint(Double.valueOf(target_lat), Double.valueOf(target_lon));
				
				int health = unit.getInt("health");
				
				//Add or update unit on overlay
				updateUnits(id, owner, type, current_pt, target_pt, health);
			}
		}
		
		mainController.redraw();
	}

	public void stopThread() {
		if (exec != null)
			exec.shutdown();
	}
	
	/* 
	 * Thread used to track unit location
	 */
	@Override
	public void run() {
		GeoPoint pt, tmpPt;
		Location loc, tmpLoc;
		
		/*
		 * Loop through each unit and see if it is moving
		 * if so calculate its new location so it is closer
		 * to its target location
		 */
		for (Unit unit : units) {
			if (unit.getType() == UnitType.VEHICLE) {
				Vehicle vehicle = (Vehicle) unit;
				pt = vehicle.getLocation();
				tmpPt = vehicle.getTargetLocation();
				
				loc = Utils.createLocation(pt);
				tmpLoc = Utils.createLocation(tmpPt);
				
				float distance = loc.distanceTo(tmpLoc);
				
				if (distance > 0) {
					vehicle.setBearing(loc.bearingTo(tmpLoc));

					int iStages = (int) Math.round(distance / 0.4);
	
			        int newLat = pt.getLatitudeE6() + ((tmpPt.getLatitudeE6() - pt.getLatitudeE6()) / iStages);
			        int newLon = pt.getLongitudeE6() + ((tmpPt.getLongitudeE6() - pt.getLongitudeE6()) / iStages);
			        
					pt.setLatitudeE6(newLat);
					pt.setLongitudeE6(newLon);
					vehicle.changeLocation(pt);
				}
			}
		}
		
		//Request map to be redrawn to show updated unit locations
		map.redraw();
	}

	public void toggleSelectMethod(boolean selectBox) {
		unitOverlay.toggleSelectMethod(selectBox);
	}
}