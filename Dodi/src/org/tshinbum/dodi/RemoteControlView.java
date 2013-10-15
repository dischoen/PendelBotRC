package org.tshinbum.dodi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class RemoteControlView extends View {

  private Paint paint;
  private Paint paint2;
  private Paint paint3;
  
  private float x2 = 0;
  private float y2 = 0;
  private float z2 = 0;
  private float light = 0;
  

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
    paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStrokeWidth(2);
    paint.setTextSize(25);
    paint.setStyle(Paint.Style.STROKE);
    paint.setColor(Color.BLACK);
    //
    paint2 = new Paint();
    paint2.setAntiAlias(true);
    paint2.setStrokeWidth(2);
    paint2.setTextSize(25);
    paint2.setStyle(Paint.Style.STROKE);
    paint2.setColor(Color.BLUE);
    //
    paint3 = new Paint();
    paint3.setAntiAlias(true);
    paint3.setStrokeWidth(2);
    paint3.setTextSize(25);
    paint3.setStyle(Paint.Style.STROKE);
    paint3.setColor(Color.WHITE);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    int xPoint = getMeasuredWidth() / 2;
    int yPoint = getMeasuredHeight() / 2;

    canvas.drawColor(Color.argb(77, 33, 77, 22));
    
    float radius = (float) (Math.max(xPoint, yPoint) * 0.6);
    canvas.drawCircle(xPoint, yPoint, radius, paint);
    canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);

    int offset = 25;
    canvas.drawText("x2 "+String.valueOf(x2),        xPoint-200, yPoint,          paint);
    canvas.drawText("y2 "+String.valueOf(y2),        xPoint-200, yPoint+offset,   paint);
    canvas.drawText("z2 "+String.valueOf(z2),        xPoint-200, yPoint+2*offset, paint);
    
    double magnitude = Math.sqrt(x2*x2+y2*y2+z2*z2);
    canvas.drawText("magnitude "+String.valueOf(magnitude), xPoint-200, yPoint+4*offset, paint);

    // 3.143 is a good approximation for the circle
    canvas.drawLine(
    		xPoint,
    		yPoint,
    		(float) (xPoint + radius
    				* Math.sin((double) (-x2) / magnitude * 3.143)),
    		(float) (yPoint - radius
    				* Math.cos((double) (-x2) / magnitude * 3.143)), paint);
    canvas.drawLine(
    		xPoint+5,
    		yPoint+5,
    		(float) (xPoint + radius
    				* Math.sin((double) (-x2) / magnitude * 3.143)),
    		(float) (yPoint - radius
    				* Math.cos((double) (-x2) / magnitude * 3.143)), paint);

    
    
    canvas.drawLine(
    		xPoint,
    		yPoint,
    		(float) (xPoint + radius
    				* Math.sin((double) (-y2) / magnitude * 3.143)),
    		(float) (yPoint - radius
    				* Math.cos((double) (-y2) / magnitude * 3.143)), paint2);
    canvas.drawLine(
    		xPoint+5,
    		yPoint+5,
    		(float) (xPoint + radius
    				* Math.sin((double) (-y2) / magnitude * 3.143)),
    		(float) (yPoint - radius
    				* Math.cos((double) (-y2) / magnitude * 3.143)), paint2);
    canvas.drawLine(
    		xPoint,
    		yPoint,
    		(float) (xPoint + radius
    				* Math.sin((double) (-z2) / magnitude * 3.143)),
    		(float) (yPoint - radius
    				* Math.cos((double) (-z2) / magnitude * 3.143)), paint3);
    canvas.drawLine(
    		xPoint+5,
    		yPoint+5,
    		(float) (xPoint + radius
    				* Math.sin((double) (-z2) / magnitude * 3.143)),
    		(float) (yPoint - radius
    				* Math.cos((double) (-z2) / magnitude * 3.143)), paint3);

    
    canvas.drawText("light "+String.valueOf(light),        xPoint-200, yPoint-3*offset, paint);
  }

  public void updateData(float[] values, long ts) {
	    this.x2    = values[0];
	    this.y2    = values[1];
	    this.z2    = values[2];
  
  invalidate();
}
} 