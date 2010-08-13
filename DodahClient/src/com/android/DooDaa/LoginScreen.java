package com.android.DooDaa;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class LoginScreen extends Activity {
	
    private EditText _UserName;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        _UserName = (EditText)findViewById(R.id.etUserName);
        int s = _UserName.getId();
        s++;
    }
}
