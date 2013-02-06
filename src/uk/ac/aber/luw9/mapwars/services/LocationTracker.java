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
import android.provider.Settings;
import android.util.Log;

public class LocationTracker extends Service implements LocationListener {

	private Context context;
	private LocationManager locationManager;
	private boolean isGPSEnabled;
	private boolean isNetworkEnabled;
	private Location currentLocation;
	private GameMapController gameMapController;
	
	private static final long MIN_DISTANCE = 2; //2 meters
    private static final long MIN_TIME = 30; //30 seconds
    private static final int ONE_MINUTE = 1000 * 60;
	
	public LocationTracker(Context context, GameMapController gameMapController) {
		this.context = context;
		this.gameMapController = gameMapController;
		
		Log.i("Location", "Adding listeners");
		addListeners();
	}

	
	public void addListeners() {
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		Log.i("Location", locationManager.getProviders(true).toString());
		
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
		if (!isGPSEnabled && !isNetworkEnabled) {
			Log.i("Location", "No locations");
			Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(myIntent);
            // no network provider is enabled
        } else {
            // First get location from Network Provider
            //if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME,
                        MIN_DISTANCE, this);
                
                Log.i("Location", "Network enabled");
                if (locationManager != null) {
                	currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
           //}
            // if GPS Enabled get lat/long using GPS Services
            /*if (isGPSEnabled) {
                if (location == null) {*/
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME,
                            MIN_DISTANCE, this);
                    
                    Log.i("Location", "GPS enabled");
                    if (locationManager != null) {
                    	currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
              /*  }
            }*/
        }
		
		if (currentLocation != null)
			gameMapController.updateUserLocation(currentLocation);
	}
	
	
	public void onLocationChanged(Location location) {
		boolean isBetter = isBetterLocation(location, currentLocation);
		
		//compare accuracy against previous position
		Log.i("Location", "Location: " + location.toString());
		//Utils.appendLog("Location (" + isBetter + "): " + location.toString());
		
		if (isBetter && gameMapController != null) {
			currentLocation = location;
			gameMapController.updateUserLocation(currentLocation);
		}
	}

	public void onProviderDisabled(String provider) {
		Log.i("Location", "Disabled: " + provider);
	}

	public void onProviderEnabled(String provider) {
		Log.i("Location", "Enabled: " + provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
	
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
	    boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}
	
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
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
