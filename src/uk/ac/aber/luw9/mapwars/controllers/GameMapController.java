package uk.ac.aber.luw9.mapwars.controllers;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;

import uk.ac.aber.luw9.mapwars.GameMap;
import uk.ac.aber.luw9.mapwars.R;
import uk.ac.aber.luw9.mapwars.services.InternetService;
import uk.ac.aber.luw9.mapwars.services.LocationTracker;
import uk.ac.aber.luw9.mapwars.units.UnitType;
import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class GameMapController implements OnClickListener, MapListener {

	private static MainController mainController;
	private static GameMap gameMap;
	private LocationTracker locationTracker;
	private InternetService internetService;
	private UnitController unitController;
	private boolean trackUserLocation;

	public GameMapController(GameMap map) {
		this.gameMap = map;
		mainController = MainController.getController((Activity)map);
		mainController.setGameMapController(this);
		
		this.internetService = mainController.getInternetService();
		if (internetService.startThread()) {
			serviceOnline("Internet");
		}
		
		this.locationTracker = new LocationTracker(map.getApplicationContext(), this);
		
		this.unitController = new UnitController(map);
	}

	public void serviceOnline(String service) {
		gameMap.serviceOnline(service);
	}

	public void updateUserLocation(Location location) {
		serviceOnline("Location");
		
		if (trackUserLocation)
			gameMap.setUserLocation(location);
		
		internetService.updateLocation(location);
	}
	
	public Location getUserLocation() {
		return locationTracker.getLocation();
	}

	public void handleUpdates(JSONObject json) throws JSONException {
		unitController.handleUpdates(json);
	}

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
    			trackUserLocation = !trackUserLocation;
    			gameMap.toggleTrackButton(trackUserLocation);
    			if (trackUserLocation) {
    				loc = locationTracker.getLocation();
    				gameMap.setUserLocation(loc);
    			}
    			break;
    		case R.id.headerShopButton:
    			gameMap.toggleShop();
    			break;
    		case R.id.unitBuyButton:
    			gameMap.toggleShop();
    			//create unit
    			loc = locationTracker.getLocation();
    			internetService.createUnit(UnitType.USER, loc);
    			break;
		}
	}

	public void redraw() {
		gameMap.redraw();
	}

	@Override
	public boolean onScroll(ScrollEvent arg0) {
		Log.i("GameMapScroll", arg0.toString());
		trackUserLocation = false;
		gameMap.toggleTrackButton(trackUserLocation);
		
		return false;
	}

	@Override
	public boolean onZoom(ZoomEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
}