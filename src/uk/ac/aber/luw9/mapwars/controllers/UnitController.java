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
import uk.ac.aber.luw9.mapwars.units.Unit;
import uk.ac.aber.luw9.mapwars.units.UnitOverlay;
import uk.ac.aber.luw9.mapwars.units.UnitType;
import uk.ac.aber.luw9.mapwars.units.Vehicle;
import uk.ac.aber.luw9.mapwars.units.VehicleType;
import android.location.Location;
import android.util.Log;

public class UnitController implements Runnable {
	private ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
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
		return getUnits(false, null);
	}
	
	public ArrayList<Unit> getUnits(boolean user, UnitType type) {
		//if (type == UnitType.VEHICLE) {
			if (!user) {
				ArrayList<Unit> usersVehicles = new ArrayList<Unit>();
				for (Vehicle vehicle : vehicles) {
					usersVehicles.add(vehicle);
				}	
				return usersVehicles;
			} else {
				//find users units
				ArrayList<Unit> usersVehicles = new ArrayList<Unit>();
				for (Vehicle vehicle : vehicles) {
					if (vehicle.getOwner().equals(mainController.getUser())) {
						usersVehicles.add(vehicle);
					}
				}		
				return usersVehicles;
			}
		//}
		
		//return null;
	}
	
	public void addUnit(Unit unit, UnitType type) {
		if (type == UnitType.VEHICLE) {
			vehicles.add((Vehicle) unit);
			
			if (!threadRunning) {
				threadRunning = true;
				exec = Executors.newSingleThreadScheduledExecutor();
				exec.scheduleAtFixedRate(this, 0, 20, TimeUnit.MILLISECONDS);
			}
		}
	}
	
	public boolean unitExists(String id) {
		for (Unit unit : vehicles) {
			if (unit.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	public void moveVehicle(String id, GeoPoint pt, boolean report) {
		for (Vehicle vehicle : vehicles) {
			if (vehicle.getId().equals(id)) {
				vehicle.setTargetLocation(pt);
				if (report) {
					mainController.getInternetService().moveUnit(vehicle.getId(), pt);
				}
				break;
			}
		}
	}
	
	public void updateVehicleLocation(String id, String owner, GeoPoint pt) {
 		if (unitExists(id)) {
 			//only update other users locations, our local copy is probably more up to date
			moveVehicle(id, pt, false);
 		} else {
 			Log.i("UnitController", "Adding unit " + id + " [" + owner + "," + mainController.getUser() + "]");
 			Unit tmpUnit;
 			if (owner.equals(mainController.getUser())) {
 				tmpUnit = new Vehicle(id, owner, VehicleType.USER, pt);
 			} else {
 				tmpUnit = new Vehicle(id, owner, VehicleType.ENEMY, pt);
 			}
 			addUnit(tmpUnit, UnitType.VEHICLE);
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
			
			GeoPoint pt = Utils.createGeoPoint(Double.valueOf(lat), Double.valueOf(lon));
			
			updateVehicleLocation(id, owner, pt);
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
		
		for (Vehicle vehicle : vehicles) {
			pt = vehicle.getLocation();
			tmpPt = vehicle.getTargetLocation();
			
			loc = Utils.createLocation(pt);
			tmpLoc = Utils.createLocation(tmpPt);
			
			float distance = loc.distanceTo(tmpLoc);
			
			if (distance > 0) {
				Log.i("UnitControllerMove", String.valueOf(distance));
				vehicle.setBearing(loc.bearingTo(tmpLoc));
				
				int iStages = (int) Math.round(distance / 0.4);

		        int newLat = pt.getLatitudeE6() + ((tmpPt.getLatitudeE6() - pt.getLatitudeE6()) / iStages);
		        int newLon = pt.getLongitudeE6() + ((tmpPt.getLongitudeE6() - pt.getLongitudeE6()) / iStages);
		        
				pt.setLatitudeE6(newLat);
				pt.setLongitudeE6(newLon);
				vehicle.setLocation(pt);
				
				unitsMoved = true;
			}
		}
		
		if (unitsMoved) {
			Log.i("UnitController", "Something moved");
			map.redraw();
		}
	}

	public void toggleSelectMethod(boolean selectBox) {
		unitOverlay.toggleSelectMethod(selectBox);
	}
}