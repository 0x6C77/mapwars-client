package uk.ac.aber.luw9.mapwars.units;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import uk.ac.aber.luw9.mapwars.GameMap;
import uk.ac.aber.luw9.mapwars.R;
import uk.ac.aber.luw9.mapwars.controllers.UnitController;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Map overlay used to display units
 * 
 * @author Luke Ward
 */
public class UnitOverlay extends Overlay {
	private Bitmap overlay_tank_user, overlay_tank_user_selected, overlay_tank_enemy, overlay_structure_user, overlay_structure_enemy;
    private Bitmap tOverlay, uOverlay, eOverlay, usOverlay, sOverlay, seOverlay;
	private UnitController unitController;
	private ArrayList<Unit> unitsSelected = new ArrayList<Unit>();
	private int TAP_RADIUS = 25;
	private int UNIT_RADIUS = 30;
	private boolean unitSelecting;
	private Point unitSelectionStart = new Point(), unitSelectionEnd = new Point();
	private Boolean selectMethodBox = false;
	
	public UnitOverlay(UnitController controller, GameMap map) {
		super(map.getApplicationContext());
		this.unitController = controller;
		
		//Add self to map overlays
		map.addOverlay(this);
		
		//Get resources to be used to represent each unit type
		overlay_tank_user = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_1);
		overlay_tank_user_selected = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_1_selected);
		overlay_tank_enemy = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_2);
		overlay_structure_user = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.structure_1);
		overlay_structure_enemy = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.structure_2);
	}
	
	/**
	 * Handle taps on overly
	 * 
	 * @param tapX tap coordinate
	 * @param tapY tap coordinate
	 * @param mapView relevant map
	 */
	public void onTap(float tapX, float tapY, MapView mapView) {
        GeoPoint tapPoint = (GeoPoint) mapView.getProjection().fromPixels(tapX, tapY);
        Location tapLocation = new Location("point B");
        tapLocation.setLatitude(tapPoint.getLatitudeE6() / 1E6);
        tapLocation.setLongitude(tapPoint.getLongitudeE6() / 1E6);
        
        //Track if a new unit has been selected this tap
        boolean newUnit = false;
       
		//look for units within tap range
		ArrayList<Unit> units = unitController.getUnits(true);
	    for (Unit unit : units) {	    	
	    	GeoPoint unitPoint = unit.getLocation();
 
	        Location unitLocation = new Location("point A");
	        unitLocation.setLatitude(unitPoint.getLatitudeE6() / 1E6);
	        unitLocation.setLongitude(unitPoint.getLongitudeE6() / 1E6);
	        
	        double distance = tapLocation.distanceTo(unitLocation);
	        Log.i("UnitOverlayTap", String.valueOf(distance));
	        
	        //If unit is within range of users tap
	        if (distance < TAP_RADIUS) {
		    	//If the unit is already selected, unselect
		    	if (unit.isSelected()) {
			    	unit.unselect();
			    	unitsSelected.remove(unit);
		        } else { //else select unit
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
				unitController.moveVehicle(unit.getId(), tapPoint, true);
			}
			unitsSelected.clear();
	    }

	    //force redraw
	    mapView.invalidate();
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
		onTap(event.getX(0), event.getY(0), mapView);
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		/*
		 * If in drag box selection mode track users movements as they
		 * tap drag and release
		 */
		if (selectMethodBox) {
			Log.i("UnitOverlayTouch",  event.toString());
			int tmpX = Math.round(event.getX());
			int tmpY = Math.round(event.getY());
			
			//Track users first tap and movements
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mapView.getProjection().fromMapPixels(tmpX, tmpY, unitSelectionStart);
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				mapView.getProjection().fromMapPixels(tmpX, tmpY, unitSelectionEnd);
				unitSelecting = true;
				mapView.invalidate();
				
			/*
			 * When user lifts finger select all units within boundary
			 */
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				unitSelecting = false;
				
				//look for units within this range
				ArrayList<Unit> units = unitController.getUnits(true);
				
			    for (Unit unit : units) {
			    	Point unitPoint = new Point();
			    	mapView.getProjection().toMapPixels(unit.getLocation(), unitPoint);
			    	
			        if (unitPoint.x > unitSelectionStart.x
			        		&& unitPoint.x < unitSelectionEnd.x
			        		&& unitPoint.y > unitSelectionStart.y
			        		&& unitPoint.y < unitSelectionEnd.y) {
			        	
						    	if (unit.isSelected()) {
							    	unit.unselect();
							    	unitsSelected.remove(unit);
						        } else {
						        	unit.select();
						        	unitsSelected.add(unit);
						        }
			        }
				}
			    
				mapView.invalidate();
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		//super.draw(canvas, mapView, shadow);
		
		//Shadows are not used so skip this draw
		if (shadow)
			return;
		
		//Convert unit radius to pixel size
        int radius = (int) mapView.getProjection().metersToEquatorPixels(UNIT_RADIUS);
        
        Point point = new Point();
        
        //Scale resources to the correct size for this zoom level
		usOverlay = Bitmap.createScaledBitmap(overlay_tank_user_selected, radius, radius, false);
		uOverlay = Bitmap.createScaledBitmap(overlay_tank_user, radius, radius, false);
		eOverlay = Bitmap.createScaledBitmap(overlay_tank_enemy, radius, radius, false);
		sOverlay = Bitmap.createScaledBitmap(overlay_structure_user, radius, radius, false);
		seOverlay = Bitmap.createScaledBitmap(overlay_structure_enemy, radius, radius, false);

		
		/*
		 * Loop units drawing them on the overlay
		 */
        ArrayList<Unit> units = unitController.getUnits();
	    for (Unit unit : units) {
	    	mapView.getProjection().toPixels(unit.getLocation(), point);
	    	if (unit.getType() == UnitType.VEHICLE) {
	    		if (unit.amOwner()) {
		        	if (unit.isSelected()) {
		        		tOverlay = usOverlay;
		        		
		        		//draw range indicator
		        		Paint paint = new Paint();
		        		paint.setARGB(128, 255, 255, 255);
		        		int rangeRadius = (int) mapView.getProjection().metersToEquatorPixels(100);
		        		canvas.drawCircle(point.x, point.y, rangeRadius, paint);
		        	} else {
		        		tOverlay = uOverlay;
		        	}
		    	} else {
		    		tOverlay = eOverlay;
		    	}
	    	} else if (unit.getType() == UnitType.STRUCTURE) {
	    		if (unit.amOwner()) {
	    			tOverlay = sOverlay;
	    		} else {
    				tOverlay = seOverlay;	
    			}
	    	}
	    	
	    	// check if unit is under attack, if so show health indicator
	    	if (unit.isSelected() || unit.isUnderAttack()) {
	    		int healthContWidth = (int) mapView.getProjection().metersToEquatorPixels(50);
	    		int healthHeight = (int) mapView.getProjection().metersToEquatorPixels(10);
	    		float healthPerc = (float) unit.getHealth() / 100;
	    		int healthWidth = Math.round(healthContWidth * healthPerc);
	    	
	    		Log.i("health", healthWidth + " " + unit.getHealth() + " " + healthPerc + " " + healthContWidth);
	    		
    			Paint paint = new Paint();
    			paint.setColor(Color.BLACK);

    			Paint redPaint = new Paint();
    			redPaint.setColor(Color.RED);

    			//draw health bar
    			canvas.drawRect(point.x-(healthContWidth/2), point.y-(healthHeight*2), point.x+(healthContWidth/2), point.y-(healthHeight*3), paint);
	    		canvas.drawRect(point.x-(healthContWidth/2)+2, point.y-(healthHeight*2)-2, point.x-(healthContWidth/2)+healthWidth-2, point.y-(healthHeight*3)+2, redPaint);
	    	}
	    	

	    	// Rotate resource to match bearing
	        Matrix mat = new Matrix();
	        mat.setTranslate(point.x - (radius/2), point.y - (radius/2));
	        mat.postRotate(unit.getBearing(), point.x, point.y);
	        
	        //draw resource on overlay
	        canvas.drawBitmap(tOverlay, mat, null);
        }

//	    if (!units.isEmpty()) {
//		    Unit unit = units.get(0);
//		    
//		    mapView.getProjection().toPixels(unit.getLocation(), point);
//	    }
	    
	    /*
	     * If user is selecting using a box, draw a dotted red line to highlight area being drawn
	     */
		if (unitSelecting) {
			Paint mPaint = new Paint();
			mPaint.setDither(true);
			mPaint.setColor(Color.RED);
			mPaint.setStyle(Paint.Style.STROKE);
			//Create dashed stroke
			mPaint.setPathEffect(new DashPathEffect(new float[] {10,10}, 0));
			mPaint.setStrokeWidth(2);
			
			canvas.drawRect(unitSelectionStart.x, unitSelectionStart.y, unitSelectionEnd.x, unitSelectionEnd.y, mPaint);
		}
    }

	public void toggleSelectMethod(boolean selectBox) {
		selectMethodBox = selectBox;
	}
}
