package org.tshinbum.dodi;

import java.io.InputStream;
import java.io.OutputStream;

import org.tshinbum.dodi.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

  private static SensorManager sensorService;
  private RemoteControlView rcView;
  private Sensor sensor;

  TextView out;

  private static final int REQUEST_ENABLE_BT = 1;
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private OutputStream outStream = null;
  private InputStream inStream = null;

  
/** Called when the activity is first created. */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //setContentView(compassView);
	setContentView(R.layout.activity_main);
    //compassView = new MyCompassView(this);
    rcView = (RemoteControlView) findViewById(R.id.myCompassView1);
    
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    
    sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    
    sensor = sensorService.getDefaultSensor(Sensor.TYPE_GRAVITY);
    if (sensor != null) {
      sensorService.registerListener(mySensorEventListener2, sensor,
          SensorManager.SENSOR_DELAY_NORMAL);
      Log.i("Compass MainActivity", "Registerered for GRAVITY Sensor");

    } else {
      Log.e("Compass MainActivity", "Registerered for GRAVITY Sensor");
      Toast.makeText(this, "GRAVITY Sensor not found",
          Toast.LENGTH_LONG).show();
      //finish();
    }

    sensor = sensorService.getDefaultSensor(Sensor.TYPE_LIGHT);
    if (sensor != null) {
      sensorService.registerListener(mySensorEventListener3, sensor,
          SensorManager.SENSOR_DELAY_NORMAL);
      Log.i("Compass MainActivity", "Registerered for LIGHT Sensor");

    } else {
      Log.e("Compass MainActivity", "Registerered for LIGHT Sensor");
      Toast.makeText(this, "LIGHT Sensor not found",
          Toast.LENGTH_LONG).show();
      //finish();
    }

    out = (TextView) findViewById(R.id.out);
    btAdapter = BluetoothAdapter.getDefaultAdapter();
    CheckBTState();

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (sensor != null) {
        sensorService.unregisterListener(mySensorEventListener2);
        sensorService.unregisterListener(mySensorEventListener3);
    }
  }

  private SensorEventListener mySensorEventListener2 = new SensorEventListener() {

	    @Override
	    public void onAccuracyChanged(Sensor sensor, int accuracy) {
	    }

	    @Override
	    public void onSensorChanged(SensorEvent event) {
	      // angle between the magnetic north direction
	      // 0=North, 90=East, 180=South, 270=West
	      //float azimuth = event.values[0];
	      rcView.updateData2(event.values,event.timestamp);
	    }
	  };

	  private SensorEventListener mySensorEventListener3 = new SensorEventListener() {

		    @Override
		    public void onAccuracyChanged(Sensor sensor, int accuracy) {
		    }

		    @Override
		    public void onSensorChanged(SensorEvent event) {
		      // angle between the magnetic north direction
		      // 0=North, 90=East, 180=South, 270=West
		      //float azimuth = event.values[0];
		      rcView.updateData3(event.values,event.timestamp);
		    }
		  };

	  private void CheckBTState() {
		    // Check for Bluetooth support and then check to make sure it is turned on
		 
		    // Emulator doesn't support Bluetooth and will return null
		    if(btAdapter==null) { 
		      AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
		    } else {
		      if (btAdapter.isEnabled()) {
		        out.append("\n...Bluetooth is enabled...");
		      } else {
		        //Prompt user to turn on Bluetooth
		        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		      }
		    }
		  }
		   
		  public void AlertBox( String title, String message ){
		    new AlertDialog.Builder(this)
		    .setTitle( title )
		    .setMessage( message + " Press OK to exit." )
		    .setPositiveButton("OK", new OnClickListener() {
		        public void onClick(DialogInterface arg0, int arg1) {
		          finish();
		        }
		    }).show();
		  }
} 
