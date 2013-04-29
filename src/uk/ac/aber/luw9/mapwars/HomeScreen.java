package uk.ac.aber.luw9.mapwars;

import uk.ac.aber.luw9.mapwars.controllers.MainController;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Luke Ward
 */
public class HomeScreen extends Activity implements OnClickListener, OnKeyListener {

	MainController controller;
	
	private SharedPreferences prefs;
	private EditText login_user, login_pass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_screen);
		controller = MainController.getController(this);
		controller.setHomeScreen(this);
		
		Button login_button = (Button)findViewById(R.id.login_button);
        login_button.setOnClickListener(this);
        
        login_user = (EditText)findViewById(R.id.login_user);
        login_user.setOnKeyListener(this);
        login_pass = (EditText)findViewById(R.id.login_pass);
        login_pass.setOnKeyListener(this);

        //load stored details and inject them into UI
        prefs = this.getSharedPreferences("uk.ac.aber.luw9.mapwarsv2", Context.MODE_PRIVATE);

        String username = prefs.getString("username", "");
        String password = prefs.getString("password", "");        
        login_user.setText(username);
        login_pass.setText(password);
	}

	/**
	 * Marks input as invalid by adding red border to input group
	 * Group is based on the error code parameter
	 * 
	 * @param code which group of inputs are invalid
	 */
	public void invalidInput(int code) {
    	switch(code) {
    		case 1:
    	    	login_user.setBackgroundDrawable(getResources().getDrawable(R.drawable.input_red));
    	    	login_pass.setBackgroundDrawable(getResources().getDrawable(R.drawable.input_red));
    	    	break;
    	}
    }
	
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.login_button:
            	String username = login_user.getText().toString();
            	String password = login_pass.getText().toString();
            	
            	if (!username.isEmpty() && !password.isEmpty()) {
	            	//store users details 
	            	prefs
	            		.edit()
	            		.putString("username", username)
	            		.putString("password", password)
	            		.commit();
	            	
	            	// Attempt user login
	            	// Response is the ability to transmit request, not the response
	            	boolean status = controller.loginUser(username, password);
	            	if (!status) {
            		    AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            		    alertDialog.setTitle("No Internet Connection");
            		    alertDialog.setMessage("Internet not available, Please check your internet connectivity and try again");
            		    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            		       public void onClick(DialogInterface dialog, int which) {
            		         //finish();
            		       }
            		    });

            		    alertDialog.show();
	            	}
            	}
            	
                break;
        }
    }
    
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		switch(v.getId()) {
        	case R.id.login_user:
        	case R.id.login_pass:
            	login_user.setBackgroundDrawable(getResources().getDrawable(R.drawable.input_background));
            	login_pass.setBackgroundDrawable(getResources().getDrawable(R.drawable.input_background));
            	break;
		}
		
		return false;
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
