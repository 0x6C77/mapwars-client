package uk.ac.aber.luw9.mapwars.units;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import uk.ac.aber.luw9.mapwars.GameMap;
import uk.ac.aber.luw9.mapwars.R;
import uk.ac.aber.luw9.mapwars.controllers.UnitController;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;

public class UnitOverlay extends Overlay {
	private Bitmap overlay_user, overlay_user_selected, overlay_enemy;
    private Bitmap tOverlay, uOverlay, eOverlay, usOverlay;
	private UnitController unitController;
	private ArrayList<Unit> unitsSelected = new ArrayList<Unit>();
	private int TAP_RADIUS = 20;
	private int UNIT_RADIUS = 25;
	private boolean unitSelecting, unitSelectionDone;
	private float unitSelectionStartX, unitSelectionEndX, unitSelectionStartY, unitSelectionEndY;
	
	public UnitOverlay(UnitController controller, GameMap map) {
		super(map.getApplicationContext());
		this.unitController = controller;
		map.addOverlay(this);
		
		overlay_user = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_1);
		overlay_user_selected = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_1_selected);
		overlay_enemy = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_2);
	}
	
	public boolean onTap(float tapX, float tapY, MapView mapView) {
        GeoPoint tapPoint = (GeoPoint) mapView.getProjection().fromPixels(tapX, tapY);
        Location tapLocation = new Location("point B");
        tapLocation.setLatitude(tapPoint.getLatitudeE6() / 1E6);
        tapLocation.setLongitude(tapPoint.getLongitudeE6() / 1E6);
		
        //Track if a new unit has been selected this tap
        boolean newUnit = false;
       
		//look for unit
		ArrayList<Unit> units = unitController.getUnits(true);
		
	    for (Unit unit : units) {
	    	
	    	GeoPoint unitPoint = unit.getLocation();
 
	        Location unitLocation = new Location("point A");
	        unitLocation.setLatitude(unitPoint.getLatitudeE6() / 1E6);
	        unitLocation.setLongitude(unitPoint.getLongitudeE6() / 1E6);
	        
	        double distance = tapLocation.distanceTo(unitLocation);
	        
	        Log.i("UnitOverlayTap", String.valueOf(distance));
	        
	        if (distance < TAP_RADIUS) {
		    	//If the unit is already selected, unselect
		    	if (unit.isSelected()) {
			    	unit.unselect();
			    	unitsSelected.remove(unit);
		        } else {
		        	unit.select();
		        	unitsSelected.add(unit);
		        }
		    	
		    	newUnit = true;
	        }
		}

	    //if no new units have been selected then user must have clicked an empty area thus move units
	    if (!newUnit) {
	    	Log.i("UnitOverlayMove", "moving");
			//move selected units to location
			for (Unit unit : unitsSelected) {
				unit.unselect();
				unitController.moveUnit(unit.getId(), tapPoint, true);
			}
			unitsSelected.clear();
	    }

	    //force redraw
	    mapView.invalidate();
		return false;
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
		onTap(event.getX(0), event.getY(0), mapView);
		return false;
	}
	
/*	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		Log.i("UnitOverlayTouch",  event.toString());
		
		float tmpX = event.getX();
		float tmpY = event.getY();
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			unitSelecting = true;
			unitSelectionStartX = tmpX;
			unitSelectionStartY = tmpY;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			unitSelectionEndX = tmpX;
			unitSelectionEndY = tmpY;
			unitSelectionDone = true;
			mapView.invalidate();
		}
		
		return true;
	}
*/
	@Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
       // super.draw(canvas, mapView, shadow);
		
		//Shadows are not used so skip this draw
		if (shadow)
			return;
		
        int radius = (int) mapView.getProjection().metersToEquatorPixels(UNIT_RADIUS);
        
        Point point = new Point();
        
		usOverlay = Bitmap.createScaledBitmap(overlay_user_selected, radius, radius, false);
		uOverlay = Bitmap.createScaledBitmap(overlay_user, radius, radius, false);
		eOverlay = Bitmap.createScaledBitmap(overlay_enemy, radius, radius, false);

        ArrayList<Unit> units = unitController.getUnits();
	    for (Unit unit : units) {
	    	mapView.getProjection().toPixels(unit.getLocation(), point);
	    	if (unit.getType() == UnitType.USER) {
	        	if (unit.isSelected()) {
	        		tOverlay = usOverlay;
	        	} else {
	        		tOverlay = uOverlay;
	        	}
	    	} else {
	    		tOverlay = eOverlay;
	    	}
	        Log.i("UnitOverlay", "Drawing [" + point.x + "," + point.y + "]");
	        canvas.drawBitmap(tOverlay, point.x, point.y, null);
        }

	    if (!units.isEmpty()) {
		    Unit unit = units.get(0);
		    
		    mapView.getProjection().toPixels(unit.getLocation(), point);
		    Log.i("UnitOverlaySelect", point.x + " | " + unitSelectionStartX);
	    }
	    
//	  if (unitSelectionDone) {
//		Log.i("UnitOverlayDraw", String.valueOf(unitSelectionStartX));
//		Log.i("UnitOverlayDraw", String.valueOf(unitSelectionEndX));
//		 Paint   mPaint = new Paint();
//	        mPaint.setDither(true);
//	        mPaint.setColor(Color.RED);
//	        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//	        mPaint.setStrokeJoin(Paint.Join.ROUND);
//	        mPaint.setStrokeCap(Paint.Cap.ROUND);
//	        mPaint.setStrokeWidth(2);
//	        
//	        Path path = new Path();
//	        
//	        path.moveTo(unitSelectionStartX, unitSelectionStartY);
//	        path.lineTo(unitSelectionEndX, unitSelectionEndY);
//	}
    }
}
