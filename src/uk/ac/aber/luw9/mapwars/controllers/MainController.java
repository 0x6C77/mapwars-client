package uk.ac.aber.luw9.mapwars.controllers;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.aber.luw9.mapwars.GameMap;
import uk.ac.aber.luw9.mapwars.HomeScreen;
import uk.ac.aber.luw9.mapwars.Utils;
import uk.ac.aber.luw9.mapwars.services.InternetService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.widget.Toast;

public class MainController {

	private static MainController mainController;
	private static Activity currentActivity;
	private static Context currentContext;
	private InternetService internetService;
	private GameMapController gameMapController;
	private HomeScreen homeScreen;
	private String user;

	public static MainController getController() {
		if (mainController == null) {
			mainController = new MainController();
			mainController.setup();
		}
		return mainController;
	}
	
	public static MainController getController(Activity activity) {
		setActivity(activity);
		mainController = getController();
		return mainController;
	}
	
	public void setup() {
		internetService = new InternetService(currentContext);
	}
	
	public static void setActivity(Activity activity) {
		currentActivity = activity;
		currentContext = activity.getApplicationContext();
		
		//Force orientation
 		if (Utils.isTablet(currentContext)) {
 			currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
        	currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
	}
	
	public void setGameMapController(GameMapController gameMapController) {
		this.gameMapController = gameMapController;
	}

	public void setHomeScreen(HomeScreen homeScreen) {
		this.homeScreen = homeScreen;
	}
	
	public void changePlaces(Class<GameMap> target) {
		Intent intent = new Intent(currentContext, target);
		
		currentActivity.startActivity(intent);
		currentActivity.finish();
	}
	
	/**
	 * Submits username and password for validation
	 * Response will be handled by handleTCPReply
	 * 
	 * @param user submitted username to be checked
	 * @param pass submitted password to be checked
	 */
	public boolean loginUser(String user, String pass) {
		return internetService.login(user, pass);
	}

	public void handleTCPReply(JSONObject json) throws JSONException {
		Log.i("tcp", json.toString());
		
		String action = json.getString("action");
		if (action.equals("login")) {
			int response = json.getInt("response");
			String user = json.getString("user");
			if (response == 1) {
				Toast.makeText(currentContext, "Correct", Toast.LENGTH_SHORT).show();
				
				this.user = user;
				internetService.setUser(user);
				
				changePlaces(GameMap.class);			
			} else {
				//change background of inputs
				homeScreen.invalidInput(1);
				
				Toast.makeText(currentContext, "Invalid", Toast.LENGTH_SHORT).show();
			}
		//} else if (action.equals("update") action.equals("update")) {
		} else {
			if (gameMapController != null)
				gameMapController.handleUpdates(json);
		}
	}

	public String getUser() {
		return user; 
	}
	
	//called by activity when application is exiting or switching to background
	public void stop() {
		internetService.stopThread();
	}

	public InternetService getInternetService() {
		return internetService;
	}

	public void redraw() {
		gameMapController.redraw();
	}
}
