package uk.ac.aber.luw9.mapwars.controllers;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.aber.luw9.mapwars.GameMap;
import uk.ac.aber.luw9.mapwars.HomeScreen;
import uk.ac.aber.luw9.mapwars.User;
import uk.ac.aber.luw9.mapwars.Utils;
import uk.ac.aber.luw9.mapwars.services.InternetService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.widget.Toast;

public class MainController {

	//Private constructor for singleton
	private static MainController mainController;
	private static Activity currentActivity;
	private static Context currentContext;
	private static InternetService internetService;
	private GameMapController gameMapController;
	private HomeScreen homeScreen;
	private User user;

	public static MainController getController() {
		if (mainController == null) {
			mainController = new MainController();
			internetService = new InternetService(currentContext);
		}
		return mainController;
	}
	
	public static MainController getController(Activity activity) {
		setActivity(activity);
		mainController = getController();
		return mainController;
	}
	
	/**
	 * Update new main activity, to be called on all activities
	 * 
	 * @param activity current activity
	 */
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
		setActivity(homeScreen);
	}
	
	
	/**
	 * Change activity
	 * @param target new activity
	 */
	public void changePlaces(Class target) {
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
	 * @return if the request was submitted correctly
	 */
	public boolean loginUser(String user, String pass) {
		return internetService.login(user, pass);
	}

	/**
	 * Handle response from server
	 * 
	 * @param json server response
	 * @throws JSONException
	 */
	public void handleTCPReply(JSONObject json) throws JSONException {		
		String action = json.getString("action");
		if (action.equals("user.login")) {
			int status = json.getInt("status");
			// as request successful 
			if (status == 1) {
				Toast.makeText(currentContext, "Correct", Toast.LENGTH_SHORT).show();
				
				int user_id = json.getInt("userID");
				String sess = json.getString("sess");
				user = new User(user_id);
				user.setSession(sess);

				internetService.setUser(user);
				
				changePlaces(GameMap.class);			
			} else {
				//change background of inputs to show incorrect value
				homeScreen.invalidInput(1);
				Toast.makeText(currentContext, "Invalid", Toast.LENGTH_SHORT).show();
			}
		} else {
			//forward all other updates to the gameMapController if it is available
			if (gameMapController != null)
				gameMapController.handleUpdates(json);
		}
	}

	public User getUser() {
		return user; 
	}
	
	//called by activity when application is exiting or switching to background
	public void stop() {
		internetService.stopThread();
	}

	public InternetService getInternetService() {
		return internetService;
	}

	/**
	 * Request redraw of game map 
	 */
	public void redraw() {
		gameMapController.redraw();
	}
}
