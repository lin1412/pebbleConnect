package com.example.pebbleconnect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver;
import com.getpebble.android.kit.PebbleKit.PebbleNackReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	
	private Button sendButton;
	private TextView lanView;
	private TextView greetingView;
	
	/**
	 * different values stored with the EVENT_ID_KEY
	 */
	private static final int DATA_SEND = 777;
	/**
	 * String used to identify Log messages coming from this class
	 */
	private static final String TAG = "Testing";
	/**
	 * The UUID of the Pebble APP running on the Pebble.
	 */
	private static final UUID PEBBLE_APP_UUID = UUID.fromString("57dd9c89-b66c-4db6-a18c-a1689c8c3b0e");
	
	private List<String> languageList;
	private List<String> greetingList;
	
	private boolean connected = false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		languageList = new ArrayList<String>();
		languageList.add("English");
		languageList.add("Chinese");
		languageList.add("Spanish");
		languageList.add("French");
		languageList.add("German");
		languageList.add("Italian");
		languageList.add("Korean");
		languageList.add("Russian");
		languageList.add("Japanese");
		languageList.add("Dutch");

		greetingList = new ArrayList<String>();
		greetingList.add("Hello");
		greetingList.add("Ni Hao");
		greetingList.add("Hola");
		greetingList.add("Bonjour");
		greetingList.add("Guten Tag");
		greetingList.add("Buonguiorno");
		greetingList.add("Ahn Nyeong Ha Se Yo");
		greetingList.add("Zdravstvuyte");
		greetingList.add("Konnichiwa");
		greetingList.add("Goedendag");
		
		setupPebbleCommunication();
		
		sendButton = (Button)findViewById(R.id.send_button);
		sendButton.setOnClickListener( new Button.OnClickListener(){
		      
			@Override
			public void onClick(View arg0) {
				if(connected){
					sendDataToPebble();
				}
				else{
					toastNotConnected();
				}
			}
        });
		
		lanView = (TextView)findViewById(R.id.language_view);
		greetingView = (TextView)findViewById(R.id.greeting_view);
		
		
	}
	
	public void toastNotConnected(){
		Toast.makeText(this, "No pebble connected!!!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * Sets up all listeners and stuff to facilitate communication with the
	 * Pebble Watch.
	 */
	private void setupPebbleCommunication() {

		// Check if Pebble is already connected!
		if (PebbleKit.isWatchConnected(getApplicationContext())) {

			String messageString = "Your Pebble watch is connected!";
			Toast.makeText(this, messageString, Toast.LENGTH_SHORT).show();
			Log.i(TAG, messageString);
			connected = true;
		} else {

			String messageString = "Your Pebble watch IS NOT connected! FIX THIS!";
			Toast.makeText(this, messageString, Toast.LENGTH_SHORT).show();
			Log.i(TAG, messageString);
			connected = false;
		}

		// Listen for the pebble connection event
		PebbleKit.registerPebbleConnectedReceiver(this,
				new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {

						String messageString = "You just connected your Pebble Watch! Congrats!";
						Toast.makeText(context, messageString,
								Toast.LENGTH_SHORT).show();
						Log.i(TAG, messageString);
						connected = true;
					}
				});

		// Listen for the pebble disconnected event
		PebbleKit.registerPebbleConnectedReceiver(this,
				new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {

						String messageString = "You just disconnected your Pebble Watch! Why you disconnect!??";
						Toast.makeText(context, messageString,
								Toast.LENGTH_SHORT).show();
						Log.i(TAG, messageString);
						connected = false;
					}
				});

		// Register to receive ACKS back from Pebble after sending message
		PebbleKit.registerReceivedAckHandler(this, new PebbleAckReceiver(
				PEBBLE_APP_UUID) {

			@Override
			public void receiveAck(Context context, int transactionId) {
				Log.i(TAG, "ACKS");
			}
		});

		// Register to receive NACKS back from the Pebble after failed sending message
		PebbleKit.registerReceivedNackHandler(this, new PebbleNackReceiver(
				PEBBLE_APP_UUID) {

			@Override
			public void receiveNack(Context context, int transactionId) {

				Log.i(TAG, "NACKS");
			}
		});

		// Register to receive messages
		PebbleKit.registerReceivedDataHandler(this,
				new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {

					@Override
					public void receiveData(Context context, int transactionId,
							PebbleDictionary data) {

						PebbleKit.sendAckToPebble(context, transactionId);
						
						sendDataToPebble();

					}
				});
	}
	
	/**
	 * Sends data to the PebbleWatch.
	 */
	private void sendDataToPebble(){
		//Add all items from the list to a PebbleDictionary 
		PebbleDictionary dict = new PebbleDictionary();
		
		//make random greetings
		int i = (int)(Math.random() * languageList.size()-1);
		String language = languageList.get(i);
		String hello = greetingList.get(i);
		
		dict.addString(1, language + ": " + hello);
		
		//display on android
		lanView.setText(language);
		greetingView.setText(hello);
		
		
		//Send the PebbleDictionary to the Pebble Watch app with PEBBLE_APP_UUID with the appropriate 
		//TransactionId 
		PebbleKit.sendDataToPebbleWithTransactionId(this, PEBBLE_APP_UUID, dict, DATA_SEND); 
		Log.i(TAG, "Grocery list to Pebble.......SENT!!!!!!!!!!!!!!!!!!!!"); 
	}
}
