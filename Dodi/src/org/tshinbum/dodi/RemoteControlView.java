package org.tshinbum.dodi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class RemoteControlView extends View {

	private Paint black;
	private Paint blue;
	private Paint white;
	private Paint red;
	private Paint green;
	
	private Paint bigBlue;
	private Paint bigBlack;
	private Paint bigRed;
	

	private float speed = 0;
	private float direc = 0;
	private float left;
	private float right;
	private int inSSC;
	private int inSpeed;
	private int inDirection;
	private int inExtraCmd;


	public RemoteControlView(Context context) {
		super(context);
		init();
	}

	public RemoteControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RemoteControlView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		black = new Paint();
		black.setAntiAlias(true);
		black.setStrokeWidth(2);
		black.setTextSize(25);
		black.setStyle(Paint.Style.STROKE);
		black.setColor(Color.BLACK);
		//
		white = new Paint();
		white.setAntiAlias(true);
		white.setStrokeWidth(2);
		white.setTextSize(25);
		white.setStyle(Paint.Style.FILL_AND_STROKE);
		white.setColor(Color.WHITE);

		red = new Paint(white);
		red.setColor(Color.RED);
		
		green = new Paint(white);
		green.setColor(Color.GREEN);
		
		blue = new Paint(white);
		blue.setColor(Color.BLUE);

		bigBlack = new Paint(black);
		bigBlack.setTextSize(50);
		bigBlack.setTypeface(Typeface.MONOSPACE);
		bigBlack.setStyle(Paint.Style.FILL_AND_STROKE);
		
		bigBlue = new Paint(bigBlack);
		bigBlue.setTextSize(50);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		int xPoint = getMeasuredWidth() / 2;
		int yPoint = getMeasuredHeight() / 2;

		canvas.drawColor(Color.argb(77, 33, 77, 22));

		float radius = (float) (Math.max(xPoint, yPoint) * 0.6);
		canvas.drawCircle(xPoint, yPoint, radius, black);
		canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), black);

		int offset = 25;
		canvas.drawText("Speed:" + String.valueOf((int)speed), xPoint + 200, 50, bigBlack);
		canvas.drawText("Direc:" + String.valueOf((int)direc), xPoint + 200, 100, bigBlue);
		canvas.drawText("left :" + String.valueOf((int)left), xPoint + 200,  150, bigBlack);
		canvas.drawText("right:" + String.valueOf((int)right), xPoint + 200, 200, bigBlue);
		
		if(inSSC != 127)
			canvas.drawText("inSSC "+String.valueOf(inSSC)+
							" V:"+String.valueOf(inSpeed)+
							" D:"+String.valueOf(inDirection)+
							" X:"+String.valueOf(inExtraCmd), xPoint-400, yPoint+12*offset, black);
		else
			canvas.drawText("no feedback via BT", xPoint-200, yPoint+5*offset, bigRed);
			
		canvas.drawLine(xPoint, 0, xPoint, getMeasuredHeight(), blue);
		canvas.drawLine(0, yPoint, getMeasuredWidth(), yPoint, blue);
		
		if(direc>0)
			canvas.drawRect(
					(float)xPoint, 
					(float)yPoint, 
					(float)(xPoint+direc/255*radius), 
					(float)yPoint+30, 
					blue);
		else
			canvas.drawRect(
					(float)(xPoint+direc/255*radius), 
					(float)yPoint, 
					(float)xPoint, 
					(float)yPoint+30, 
					white);
		if(speed>0)	
			canvas.drawRect(
					(float)xPoint, 
					(float)yPoint, 
					(float)xPoint+30, 
					(float)(yPoint+speed/255*radius), 
					green);
		else
			canvas.drawRect(
					(float)xPoint, 
					(float)(yPoint+speed/255*radius), 
					(float)xPoint+30, 
					(float)yPoint, 
					red);

		canvas.drawRect(
				(float)xPoint-230, 
				(float)(yPoint-200), 
				(float)xPoint-200, 
				(float)yPoint+200, 
				black);
		canvas.drawRect(
				(float)xPoint-190, 
				(float)(yPoint-200), 
				(float)xPoint-160, 
				(float)yPoint+200, 
				black);

  		if(left<0)
 			canvas.drawRect(
					(float)xPoint-225, 
					(float)(yPoint), 
					(float)xPoint-205, 
					(float)(yPoint-left*200/255), 
					green);
		else
			canvas.drawRect(
				(float)xPoint-225, 
				(float)(yPoint-left*200/255), 
				(float)xPoint-205, 
				(float)yPoint, 
				green);
  		if(right<0)
 			canvas.drawRect(
					(float)xPoint-185, 
					(float)(yPoint), 
					(float)xPoint-165, 
					(float)(yPoint-right*200/255), 
					green);
		else
			canvas.drawRect(
				(float)xPoint-185, 
				(float)(yPoint-right*200/255), 
				(float)xPoint-165, 
				(float)yPoint, 
				green);
		
	}

	public void updateData(int speed, int direc, int left, int right) {
		this.speed    = speed;
		this.direc    = direc;
		this.left = left;
		this.right = right;
		invalidate();
	}
	public void updatePeriodicData(int inSSC, int inSpeed, int inDirection, int inExtraCmd) {
		this.inSSC    = inSSC;
		this.inSpeed = inSpeed;
		this.inDirection = inDirection;
		this.inExtraCmd = inExtraCmd;
		//invalidate();
	}
} 