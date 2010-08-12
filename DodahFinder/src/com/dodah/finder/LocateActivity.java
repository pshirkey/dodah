package com.dodah.finder;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

//Activity to locate a dodah without looking like a damn fool.
public class LocateActivity extends Activity {
	
	private long GPSUpdateRate = 100; // ms
	private LocationListener gpsListener;
	
	private static final String TAG = "Dodah Search";
	private Camera camera;
	private SearchView preview;
	private LocationManager locMan;
	
	Button buttonClick;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// camera preview
		setContentView(R.layout.main);

		// Compose the overlay target image onto the camera preview.
		TargetOverlayView targetViewOverlay = new TargetOverlayView(this);
		addContentView(targetViewOverlay, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		preview = new SearchView(this);
		((FrameLayout) findViewById(R.id.preview)).addView(preview);

		buttonClick = (Button) findViewById(R.id.buttonClick);
		buttonClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				preview.camera.takePicture(shutterCallback, rawCallback,
						jpegCallback);
			}
		});

		gpsListener = new MyLocationListener();
		 
		locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
     	locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);   

		Log.d(TAG, "onCreate'd");
	}
	
	// GPS listener
	private class MyLocationListener implements LocationListener{
		private Location curLocation;
		private boolean locationChanged = false;
		
		@Override
		public void onLocationChanged(Location location) {
			 if(curLocation == null)
	         {
	            curLocation = location;
	            locationChanged = true;
	         }
	         
	         if(curLocation.getLatitude() == location.getLatitude() &&
	               curLocation.getLongitude() == location.getLongitude())
	            locationChanged = false;
	         else
	            locationChanged = true;
	         
	         curLocation = location;	
	         //preview.gpsLocation = curLocation;
	     	
	 		if(locationChanged)
	 		{
	 			Toast.makeText(preview.getContext(), "Location changed : Lat: " + curLocation.getLatitude() + 
	                    " Long: " + curLocation.getLongitude(), Toast.LENGTH_LONG).show();	
	 		}
	 		else
	 		{
	 			
	 		}
	 		
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter");
		}
	};

	/** Handles data for raw picture */
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};

	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				// write to local sandbox file system
				// outStream =
				// CameraDemo.this.openFileOutput(String.format("%d.jpg",
				// System.currentTimeMillis()), 0);
				// Or write to sdcard
				outStream = new FileOutputStream(String.format(
						"/sdcard/%d.jpg", System.currentTimeMillis()));
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};
}    
       
      