package org.tshinbum.dodi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class RemoteControlView extends View {

	private Paint black;
	private Paint blue;
	private Paint white;
	private Paint bigBlue;
	private Paint bigBlack;
	private Paint bigRed;
	

	private float x2 = 0;
	private float y2 = 0;
	private float z2 = 0;
	private float light = 0;
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
		bigBlack = new Paint(black);
		bigBlack.setTextSize(50);
		bigRed = new Paint(bigBlack);
		bigRed.setColor(Color.RED);
		//
		blue = new Paint();
		blue.setAntiAlias(true);
		blue.setStrokeWidth(2);
		blue.setTextSize(25);
		blue.setStyle(Paint.Style.STROKE);
		blue.setColor(Color.BLUE);
		bigBlue = new Paint(blue);
		bigBlue.setTextSize(50);
		//
		white = new Paint();
		white.setAntiAlias(true);
		white.setStrokeWidth(2);
		white.setTextSize(25);
		white.setStyle(Paint.Style.STROKE);
		white.setColor(Color.WHITE);
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
		canvas.drawText("x2 "+String.valueOf(Math.round(x2*100)/100),        xPoint-200, yPoint,          black);
		canvas.drawText("y2 "+String.valueOf(Math.round(y2*100)/100),        xPoint-200, yPoint+offset,   black);
		canvas.drawText("z2 "+String.valueOf(Math.round(z2*100)/100),        xPoint-200, yPoint+2*offset, black);

		canvas.drawText("L" + String.valueOf((int)(Math.cos((double) -x2) * 255)), xPoint + 200, yPoint, bigBlack);
		canvas.drawText("F" + String.valueOf((int)(Math.sin((double) -y2) * 255)), xPoint + 200, yPoint+55, bigBlue);
		
		double magnitude = Math.sqrt(x2*x2+y2*y2+z2*z2);
		//canvas.drawText("magnitude "+String.valueOf(magnitude), xPoint-200, yPoint+4*offset, black);
		if(inSSC != 127)
			canvas.drawText("inSSC "+String.valueOf(inSSC)+
							" V:"+String.valueOf(inSpeed)+
							" D:"+String.valueOf(inDirection)+
							" X:"+String.valueOf(inExtraCmd), xPoint-200, yPoint+5*offset, black);
		else
			canvas.drawText("no feedback via BT", xPoint-200, yPoint+5*offset, bigRed);
			
		canvas.drawLine(
				xPoint,
				yPoint,
				(float) (xPoint + radius
						* Math.sin((double) (-x2) / magnitude * 3.143)),
						(float) (yPoint - radius
								* Math.cos((double) (-x2) / magnitude * 3.143)), black);
		canvas.drawLine(
				xPoint+5,
				yPoint+5,
				(float) (xPoint + radius
						* Math.sin((double) (-x2) / magnitude * 3.143)),
						(float) (yPoint - radius
								* Math.cos((double) (-x2) / magnitude * 3.143)), black);



		canvas.drawLine(
				xPoint,
				yPoint,
				(float) (xPoint + radius
						* Math.sin((double) (-y2) / magnitude * 3.143)),
						(float) (yPoint - radius
								* Math.cos((double) (-y2) / magnitude * 3.143)), blue);
		canvas.drawLine(
				xPoint+5,
				yPoint+5,
				(float) (xPoint + radius
						* Math.sin((double) (-y2) / magnitude * 3.143)),
						(float) (yPoint - radius
								* Math.cos((double) (-y2) / magnitude * 3.143)), blue);
		canvas.drawLine(
				xPoint,
				yPoint,
				(float) (xPoint + radius
						* Math.sin((double) (-z2) / magnitude * 3.143)),
						(float) (yPoint - radius
								* Math.cos((double) (-z2) / magnitude * 3.143)), white);
		canvas.drawLine(
				xPoint+5,
				yPoint+5,
				(float) (xPoint + radius
						* Math.sin((double) (-z2) / magnitude * 3.143)),
						(float) (yPoint - radius
								* Math.cos((double) (-z2) / magnitude * 3.143)), white);


		//canvas.drawText("light "+String.valueOf(light),        xPoint-200, yPoint-3*offset, paint);
	}

	public void updateData(float[] values, long ts) {
		this.x2    = values[0];
		this.y2    = values[1];
		this.z2    = values[2];

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