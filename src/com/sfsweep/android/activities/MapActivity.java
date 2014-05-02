package com.sfsweep.android.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sfsweep.android.R;
import com.sfsweep.android.adapters.StreetSweeperDataMapAdapter;
import com.sfsweep.android.fragments.NotifierDrawerFragment;
import com.sfsweep.android.fragments.NotifierFragment.OnScheduleAlarmCallbacks;
import com.sfsweep.android.fragments.NotifierIconFragment;
import com.sfsweep.android.fragments.NotifierIconFragment.OnNotifierIconClickListener;
import com.sfsweep.android.fragments.SweepDataDetailFragment;
import com.sfsweep.android.helpers.HeightAnimation;
import com.sfsweep.android.models.StreetSweeperData;

public class MapActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		OnCameraChangeListener, OnMapClickListener, OnScheduleAlarmCallbacks,
		OnNotifierIconClickListener {

	private static final LatLng SF = new LatLng(37.7577, -122.4376);

	private SupportMapFragment mapFragment;
	private GoogleMap map;
	private LocationClient mLocationClient;
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private static final int HUB_REQUEST = 1; 
	private static final String NOTIFIER_ICON_FRAGMENT_TAG = "notifier_icon_fragment_tag"; 
	private static final String NOTIFIER_CONTAINER_FRAGMENT_TAG = "notifier_container_fragment_tag"; 
	
	private boolean expanded = false;
	private int animDuration;
	private Marker marker;

	private SweepDataDetailFragment sweepDataDetailFragment;

	private Button   mBtnMoveBy;
	private TextView mTvMoveBy;
	private TextView mTvDay; 
	
	private String   mFont = "Roboto-Light.ttf";
	private Typeface mTypeface;

	private StreetSweeperDataMapAdapter mapAdapter; 
	private int spinnerItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		
		//************************************************************************
				//Mimi objects
				//R always refers to xml data
					final Spinner spinner1= (Spinner) findViewById(R.id.spinner1); // find spinner1 in xml and attach it to spinner1 java
						ArrayAdapter<CharSequence> spinnerAdapter1 = ArrayAdapter.createFromResource( // got  ahandle to the adapter
								this, R.array.spinner1_opt, android.R.layout.simple_spinner_item); //spinner1_opt is array list of positions in drop down Sun-Sat
					
						spinnerAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //associating the adapter with the spinner
						spinner1.setAdapter(spinnerAdapter1); 
						//readItems(); //reads whatever is in the file that is written (which was the current position at the time (last selection) into spinneritem
						  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getBaseContext());//declare prefs as a variable
			              // above: give me the handle
						
						int spinselection = prefs.getInt("savedValue", 0); //RHS = get the retrieved value and LHS creates a variable to save the retrieved value
						// "saved value" is the key that the int is the value is associated with
						
						spinner1.setSelection(spinselection); // use initial spinner position from text file. Use this position to set current value of spinner
						spinner1.setAdapter(spinnerAdapter1); // have spinner in place and keeping track of what position it is in
						//Have spinner do something when you select item

						spinner1.setOnItemSelectedListener(new OnItemSelectedListener() { 
					
						 
							//spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {           
						           @Override
						           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						        	   // can only store primitive data types like ints, strings(only non primitive),boolean, float, double, char
						        	   // the object in this case is the shared preference. will get a handle to the shared pref object. 
						        	   //common pattern for already accessible android object create from the sharedpref class
						               SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getBaseContext());//declare prefs as a variable
						              // above: give me the handle
						               SharedPreferences.Editor prefEditor = prefs.edit();// intermediary abstraction to hide implementation. We share saved prefs in batches
						               prefEditor.putInt("savedValue",position);// what's returned from spinner.getsel will be stored
						               prefEditor.commit();
						               String savedValue=spinner1.getSelectedItem().toString();

						               for(int i=0;i<7;i++)
						               if(savedValue.equals(spinner1.getItemAtPosition(i).toString()))
						               {
						                   spinner1.setSelection(i);
						                   break;
						               }
						           }
						           @Override
						           public void onNothingSelected(AdapterView<?> parent){}
						    });
	
						
						
						
						
