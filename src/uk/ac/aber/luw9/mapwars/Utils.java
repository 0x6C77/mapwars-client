package uk.ac.aber.luw9.mapwars;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.location.Location;
import android.util.DisplayMetrics;

public class Utils {
	
	// taken from http://stackoverflow.com/questions/1756296/android-writing-logs-to-text-file
	public static void appendLog(String text)
	{       
	   File logFile = new File("sdcard/mapwars.log");
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
	
    /**
     * Checks if the screen size is equal or above given length
     * @param activity activity screen
     * @param screen_size diagonal size of screen, for example 7.0 inches
     * @return True if its equal or above, else false
     * 
     * taken from http://stackoverflow.com/questions/10080451/android-detect-small-tablet-vs-big-phone
     */
	public static boolean isTablet(Context context) {
	    try {
	        // Compute screen size
	        DisplayMetrics dm = context.getResources().getDisplayMetrics();
	        
	        float screenWidth  = dm.widthPixels / dm.xdpi;
	        float screenHeight = dm.heightPixels / dm.ydpi;
	        double size = Math.sqrt(Math.pow(screenWidth, 2) +
	                                Math.pow(screenHeight, 2));
	        // Tablet devices should have a screen size greater than 6 inches
	        return size >= 6;
	    } catch(Throwable t) {
	        return false;
	    }
	} 
	
	public static GeoPoint createGeoPoint(double lat, double lon) {
		return new GeoPoint((int)(lat * 1E6), (int)(lon * 1E6));
	}
	
	public static Location createLocation(GeoPoint pt) {
		Location location = new Location("");
		double latitude = pt.getLatitudeE6() / 1E6;
		double longitude = pt.getLongitudeE6() / 1E6;

		location.setLatitude(latitude);
		location.setLongitude(longitude);
		
		return location;
	}
}
