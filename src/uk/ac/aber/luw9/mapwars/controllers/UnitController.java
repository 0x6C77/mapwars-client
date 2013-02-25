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
import uk.ac.aber.luw9.mapwars.units.Defence;
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
	
	public ArrayList<Unit> getUnits() {
		return getUnits(false);
	}
	
	public ArrayList<Unit> getUnits(boolean user) {
		return getUnits(user, null);
	}
	
	public ArrayList<Unit> getUnits(boolean user, UnitType type) {
		ArrayList<Unit> response = new ArrayList<Unit>();
		//if (type == UnitType.VEHICLE) {
			if (!user) {
				for (Unit unit : units) {
					response.add(unit);
				}	
				return response;
			} else {
				//find users units
				for (Unit unit : units) {
					if (unit.amOwner()) {
						response.add(unit);
					}
				}		
				return response;
			}
		//}
		
		//return null;
	}
	
	public void addUnit(Unit unit) {
		units.add(unit);
		
		if (!threadRunning) {
			threadRunning = true;
			exec = Executors.newSingleThreadScheduledExecutor();
			exec.scheduleAtFixedRate(this, 0, 20, TimeUnit.MILLISECONDS);
		}
	}
	
	public boolean unitExists(String id) {
		for (Unit unit : units) {
			if (unit.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	public void moveVehicle(String id, GeoPoint pt, boolean report) {
		for (Unit unit : units) {
			if (unit.getId().equals(id) && unit.getType() == UnitType.VEHICLE) {
				Vehicle vehicle = (Vehicle) unit;
				vehicle.setTargetLocation(pt);
				if (report) {
					mainController.getInternetService().moveUnit(id, pt);
				}
				break;
			}
		}
	}
	
	public void updateUnits(String id, String owner, GeoPoint pt, String type) {
		UnitType unitType = UnitType.valueOf(type);
		
		if (owner.equals(mainController.getUser()))
			owner = null;
		
 		if (unitExists(id)) {
 			if (unitType == UnitType.VEHICLE)
 				moveVehicle(id, pt, false);
 		} else {
 			Log.i("UnitController", "Adding unit " + id + " [" + owner + "," + mainController.getUser() + "]");
 			Unit tmpUnit;
 			if (unitType == UnitType.VEHICLE) {
	 			tmpUnit = new Vehicle(id, owner, pt);
	 			addUnit(tmpUnit);
 			} else if (unitType == UnitType.DEFENCE) {
 				tmpUnit = new Defence(id, owner, pt);
 				addUnit(tmpUnit);
 			}
 		}
	}
	
	public void handleUpdates(JSONObject json) throws JSONException {
		//get each user listed and pass them to GameMapController
		//JSONObject units = json.getJSONObject("units");
		//JSONArray vehicles = units.getJSONArray("vehicles");
		JSONArray units = json.getJSONArray("units");
		for (int i = 0 ; i < units.length(); i++){
			Log.i("UnitControllerUpdate", units.getString(i));
			JSONObject unit = new JSONObject(units.getString(i));
			String id = unit.getString("id");
			String owner = unit.getString("user");
			String lat = unit.getString("lat");
			String lon = unit.getString("lon");
			String type = unit.getString("type");
			
			GeoPoint pt = Utils.createGeoPoint(Double.valueOf(lat), Double.valueOf(lon));
			
			updateUnits(id, owner, pt, type);
		}
		
		mainController.redraw();
	}

	public void stopThread() {
		if (exec != null)
			exec.shutdown();
	}
	
	@Override
	public void run() {
		GeoPoint pt, tmpPt;
		Location loc, tmpLoc;
		boolean unitsMoved = false;
		
		for (Unit unit : units) {
			if (unit.getType() == UnitType.VEHICLE) {
				Vehicle vehicle = (Vehicle) unit;
				pt = vehicle.getLocation();
				tmpPt = vehicle.getTargetLocation();
				
				loc = Utils.createLocation(pt);
				tmpLoc = Utils.createLocation(tmpPt);
				
				float distance = loc.distanceTo(tmpLoc);
				
				if (distance > 0) {
					//Log.i("UnitControllerMove", String.valueOf(distance));
					vehicle.setBearing(loc.bearingTo(tmpLoc));
					
					int iStages = (int) Math.round(distance / 0.4);
	
			        int newLat = pt.getLatitudeE6() + ((tmpPt.getLatitudeE6() - pt.getLatitudeE6()) / iStages);
			        int newLon = pt.getLongitudeE6() + ((tmpPt.getLongitudeE6() - pt.getLongitudeE6()) / iStages);
			        
					pt.setLatitudeE6(newLat);
					pt.setLongitudeE6(newLon);
					vehicle.changeLocation(pt);
					
					unitsMoved = true;
				}
			} else if (unit.getType() == UnitType.DEFENCE) {
				//seek the closest enemy unit
				float closestDistance = 100;
				for (Unit unit2 : units) {
					if (unit2.getOwner() != unit.getOwner()) {
						pt = unit.getLocation();
						tmpPt = unit2.getLocation();
						
						loc = Utils.createLocation(pt);
						tmpLoc = Utils.createLocation(tmpPt);
						
						float distance = loc.distanceTo(tmpLoc);
						
						if (distance < closestDistance) {
							closestDistance = distance;
							unit.setBearing(loc.bearingTo(tmpLoc) - 90);
						}
					}
				}
				unitsMoved = true;
			}
		}
		
		if (unitsMoved) {
			//Log.i("UnitController", "Something moved");
			map.redraw();
		}
	}

	public void toggleSelectMethod(boolean selectBox) {
		unitOverlay.toggleSelectMethod(selectBox);
	}
}