package uk.ac.aber.luw9.mapwars.controllers;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.aber.luw9.mapwars.GameMap;
import uk.ac.aber.luw9.mapwars.HomeScreen;
import uk.ac.aber.luw9.mapwars.R;
import uk.ac.aber.luw9.mapwars.services.InternetService;
import uk.ac.aber.luw9.mapwars.services.LocationTracker;
import uk.ac.aber.luw9.mapwars.units.UnitType;
import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * @author flabbyrabbit
 */

public class GameMapController implements OnClickListener {

	private MainController mainController;
	private GameMap gameMap;
	private LocationTracker locationTracker;
	private InternetService internetService;
	private UnitController unitController;
	private boolean selectBox;

	public GameMapController(GameMap map) {
		this.gameMap = map;
		mainController = MainController.getController((Activity)map);
		mainController.setGameMapController(this);
		
		//Internet availability is a given, otherwise user will not have logged in
		this.internetService = mainController.getInternetService();
		if (internetService.startThread()) {
			serviceOnline("Internet");
		}
		
		this.locationTracker = new LocationTracker(map.getApplicationContext(), this);
		
		this.unitController = new UnitController(map);
	}
 
	/**
	 * Relay service message to gameMap
	 * @param service Service online
	 */
	public void serviceOnline(String service) {
		gameMap.serviceOnline(service);
	}

	/**
	 * Relay service message to gameMap and inform server
	 * that the users location has updated
	 * @param location Users current location
	 */
	public void updateUserLocation(Location location) {
		serviceOnline("Location");
		internetService.updateLocation(location);
	}
	
	/**
	 * Get users location from location tracker
	 * @return users location
	 */
	public Location getUserLocation() {
		return locationTracker.getLocation();
	}

	/**
	 * Pass unit updates to the unit controller
	 * @param json updates
	 * @throws JSONException
	 */
	public void handleUpdates(JSONObject json) throws JSONException {
		unitController.handleUpdates(json);
	}

	/**
	 * Stop mainController and unitController
	 */
	public void stop() {
		mainController.stop();
		unitController.stopThread();
	}
	
	@Override
	public void onClick(View v) {
		Location loc;
		switch(v.getId()) {
    		case R.id.zoomInButton:
    			gameMap.zoomIn();
    			break;
    		case R.id.zoomOutButton:
    			gameMap.zoomOut();
    			break;
    		case R.id.trackLocationButton:
    			//center map on users location
				loc = locationTracker.getLocation();
				gameMap.moveMap(loc);
    			break;
    		case R.id.headerShopButton:
    			gameMap.toggleShop();
    			break;
    		case R.id.vehicleBuyButton:
    			gameMap.toggleShop();
    			//create unit in users location
    			loc = locationTracker.getLocation();
    			Log.i("GameMapController", loc.toString());
    			internetService.createUnit(UnitType.VEHICLE, loc);
    			break;
    		case R.id.selectToggleButton:
    			selectBox = !selectBox;
    			gameMap.toggleSelectButton(selectBox);
    			unitController.toggleSelectMethod(selectBox);
    			break;
    		case R.id.mapToggleButton:
    			gameMap.toggleMap();
    			break;
		}
	}

	
	/**
	 * Request for the game map to be redrawn
	 */
	public void redraw() {
		gameMap.redraw();
	}
	
}
