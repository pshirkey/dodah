package com.android.dodah;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity implements OnClickListener {
	
	public void onClick(View v) {
	      // do something when the button is clicked
	   
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button button = (Button)findViewById(R.id.loginButton);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(this);
        
        
    }
}