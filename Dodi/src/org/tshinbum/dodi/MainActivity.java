package org.tshinbum.dodi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.tshinbum.dodi.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static SensorManager sensorService;
	private RemoteControlView rcView;
	private Button btnConnect;
	private Sensor sensor;

	TextView out;

	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private InputStream inStream = null;

	// Well known SPP UUID
	private static final UUID MY_UUID =
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Insert your server's MAC address
	private static String address = "00:07:80:83:AC:00";

	private ScheduledExecutorService scheduleTaskExecutor;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(compassView);
		setContentView(R.layout.activity_main);
		//compassView = new MyCompassView(this);
		scheduleTaskExecutor= Executors.newScheduledThreadPool(5);

		rcView = (RemoteControlView) findViewById(R.id.myCompassView1);
		btnConnect = (Button) findViewById(R.id.button1);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		sensor = sensorService.getDefaultSensor(Sensor.TYPE_GRAVITY);
		if (sensor != null) {
			sensorService.registerListener(mySensorEventListener, sensor,
					SensorManager.SENSOR_DELAY_NORMAL);
			Log.i("Compass MainActivity", "Registerered for GRAVITY Sensor");

		} else {
			Log.e("Compass MainActivity", "Registerered for GRAVITY Sensor");
			Toast.makeText(this, "GRAVITY Sensor not found",
					Toast.LENGTH_LONG).show();
			//finish();
		}

		out = (TextView) findViewById(R.id.out);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		CheckBTState();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		out.append("\n...In onPause()...");

		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				AlertBox("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
			}
		}

		if(btSocket != null)
		{
			try     {
				btSocket.close();
			} catch (IOException e2) {
				AlertBox("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (sensor != null) {
			sensorService.unregisterListener(mySensorEventListener);
		}
	}

	@Override
	protected void onStop() {
		if(btAdapter.isEnabled()) {
			btAdapter.disable();
		}
		super.onStop();
	}

	private SensorEventListener mySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			rcView.updateData(event.values,event.timestamp);
		}
	};

	private void CheckBTState() {
		// Check for Bluetooth support and then check to make sure it is turned on

		// Emulator doesn't support Bluetooth and will return null
		if(btAdapter==null) { 
			AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
		} 
	};

	public void OnConnect(View view) {
		if (btAdapter.isEnabled()) {
			out.append("\n...Bluetooth is enabled...");
			Connect2();
		} else {
			//Prompt user to turn on Bluetooth
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// If the request went well (OK) and the request was REQUEST_ENABLE_BT
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
			Connect2();
		}
	}


	protected void Connect2() {

		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Set<BluetoothDevice> bonded = btAdapter.getBondedDevices();
		for(BluetoothDevice dev : bonded) {

			if(!dev.getAddress().equals(address))
				continue;

			Toast toast = Toast.makeText(context, dev.getName() + " " + dev.getAddress(), duration);
			toast.show();

			out.append("\n...In onResume...\n...Attempting client connect...");
			// Set up a pointer to the remote node using it's address.
			BluetoothDevice device = btAdapter.getRemoteDevice(address);

			// Two things are needed to make a connection:
			//   A MAC address, which we got above.
			//   A Service ID or UUID.  In this case we are using the
			//     UUID for SPP.
			try {
				btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				AlertBox("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
			}

			// Discovery is resource intensive.  Make sure it isn't going on
			// when you attempt to connect and pass your message.
			btAdapter.cancelDiscovery();
		}
		// Establish the connection.  This will block until it connects.
		try {
			btSocket.connect();
			out.append("\n...Connection established and data link opened...");
		} catch (IOException e) {
			try {
				btSocket.close();
				return;
			} catch (IOException e2) {
				AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
			}
		}

		btnConnect.setBackgroundColor(Color.GREEN);
		// Create a data stream so we can talk to server.
		out.append("\n...Sending message to server...");

		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
		}

		try {
			inStream = btSocket.getInputStream();
		} catch (IOException e) {
			AlertBox("Fatal Error", "In onResume() and input stream creation failed:" + e.getMessage() + ".");
		}


		scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				// Parsing RSS feed:
				sendCommand();

				// If you need update UI, simply do this:
				//runOnUiThread(new Runnable() {
				//	public void run() {
						// update your UI component here.
					//	myTextView.setText("refreshed");
					//}
				}
			
		}, 0, 50, TimeUnit.MILLISECONDS);
	}

public void sendCommand() {

	String message = "Hello from Android.\n";
	byte[] msgBuffer = message.getBytes();
	byte[] inBuffer = new byte[10];
	try {
		outStream.write(msgBuffer);
		inStream.read(inBuffer);
	} catch (IOException e) {
		String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
		if (address.equals("00:00:00:00:00:00")) 
			msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
		msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

		AlertBox("Fatal Error", msg);       
	}
	int a = 34;
	a += 1;

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