//						public void onItemSelected(AdapterView<?> arg0, View arg1,
//					                   int pos, long arg) {
//								 Spinner spinner1= (Spinner) findViewById(R.id.spinner1); //Get handle to spinner1 in xml file
//								 spinnerItem=pos;//store user selection in spinnerItem variable
//								 //writeItems();//write user selection to file save value of spinner item which is position to a file 
//								 if(pos ==0){  // conditional: based on selected spinner value it will execute different code. If position =0 
//									 			//then it is first option in spinner menu=> heatmap mode
//									 Log.d("DEBUG", "Hello from on if_heatmap");
//								     mapAdapter.setModeHeatmap() ; // calls 
//								    }
//								    else 
//								    	{mapAdapter.setModeWeekday(spinner1.getSelectedItem().toString());
//								    	Log.d("DEBUG", "Hello from weekdayMode");
//								    	}
//								    Log.d("DEBUG", "Hello from on item selected");				
//							 }
//							@Override
//							public void onNothingSelected(AdapterView<?> arg0) { //super type is a method inherited form the parent class
//								// TODO Auto-generated method stub
//							}
//								
//								});

		
		mLocationClient = new LocationClient(this, this, this);
		mapFragment = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map));
		if (mapFragment != null) {
			map = mapFragment.getMap();
			if (map != null) {
				Toast.makeText(this, "Map Fragment was loaded properly!",
						Toast.LENGTH_SHORT).show();
				map.setMyLocationEnabled(true);
				map.getUiSettings().setZoomControlsEnabled(false);

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(SF, 18));

				map.setOnCameraChangeListener(this);
				map.setOnMapClickListener(this);
				
				mapAdapter = new StreetSweeperDataMapAdapter(map);
//				mapAdapter.setModeWeekday("Fri");

			} else {
				Toast.makeText(this, "Error - Map was null!!",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, "Error - Map Fragment was null!!",
					Toast.LENGTH_SHORT).show();
		}

		sweepDataDetailFragment = (SweepDataDetailFragment) getSupportFragmentManager()
				.findFragmentById(R.id.sweepDetail);

		setupMoveByButton();
		setupFragments(); 
		
		animDuration = (int) (1000 / getResources().getDisplayMetrics().density);

	}
	
	//*******************************************************************************
