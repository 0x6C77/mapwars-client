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
		if (!user) {
			return units;
		} else {
			//find users units
			ArrayList<Unit> usersUnits = new ArrayList<Unit>();
			for (Unit unit : units) {
				if (unit.getOwner().equals(mainController.getUser())) {
					usersUnits.add(unit);
				}
			}		
			return usersUnits;
		}
	}
	
	public boolean addUnit(Unit unit) {
		units.add(unit);
		
		if (!threadRunning) {
			threadRunning = true;
			exec = Executors.newSingleThreadScheduledExecutor();
			exec.scheduleAtFixedRate(this, 0, 20, TimeUnit.MILLISECONDS);
		}
		
		return true;
	}
	
	public boolean unitExists(String id) {
		for (Unit unit : units) {
			if (unit.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	public void moveUnit(String id, GeoPoint pt, boolean report) {
		for (Unit unit : units) {
			if (unit.getId().equals(id)) {
				unit.setTargetLocation(pt);
				if (report) {
					mainController.getInternetService().moveUnit(unit.getId(), pt);
				}
				break;
			}
		}
	}
	
	public void updateUnitLocation(String id, String owner, GeoPoint pt) {
 		if (unitExists(id)) {
 			//only update other users locations, our local copy is probably more up to date
 			if (!owner.equals(mainController.getUser())) {
 				moveUnit(id, pt, false);
 			}
 		} else {
 			Log.i("UnitController", "Adding unit " + id + " [" + owner + "," + mainController.getUser() + "]");
 			Unit tmpUnit;
 			if (owner.equals(mainController.getUser())) {
 				tmpUnit = new Unit(id, owner, UnitType.USER, pt);
 			} else {
 				tmpUnit = new Unit(id, owner, UnitType.ENEMY, pt);
 			}
 			addUnit(tmpUnit);
 		}
	}
	
	public void handleUpdates(JSONObject json) throws JSONException {
		//get each user listed and pass them to GameMapController
		JSONArray units = json.getJSONArray("units");
		for (int i = 0 ; i < units.length(); i++){
			Log.i("UnitController", units.getString(i));
			JSONObject unit = new JSONObject(units.getString(i));
			String id = unit.getString("id");
			String owner = unit.getString("user");
			String lat = unit.getString("lat");
			String lon = unit.getString("lon");
			
			GeoPoint pt = Utils.createGeoPoint(Double.valueOf(lat), Double.valueOf(lon));
			
			updateUnitLocation(id, owner, pt);
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
			pt = unit.getLocation();
			tmpPt = unit.getTargetLocation();
			
			loc = Utils.createLocation(pt);
			tmpLoc = Utils.createLocation(tmpPt);
			
			float distance = loc.distanceTo(tmpLoc);
			
			unit.setBearing(loc.bearingTo(tmpLoc));
			
			if (distance > 0) {
				int iStages = (int) Math.round(distance / 0.4);

		        int newLat = pt.getLatitudeE6() + ((tmpPt.getLatitudeE6() - pt.getLatitudeE6()) / iStages);
		        int newLon = pt.getLongitudeE6() + ((tmpPt.getLongitudeE6() - pt.getLongitudeE6()) / iStages);
		        
				pt.setLatitudeE6(newLat);
				pt.setLongitudeE6(newLon);
				unit.setLocation(pt);
				
				unitsMoved = true;
			}
		}
		
		if (unitsMoved) {
			map.redraw();
		}
	}
}