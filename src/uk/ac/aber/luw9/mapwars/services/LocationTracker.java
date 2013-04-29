package uk.ac.aber.luw9.mapwars.services;

import uk.ac.aber.luw9.mapwars.controllers.GameMapController;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Handle location updates and store record of last known position
 * 
 * @author Luke Ward
 */
public class LocationTracker extends Service implements LocationListener {

	private Context context;
	private LocationManager locationManager;
	private boolean isGPSEnabled;
	private boolean isNetworkEnabled;
	private Location currentLocation;
	private GameMapController gameMapController;
	
	private static final long MIN_DISTANCE = 2; //2 meters between location updates
    private static final long MIN_TIME = 30; //30 seconds between location updates
    private static final int ONE_MINUTE = 1000 * 60; // time deemed to be reasonable
	
	public LocationTracker(Context context, GameMapController gameMapController) {
		this.context = context;
		this.gameMapController = gameMapController;
		
		Log.i("Location", "Adding listeners");
		addListeners();
	}

	
	/**
	 * Add all location listeners
	 */
	public void addListeners() {
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		Log.i("Location", locationManager.getProviders(true).toString());
		
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
		if (!isGPSEnabled && !isNetworkEnabled) {
			Log.i("Location", "No locations");
        } else {
			locationManager.requestLocationUpdates(
			        LocationManager.NETWORK_PROVIDER,
			        MIN_TIME,
			        MIN_DISTANCE, this);
			
			locationManager.requestLocationUpdates(
			        LocationManager.GPS_PROVIDER,
			        MIN_TIME,
			        MIN_DISTANCE, this);
        }
		
		currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location tmpLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		if (tmpLocation != null && isBetterLocation(tmpLocation, currentLocation))
			currentLocation = tmpLocation;
		
		if (currentLocation != null) {
			Log.i("Location", currentLocation.toString());
			gameMapController.updateUserLocation(currentLocation);
		}
	}
	
	
	public void onLocationChanged(Location location) {
		boolean isBetter = isBetterLocation(location, currentLocation);
		Log.i("Location", "Location: " + location.toString());
		
		if (isBetter) {
			currentLocation = location;
			gameMapController.updateUserLocation(currentLocation);
		}
	}

	public void onProviderDisabled(String provider) {
		//remove listener
	}

	public void onProviderEnabled(String provider) {
		//add listener
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		//check status
	}
	
	/**
	 * Determine if location is better than the one already known
	 * taken from http://developer.android.com/guide/topics/location/strategies.html
	 * 
	 * @param location
	 * @param currentBestLocation
	 * @return
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		// If no location is known then new location is to be accepted
	    if (currentBestLocation == null) {
	        return true;
	    }

	    // Check whether the new location is newer or older and within a reasonable time frame
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
	    boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
	    boolean isNewer = timeDelta > 0;

	    // If the new location is considerably newer then it is probably better
	    if (isSignificantlyNewer) {
	        return true;
	     // If the new location is considerably older then it is probably better
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // If new location is more accurate or newer and not 
	    // considerably less accurate accept new location
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate) {
	        return true;
	    }
	    
	    // Else reject new location
	    return false;
	}
	
	public Location getLocation() {
		return currentLocation;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
