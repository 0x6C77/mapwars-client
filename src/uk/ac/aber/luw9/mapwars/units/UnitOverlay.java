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

public class UnitOverlay extends Overlay {
	private Bitmap overlay_tank_user, overlay_tank_user_selected, overlay_tank_enemy, overlay_defence_user, overlay_defence_enemy;
    private Bitmap tOverlay, uOverlay, eOverlay, usOverlay, dOverlay, deOverlay;
	private UnitController unitController;
	private ArrayList<Unit> unitsSelected = new ArrayList<Unit>();
	private int TAP_RADIUS = 20;
	private int UNIT_RADIUS = 25;
	private boolean unitSelecting;
	private Point unitSelectionStart = new Point(), unitSelectionEnd = new Point();
	private Boolean selectMethodBox = false;
	
	public UnitOverlay(UnitController controller, GameMap map) {
		super(map.getApplicationContext());
		this.unitController = controller;
		map.addOverlay(this);
		
		overlay_tank_user = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_1);
		overlay_tank_user_selected = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_1_selected);
		overlay_tank_enemy = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.tank_2);
		overlay_defence_user = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.defence_1);
		overlay_defence_enemy = BitmapFactory.decodeResource(map.getApplicationContext().getResources(), R.drawable.defence_2);
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
				unitController.moveVehicle(unit.getId(), tapPoint, true);
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
	
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if (selectMethodBox) {
			Log.i("UnitOverlayTouch",  event.toString());
			int tmpX = Math.round(event.getX());
			int tmpY = Math.round(event.getY());
			
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mapView.getProjection().fromMapPixels(tmpX, tmpY, unitSelectionStart);
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				mapView.getProjection().fromMapPixels(tmpX, tmpY, unitSelectionEnd);
				unitSelecting = true;
				mapView.invalidate();
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
       // super.draw(canvas, mapView, shadow);
		
		//Shadows are not used so skip this draw
		if (shadow)
			return;
		
        int radius = (int) mapView.getProjection().metersToEquatorPixels(UNIT_RADIUS);
        
        Point point = new Point();
        
		usOverlay = Bitmap.createScaledBitmap(overlay_tank_user_selected, radius, radius, false);
		uOverlay = Bitmap.createScaledBitmap(overlay_tank_user, radius, radius, false);
		eOverlay = Bitmap.createScaledBitmap(overlay_tank_enemy, radius, radius, false);
		dOverlay = Bitmap.createScaledBitmap(overlay_defence_user, radius, radius, false);
		deOverlay = Bitmap.createScaledBitmap(overlay_defence_enemy, radius, radius, false);

        ArrayList<Unit> units = unitController.getUnits(false, UnitType.VEHICLE);
	    for (Unit unit : units) {
	    	mapView.getProjection().toPixels(unit.getLocation(), point);
	    	if (unit.getType() == UnitType.VEHICLE) {
	    		if (unit.amOwner()) {
		        	if (unit.isSelected()) {
		        		tOverlay = usOverlay;
		        	} else {
		        		tOverlay = uOverlay;
		        	}
		    	} else {
		    		tOverlay = eOverlay;
		    	}
	    	} else if (unit.getType() == UnitType.DEFENCE) {
	    		if (unit.amOwner()) {
	    			tOverlay = dOverlay;
	    		} else {
    				tOverlay = deOverlay;	
    			}
	    	}


	        Matrix mat = new Matrix();
	        mat.setTranslate(point.x - (radius/2), point.y - (radius/2));
	        mat.postRotate(unit.getBearing(), point.x, point.y);
	        canvas.drawBitmap(tOverlay, mat, null);
        }

	    if (!units.isEmpty()) {
		    Unit unit = units.get(0);
		    
		    mapView.getProjection().toPixels(unit.getLocation(), point);
	    }
	    
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
