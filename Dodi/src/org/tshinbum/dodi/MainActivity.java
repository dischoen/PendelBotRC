package org.tshinbum.dodi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import org.tshinbum.dodi.R;

import android.os.Bundle;
import android.os.Handler;
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

	private static int SSC = 0;
	private static float raw_speed;
	private static float raw_direc;
	private static int speed;
	private static int direc;
	private static float speed_off = 0;
	private static float direc_off = 0;

	public static Handler mHandler;
	private int mInterval = 50;
	protected int left;
	protected int right;


	// SPP UUID
	private static final UUID MY_UUID =
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// My ARDUINOBT
	private static String address = "00:07:80:83:AC:00";

	//private ScheduledExecutorService scheduleTaskExecutor;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
		}

		out = (TextView) findViewById(R.id.out);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		CheckBTState();
		mHandler = new Handler();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		
		stopRepeatingTask();
		
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
			double magnitude = Math.sqrt(
						event.values[0]*event.values[0]+
						event.values[1]*event.values[1]+
						event.values[2]*event.values[2]);

			raw_speed = (float)(event.values[0]/magnitude);
			raw_direc = (float)(event.values[1]/magnitude);

			speed = (int)(255 * Math.sin(Math.asin(raw_speed) - Math.asin(speed_off)));
			direc = (int)(255 * Math.sin(Math.asin(raw_direc) - Math.asin(direc_off)));
			Log.d("onSensorChanged", "speed "+String.valueOf(speed)+" Dir:"+String.valueOf(direc));

			if(direc<0)
			{
				left  = speed + direc * 2 * speed / 255;
				right = speed;
				Log.d("onSensorChanged", "<left "+String.valueOf(left)+" right:"+String.valueOf(right));
			}
			else
			{
				left = speed;
				right = speed - direc * 2 * speed / 255;
				Log.d("onSensorChanged", ">left "+String.valueOf(left)+" right:"+String.valueOf(right));
			}
			// invert, for better usability
			left  = -left;
			right = -right;
			rcView.updateData(speed, direc, left, right);
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
	}

	public void sendCommand() {

		if(SSC==32)
			SSC = 0;


		byte[] outBuffer = new byte[8];
		outBuffer[0] = (byte)0xff;
		outBuffer[1] = (byte)0xff;
		outBuffer[2] = (byte)SSC++;
		outBuffer[3] = (byte)(left >> 8);
		outBuffer[4] = (byte)(left & 0xFF);
		outBuffer[5] = (byte)(right >> 8);
		outBuffer[6] = (byte)(right & 0xFF);
		outBuffer[7] = (byte)0x66;

		byte[] inBuffer = new byte[6];
		try {
			outStream.write(outBuffer);
			//inStream.available()
			inStream.read(inBuffer);
			int inSSC       = inBuffer[0];
			int inSpeed     = (inBuffer[1] << 8) + inBuffer[2];
			int inDirection = (inBuffer[3] << 8) + inBuffer[4];
			int inExtraCmd  = inBuffer[5];
			Log.d("sendCmd", "inSSC "+String.valueOf(inSSC)+" V:"+String.valueOf(inSpeed)+" D:"+String.valueOf(inDirection));
			rcView.updatePeriodicData(inSSC, inSpeed, inDirection, inExtraCmd);

		} catch (IOException e) {
			rcView.updatePeriodicData(127,0,0,0);
		}
	}

	Runnable mStatusChecker = new Runnable() {

		@Override 
		public void run() {
			sendCommand();
			mHandler.postDelayed(mStatusChecker, mInterval);
		}
	};

	void startRepeatingTask() {
		mStatusChecker.run(); 
	}

	void stopRepeatingTask() {
		mHandler.removeCallbacks(mStatusChecker);
	} 

	public void OnTimerStart(View view) {
		startRepeatingTask();
	};

	public void OnZero(View view) {
		zero();
	};

	void zero() {
		speed_off = raw_speed;
		direc_off = raw_direc;
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
