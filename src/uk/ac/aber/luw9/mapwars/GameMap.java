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

public class GameMap extends MapActivity {

	private GameMapController gameMapController;
    private MapView mapView;
    private MapController mapViewController;
    private LinearLayout shopView;
    private ImageView shopButton;
    
    private boolean loaded;
    private int loadedCount = 0;
    private TextView loadingText;
    private ArrayList<Overlay> tmpOverlays = new ArrayList<Overlay>();


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        loaded = false;
        
        loadingText = (TextView)findViewById(R.id.loadingText);
        
        gameMapController = new GameMapController(this);
     }
	
	public void serviceOnline(String service) {
		if (!loaded) {
			loadingText.append(service + "... ");
			
			loadedCount++;
			if (loadedCount == 2) {
				loaded = true;
				setupComplete();
			}
		}
	}
	
	 public void setupComplete() {
	 	setContentView(R.layout.map); 
	 
		mapView = (MapView) findViewById(R.id.map);
		mapView.setBuiltInZoomControls(false);
		
		XYTileSource tileSource = new XYTileSource("Meow", null, 12, 17, 256, ".png", "http://a.tiles.mapbox.com/v3/flabbyrabbit.map-wpji9msa/");
		//XYTileSource tileSource = new XYTileSource("v2", null, 12, 17, 256, ".png", "http://a.tiles.mapbox.com/v3/flabbyrabbit.map-dq73ewie/");
		
		mapView.setTileSource(tileSource);
		
	    mapView.setBuiltInZoomControls(false);
	    mapView.setMultiTouchControls(false);
	    
	    //load overlays
	    for (Overlay overlay : tmpOverlays) {
	    	addOverlay(overlay);
	    }

	    mapView.setMapListener(gameMapController);
	    
		mapViewController = mapView.getController();
		mapViewController.setZoom(17);
		
		Location loc = gameMapController.getUserLocation();
		mapViewController.setCenter(Utils.createGeoPoint(loc.getLatitude(), loc.getLongitude()));
		
		findViewById(R.id.zoomInButton).setOnClickListener(gameMapController);
		findViewById(R.id.zoomOutButton).setOnClickListener(gameMapController);
		findViewById(R.id.trackLocationButton).setOnClickListener(gameMapController);
		findViewById(R.id.unitBuyButton).setOnClickListener(gameMapController);
		findViewById(R.id.selectToggleButton).setOnClickListener(gameMapController);
		
		shopButton = (ImageView)findViewById(R.id.headerShopButton);
		if (shopButton != null) {
			shopButton.setOnClickListener(gameMapController);
		}
		
		shopView = (LinearLayout)findViewById(R.id.overlayShop);
	 }
 	
	 public void addOverlay(Overlay overlay) {
		 if (mapView != null) {
			 mapView.getOverlays().add(overlay);
		 } else {
			tmpOverlays.add(overlay);
		 }
	 }
	 
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
	        case R.id.menu_settings:
	            return true;
	        case R.id.menu_exit:
	        	finish();
	            return true;
        }
        
        return false;
    }

	public void zoomIn() {
		mapViewController.zoomIn();
	}
	
	public void zoomOut() {
		mapViewController.zoomOut();
	}
	
	public void setUserLocation(Location loc) {
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
}
