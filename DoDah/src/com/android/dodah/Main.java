package com.android.dodah;
 
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.content.DialogInterface;
 
/**
 *  Application Name: Generic Login Screen for the Android Platform (back end)
 *  Description: This is a generic login screen which catches the username and password values
 *  Created on: November 23, 2007
 *  Created by: Pogz Ortile
 *  Contact: pogz(at)redhat(dot)polarhome(dot)com
 *  Notes: The string values for username and password are assigned to sUserName and sPassword respectively
 *              You are free to distribute, modify, and wreck for all I care. GPL ya!
 * */
 
public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override   
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
       
        // load up the layout
        setContentView(R.layout.main);
       
        // get the button resource in the xml file and assign it to a local variable of type Button
        Button launch = (Button)findViewById(R.id.login_button);
       
        // this is the action listener
        launch.setOnClickListener( new OnClickListener()
        {
               
                public void onClick(View viewParam)
                {
                        // this gets the resources in the xml file and assigns it to a local variable of type EditText
                EditText usernameEditText = (EditText) findViewById(R.id.txt_username);
                EditText passwordEditText = (EditText) findViewById(R.id.txt_password);
               
                // the getText() gets the current value of the text box
                // the toString() converts the value to String data type
                // then assigns it to a variable of type String
                String sUserName = usernameEditText.getText().toString();
                String sPassword = passwordEditText.getText().toString();
                       
                // this just catches the error if the program cant locate the GUI stuff
                if(usernameEditText == null || passwordEditText == null){
                    showAlert("Crap!", "Couldn't find the 'txt_username' or 'txt_password' "
                              + "EditView in main.xml");
                }else{
                        // display the username and the password in string format
                        showAlert("Logging in", "Username: " + sUserName + "nPassword: " + sPassword);
                        }
                }
        }
       
        ); // end of launch.setOnclickListener
    }
    
   
    
    public void showAlert( String title, String message )
    {
        new AlertDialog.Builder(this)
          .setMessage(message)
          .setTitle(title)
          .setPositiveButton("ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}})
          .setCancelable(true)
          .setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            	// TODO Auto-generated method stub
            }})
          .show();
    }
    
}