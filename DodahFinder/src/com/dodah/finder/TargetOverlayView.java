package com.dodah.finder;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

// View of capture target for LocateActivity
public class TargetOverlayView extends View {
    private int captureTargetDensity = 150;
    private final int MAXDENSITY = 192;
    private final int MINDENSITY = 64;
    private boolean increasingDensity = true;
    
	public TargetOverlayView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	// Draw overlay target and framing
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.rgb(23, 240, 12));
		canvas.drawText("Test Text", 200, 200, paint);
		
		// Overlay with semi-transparent rouge
		canvas.drawColor(Color.argb(128, 230, 32, 32));
		
		// Setup animated capture graphic in image center.
		float cx = 150;
		float cy = 150;
		
		if(increasingDensity && captureTargetDensity < MAXDENSITY)
		{
			captureTargetDensity+=5;
		}
		else
		{
			increasingDensity = false;
		}
		
		if(!increasingDensity && captureTargetDensity > MINDENSITY)
		{
			captureTargetDensity-=5;
		}
		else
		{
			increasingDensity = true;
		}
		
		paint.setFilterBitmap(true);
		paint.setColor(Color.argb(captureTargetDensity,23,255,12));
		paint.setMaskFilter( new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
		canvas.drawCircle(cx, cy, 19, paint);
		
		super.onDraw(canvas);
	}
}