//		public class Spinner1Activity extends Activity {
//			
//		}
//				
//		 private void readItems() {
//			   File filesDir = getFilesDir();
//			   File spinnerFile = new File(filesDir, "spinner.tx"); //open the file on device 
//			 
//			   try { 
//				   // read the text from file and puts it in the spinneritems variable. which is an int showing currently selected value of the spinner
//				   spinnerItem = Integer.parseInt(FileUtils.readFileToString(spinnerFile));
//			   } catch (IOException e) {
//				   spinnerItem = 7 ; //  set an arbitrary default value... 
//				   //spinnerItem = new String(); //left is what is getting assigned. right is what you are putting there
//				   
//			   }
//
//		}
//		   private void writeItems() {
//			   File filesDir = getFilesDir();
//			   File spinnerFile = new File(filesDir, "spinner.tx"); //opening a file on the device
//			   try {
//				   FileUtils.writeStringToFile(spinnerFile, String.valueOf(spinnerItem)); //writing text to the file
//			   } catch (IOException e) {
//				   e.printStackTrace();
//			   }	   
//		   }
		
		   //********************************************************************************************


	private void setupMoveByButton() {
		mTypeface = Typeface.createFromAsset(getAssets(), mFont); 
		
		mTvMoveBy = (TextView) findViewById(R.id.tvMoveBy); 
		mTvMoveBy.setTypeface(mTypeface); 
		
		mTvDay    = (TextView) findViewById(R.id.tvDay); 
		mTvDay.setTypeface(mTypeface); 
		
		mBtnMoveBy = (Button) findViewById(R.id.btnMoveBy); 
		mBtnMoveBy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MapActivity.this, com.sfsweep.android.zdeprecated.HubActivity.class);
				startActivityForResult(i, HUB_REQUEST); 
			}
		});
	}
	
	private void setupFragments() {
		NotifierIconFragment niFragment = new NotifierIconFragment(); 
		NotifierDrawerFragment ncFragment = new NotifierDrawerFragment(); 
		
		FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction()
		  .add(R.id.flIconContainer, niFragment, NOTIFIER_ICON_FRAGMENT_TAG)
		  .add(R.id.flNotifierContainer, ncFragment, NOTIFIER_CONTAINER_FRAGMENT_TAG)
		  .hide(ncFragment)
		  .commit();
	}
	
	/*
	 * Called when the Activity becomes visible.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		if (isGooglePlayServicesAvailable()) {
			mLocationClient.connect();
		}

	}

	/*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
		super.onStop();
	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				mLocationClient.connect();
				break;
			}

		}
	}

	private boolean isGooglePlayServicesAvailable() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			return true;
		} else {
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getSupportFragmentManager(),
						"Location Updates");
			}

			return false;
		}
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle dataBundle) {
		// Display the connection status
		Location location = mLocationClient.getLastLocation();
		if (location != null) {
			Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT)
					.show();
			LatLng latLng = new LatLng(location.getLatitude(),
					location.getLongitude());
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					latLng, 18);
			map.animateCamera(cameraUpdate);
		} else {
			Toast.makeText(this,
					"Current location was null, enable GPS on emulator!",
					Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			Toast.makeText(getApplicationContext(),
					"Sorry. Location services not available to you",
					Toast.LENGTH_LONG).show();
		}
	}

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	@Override
	public void onCameraChange(CameraPosition pos) {
		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
		Log.e("onCameraChange", bounds.toString());
		mapAdapter.fetchData(bounds);
	}

	@Override
	public void onMapClick(LatLng point) {

		Pair<StreetSweeperData, LatLng> p = mapAdapter.findNearestData(point);
		point = p.second;
		StreetSweeperData data = p.first;

		if (data == null)
			return;

		if (expanded) {
			// Remove the marker
			if (marker != null)
				marker.remove();

			// Re-enable controls
			map.setMyLocationEnabled(true);
		} else {

			sweepDataDetailFragment.setData(data);

			// Set the marker
			marker = map.addMarker(new MarkerOptions().position(point));

			// Disable controls
			map.setMyLocationEnabled(false);

			// Zoom to the click
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(point);
			map.animateCamera(cameraUpdate, animDuration, null);
		}

		View v = findViewById(R.id.sweepDetail);
		HeightAnimation a;
		if (expanded) {
			a = new HeightAnimation(v, 0);
		} else {
			int height = Math
					.round(this.getWindow().getDecorView().getBottom() * 0.24f);  // For reference: originally 0.6f
			a = new HeightAnimation(v, height);
		}
		a.setDuration(animDuration);
		v.startAnimation(a);
		expanded = !expanded;
	}

	@Override
	public String onSweepTimeRange() {
		SweepDataDetailFragment fragment = (SweepDataDetailFragment) getSupportFragmentManager()
				.findFragmentById(R.id.sweepDetail); 
		return fragment.getSweepTimeRange(); 
	}
	
	@Override
	public String onDaysToNextSweep() {
		SweepDataDetailFragment fragment = (SweepDataDetailFragment) getSupportFragmentManager().
				findFragmentById(R.id.sweepDetail);
		return fragment.getDaysToNextSweep();
	}
	
	@Override
	public void onNotifierIconClick() {
		// Bump drawer to show notifiers
		View v = findViewById(R.id.sweepDetail);
		int height = Math.round(this.getWindow().getDecorView().getBottom() * 0.60f);  
		HeightAnimation a = new HeightAnimation(v, height);
		a.setDuration(animDuration);
		v.startAnimation(a);
		
		// Show notifier container fragment 
		FragmentManager fm = getSupportFragmentManager(); 
		NotifierDrawerFragment ncFragment = (NotifierDrawerFragment) 
				fm.findFragmentByTag(NOTIFIER_CONTAINER_FRAGMENT_TAG);
		fm.beginTransaction().show(ncFragment).commit(); 
	}
}