package uk.ac.aber.luw9.mapwars;

import java.util.ArrayList;

import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapActivity;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import uk.ac.aber.luw9.mapwars.controllers.GameMapController;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Main game activity, implements a rudimentary loading screen
 * 
 * @author Luke Ward (luw9)
 */
public class GameMap extends MapActivity {

	private GameMapController gameMapController;
    private MapView mapView;
    private MapController mapViewController;
    private LinearLayout shopView;
    private ImageView shopButton;
    
    private boolean loaded;
    private int loadedCount = 0;
    private TextView loadingText;
    private ArrayList<Overlay> overlays = new ArrayList<Overlay>();
    
    private boolean tileSourceSwitch = false;

	/**
	 * Display loading screen and create game controller, process
	 * will wait for all services to be loaded before continuing
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        loaded = false;
        
        loadingText = (TextView)findViewById(R.id.loadingText);
        
        gameMapController = new GameMapController(this);
        
        if (loaded)
        	setupComplete();
     }
	
	/**
	 * Loading screen waits until the specified number of services are online
	 * Once the count is reached the loading screen is hidden and the game map displayed
	 * 
	 * @param service Service that has been verified
	 */
	public void serviceOnline(String service) {
		if (!loaded) {
			loadingText.append(service + "... ");
			
			loadedCount++;
			if (loadedCount == 2) {
				loaded = true;
				if (gameMapController != null)
					setupComplete();
			}
		}
	}
	
	 /**
	 * After all services have been checked create the game map and controls
	 */
	private void setupComplete() {
	 	setContentView(R.layout.map); 
	 
	 	mapView = (MapView) findViewById(R.id.map);
		
		//set default tilesource to the light map tiles
		XYTileSource tileSource = new XYTileSource("light2", null, 12, 17, 256, ".png", "http://a.tiles.mapbox.com/v3/flabbyrabbit.map-dq73ewie/");
		mapView.setTileSource(tileSource);
		
		//hide default zoom controls as custom controls will be added
	    mapView.setBuiltInZoomControls(false);
	    //disable pinch to zoom controls
	    mapView.setMultiTouchControls(false);
	    
	    //add overlays to the game map
	    for (Overlay overlay : overlays) {
	    	addOverlay(overlay);
	    }
	    
		mapViewController = mapView.getController();
	    //set the starting zoom level
	    mapViewController.setZoom(17);
	    
		//fetch latest known location and center map
		Location loc = gameMapController.getUserLocation();
		mapViewController.setCenter(Utils.createGeoPoint(loc.getLatitude(), loc.getLongitude()));
		
		//add event listeners to all buttons
		findViewById(R.id.zoomInButton).setOnClickListener(gameMapController);
		findViewById(R.id.zoomOutButton).setOnClickListener(gameMapController);
		findViewById(R.id.trackLocationButton).setOnClickListener(gameMapController);
		findViewById(R.id.vehicleBuyButton).setOnClickListener(gameMapController);
		findViewById(R.id.selectToggleButton).setOnClickListener(gameMapController);
		findViewById(R.id.mapToggleButton).setOnClickListener(gameMapController);
		
		//check if certain button exists before attempting to add listener
		//button is not utilized on tablet layout
		shopButton = (ImageView)findViewById(R.id.headerShopButton);
		if (shopButton != null) {
			shopButton.setOnClickListener(gameMapController);
			shopView = (LinearLayout)findViewById(R.id.overlayShop);
		}
	}
 	
	
	/**
	 * Add overlay to map
	 * can be done before map is ready
	 * 
	 * @param overlay Overlay to be added
	 */
	public void addOverlay(Overlay overlay) {
		/*
		 * check if map is ready
		 * if so add directly to map
		 * else store in array for later use
		 */
		if (mapView != null) {
			mapView.getOverlays().add(overlay);
		} else {
			overlays.add(overlay);
		}
	}
	
	/**
	 * Invalidate map to force redraw 
	 */
	public void redraw() {
		 if (mapView != null)
			 mapView.postInvalidate();
	}
    
	@Override
    protected void onStop() {
		gameMapController.stop();
		super.onStop();
    }
	
	@Override
	protected void onResume() {
		Log.i("TCP", "Resuming");
		//controller.resume();
		super.onResume();
	}
	
	
	/* Menu creation and handerling */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.map_menu, menu);
        return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.menu_exit:
	        	finish();
	            return true;
        }
        
        return false;
    }

	/**
	 * Zoom map view in one level
	 */
	public void zoomIn() {
		mapViewController.zoomIn();
	}

	/**
	 * Zoom map view out one level
	 */	
	public void zoomOut() {
		mapViewController.zoomOut();
	}

	/**
	 * Move map to center on location
	 */	
	public void moveMap(Location loc) {
		GeoPoint pt = Utils.createGeoPoint(loc.getLatitude(), loc.getLongitude());
		
		if (loaded) {
			mapViewController.animateTo(pt);
		}
	}

	public void toggleShop() {
		if (shopView == null) {
			return;
		}
		
		if (shopView.getVisibility() == View.VISIBLE) {
			shopView.setVisibility(View.INVISIBLE);
		} else {
			shopView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onLocationLost() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location pLoc) {
		// TODO Auto-generated method stub
		
	}
	
	public void toggleSelectButton(boolean boxSelection) {
		ImageView selectBoxButton = (ImageView)findViewById(R.id.selectBox);
		ImageView selectHandButton = (ImageView)findViewById(R.id.selectHand);
		if (boxSelection) {
			selectBoxButton.setImageResource(R.drawable.box_green);
			selectBoxButton.setBackgroundColor(0xFF000000);
			selectHandButton.setImageResource(R.drawable.hand_white);
			selectHandButton.setBackgroundColor(0x00000000);
		} else {
			selectBoxButton.setImageResource(R.drawable.box_white);
			selectBoxButton.setBackgroundColor(0x00000000);
			selectHandButton.setImageResource(R.drawable.hand_green);
			selectHandButton.setBackgroundColor(0xFF000000);
		}
	}
	
	public void showLocationSettings() {
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}
	
	public void toggleMap() {
		XYTileSource tileSource;
		tileSourceSwitch = !tileSourceSwitch;
		if (tileSourceSwitch)
			tileSource = new XYTileSource("dark2", null, 12, 17, 256, ".png", "http://a.tiles.mapbox.com/v3/flabbyrabbit.map-wpji9msa/");
		else
			tileSource = new XYTileSource("light2", null, 12, 17, 256, ".png", "http://a.tiles.mapbox.com/v3/flabbyrabbit.map-dq73ewie/");
		mapView.setTileSource(tileSource);
	}
}
