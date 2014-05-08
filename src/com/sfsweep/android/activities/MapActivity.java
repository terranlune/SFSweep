package com.sfsweep.android.activities;

import java.util.Calendar;
import java.util.Date;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sfsweep.android.R;
import com.sfsweep.android.adapters.StreetSweeperDataMapAdapter;
import com.sfsweep.android.fragments.NotifierDrawerFragment;
import com.sfsweep.android.fragments.NotifierDrawerFragment.OnScheduleAlarmListener;
import com.sfsweep.android.fragments.SweepDataDetailFragment;
import com.sfsweep.android.fragments.SweepDataDetailFragment.OnClickParkActionListener;
import com.sfsweep.android.helpers.HeightAnimation;
import com.sfsweep.android.models.StreetSweeperData;

public class MapActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		OnCameraChangeListener, OnMapClickListener, OnScheduleAlarmListener,
		OnClickParkActionListener, OnMarkerClickListener,
		OnMyLocationButtonClickListener {

	private static final LatLng DEFAULT_LATLNG = new LatLng(37.7577, -122.4376); // SF
	private static final int DEFAULT_ZOOM = 18;

	private SupportMapFragment mapFragment;
	private GoogleMap map;
	private LocationClient mLocationClient;
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private static final String PREF_PARKED_SWEEP_DATA_ID = "parked_sweep_data_id";
	private static final String PREF_PARKED_SWEEP_DATA_LAT = "parked_sweep_data_lat";
	private static final String PREF_PARKED_SWEEP_DATA_LNG = "parked_sweep_data_lng";
	private static final String PREF_PARKED_SWEEP_DATA_DATE = "parked_sweep_data_date";
	private static final String PREF_MOVE_BY_DAY = "move_by_day";
	private static final String PREF_LAST_LAT = "last_lat";
	private static final String PREF_LAST_LNG = "last_lon";
	
	private final String DAY_OF_WEEK_1 = "Sun";
	private final String DAY_OF_WEEK_2 = "Mon";
	private final String DAY_OF_WEEK_3 = "Tues";
	private final String DAY_OF_WEEK_4 = "Wed";
	private final String DAY_OF_WEEK_5 = "Thurs";
	private final String DAY_OF_WEEK_6 = "Fri";
	private final String DAY_OF_WEEK_7 = "Sat";
	private final String DAY_OF_WEEK_DEFAULT = "n/a";
	
	private boolean expanded = false;
	private int animDuration;
	private Marker clickedMarker;
	private Marker parkedMarker;

	private SweepDataDetailFragment sweepDataDetailFragment;

	private TextView mTvMoveBy;
	private TextView mTvDay;
	private String mFont = "Roboto-Light.ttf";
	private Typeface mTypeface;

	private StreetSweeperDataMapAdapter mapAdapter;
	
	private ImageView ivZoomToParked;

	private StreetSweeperData clickedData;
	private boolean myLocationButtonClicked;
	private OnNavigationListener mNavigationListener;
	private Spinner mSpnModeSelector;
	private SpinnerAdapter mSpnAdapter; 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		sweepDataDetailFragment = (SweepDataDetailFragment) getSupportFragmentManager()
				.findFragmentById(R.id.sweepDetail);
		animDuration = (int) (1000 / getResources().getDisplayMetrics().density);
		myLocationButtonClicked = false;

		setupMap();
		restoreParkedMarker();
		setupZoomToParked();
		showMapControls();
		setupMoveByTab();
		setInitialCamera();

		showAlarm(getIntent());
	}

	private void showAlarm(Intent intent) {

		if(!intent.hasExtra(NotifierDrawerFragment.EXTRA_FROM_ALARM)) { return; }
		
		Date nextSweeping = new Date(intent.getLongExtra(NotifierDrawerFragment.EXTRA_NEXT_SWEEPING, 0));
		
		new AlertDialog.Builder(this)
	    .setTitle("Move your car!")
	    
	    .setMessage("Street sweeping " + DateUtils
				.getRelativeTimeSpanString(
						nextSweeping.getTime(),
						new Date().getTime(),
						DateUtils.MINUTE_IN_MILLIS))
//	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//	        public void onClick(DialogInterface dialog, int which) { 
//	            // continue with delete
//	        }
//	     })
	    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();
	}

	private void setupMap() {
		mLocationClient = new LocationClient(this, this, this);
		mapFragment = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map));
		if (mapFragment != null) {
			map = mapFragment.getMap();
			if (map != null) {
				map.setMyLocationEnabled(true);
				map.getUiSettings().setZoomControlsEnabled(false);
				map.setIndoorEnabled(false);

				map.setOnCameraChangeListener(this);
				map.setOnMapClickListener(this);
				map.setOnMarkerClickListener(this);
				map.setOnMyLocationButtonClickListener(this);

				mapAdapter = new StreetSweeperDataMapAdapter(map);

			} else {
				Toast.makeText(this, "Error - Unable to load map",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, "Error - Unable to load map fragment",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void setInitialCamera() {
		if (parkedMarker != null) {
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(
					parkedMarker.getPosition(), DEFAULT_ZOOM));
		} else {

			// Get last location
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			if (prefs.contains(PREF_LAST_LAT)) {
				LatLng last = new LatLng(prefs.getFloat(PREF_LAST_LAT, 0),
						prefs.getFloat(PREF_LAST_LNG, 0));
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(last,
						DEFAULT_ZOOM));
			} else {
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(
						DEFAULT_LATLNG, DEFAULT_ZOOM));
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_actionbar, menu); 
		ActionBar ab = getActionBar(); 
		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ab.setCustomView(R.layout.action_title); 
		setupSpinner(menu); 
		return true;
	}

	private void setupSpinner(Menu menu) {
		MenuItem spnModeSelectorMenuItem = menu.findItem(R.id.spnModeSelectorMenuItem); 
		RelativeLayout rlContainer = (RelativeLayout) MenuItemCompat.getActionView(spnModeSelectorMenuItem); 
		mSpnModeSelector = (Spinner) rlContainer.findViewById(R.id.spnModeSelector); 
		mSpnModeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					mapAdapter.setModeHeatmap(); 
				} else {
					mapAdapter.setModeWeekday(mSpnModeSelector
							.getItemAtPosition(position)
							.toString()); 
				}

				// Save spinner position
				PreferenceManager
						.getDefaultSharedPreferences(getBaseContext())
						.edit()
						.putInt("position", position)
						.commit(); 
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		// Set up and attach spinner adapter
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.spn_options_modes,
				R.layout.spinner_item_custom); 
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
		mSpnAdapter = adapter; 
		mSpnModeSelector.setAdapter(mSpnAdapter); 
		
		// Restore saved spinner selection, if any
		int spinSelection = PreferenceManager
							.getDefaultSharedPreferences(getBaseContext())
							.getInt("position", 0);
		mSpnModeSelector.setSelection(spinSelection);
	}
	
	private void setupZoomToParked() {
		ivZoomToParked = (ImageView) findViewById(R.id.ivZoomToParked);
		ivZoomToParked.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				zoomToParked();
			}

		});
	}

	private void restoreParkedMarker() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (prefs.contains(PREF_PARKED_SWEEP_DATA_ID)) {
			float lat = prefs.getFloat(PREF_PARKED_SWEEP_DATA_LAT, 0);
			float lng = prefs.getFloat(PREF_PARKED_SWEEP_DATA_LNG, 0);
			LatLng p = new LatLng(lat, lng);
			placeParkedMarker(p);
		}
	}

	protected void zoomToParked() {
		if (parkedMarker != null) {
			StreetSweeperData d = getParkedData();
			showSweepDetail(parkedMarker.getPosition(), d, true);
		}
	}

	private void setupMoveByTab() {
		mTypeface = Typeface.createFromAsset(getAssets(), mFont); 
		
		mTvMoveBy = (TextView) findViewById(R.id.tvMoveBy);
		mTvMoveBy.setTypeface(mTypeface);
		mTvMoveBy.setText(R.string.tv_move_by);  

		mTvDay = (TextView) findViewById(R.id.tvDay);
		mTvDay.setTypeface(mTypeface);
		mTvDay.setText(restoreMoveByDay());
	}

	private String restoreMoveByDay() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String moveByDay = prefs.getString(PREF_MOVE_BY_DAY, null);
		if (moveByDay == null) {
			moveByDay = DAY_OF_WEEK_DEFAULT;
		}
		return moveByDay;
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
			if (myLocationButtonClicked) {
				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				CameraUpdate cameraUpdate = CameraUpdateFactory
						.newLatLng(latLng);
				map.animateCamera(cameraUpdate);
			}
		} else {
			Toast.makeText(this, "Current location was null; enable GPS!",
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

		// Save the last location
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putFloat(PREF_LAST_LAT, (float) pos.target.latitude)
				.putFloat(PREF_LAST_LNG, (float) pos.target.longitude).commit();
	}

	@Override
	public void onMapClick(LatLng p) {

		Pair<StreetSweeperData, LatLng> pair = mapAdapter.findNearestData(p);
		LatLng clickedPoint = pair.second;
		clickedData = pair.first;

		if (clickedData == null)
			return;

		if (expanded) {
			removeClickedMarker();
			showMapControls();
			hideSweepDetail();
		} else {
			Marker marker = placeClickedMarker(clickedPoint);
			showSweepDetail(marker.getPosition(), clickedData, false);
		}
	}

	private void hideSweepDetail() {
		View v = findViewById(R.id.sweepDetail);
		HeightAnimation a = new HeightAnimation(v, 0);
		a.setDuration(animDuration);
		v.startAnimation(a);

		expanded = false;
	}

	private void showSweepDetail(LatLng point, StreetSweeperData data,
			Boolean parked) {

		hideMapControls();

		// Zoom to the click
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(point);
		map.animateCamera(cameraUpdate, animDuration, null);

		// Set the data
		sweepDataDetailFragment.setData(data, parked);

		// Animate the view's visibility
		View v = findViewById(R.id.sweepDetail);
		v.measure(
				MeasureSpec.makeMeasureSpec(this.getWindow().getDecorView()
						.getWidth(), MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int height = v.getMeasuredHeight();

		// int height = Math
		// .round(this.getWindow().getDecorView().getBottom() * 0.14f);
		HeightAnimation a = new HeightAnimation(v, height);
		a.setDuration(animDuration);
		v.startAnimation(a);

		expanded = true;
	}

	private void hideMapControls() {
		map.setMyLocationEnabled(false);
		ivZoomToParked.setVisibility(View.GONE);
	}

	private void showMapControls() {
		map.setMyLocationEnabled(true);
		if (PreferenceManager.getDefaultSharedPreferences(this).contains(
				PREF_PARKED_SWEEP_DATA_ID)) {
			ivZoomToParked.setVisibility(View.VISIBLE);
		} else {
			ivZoomToParked.setVisibility(View.GONE);
		}
	}

	@Override
	public long onScheduleAlarm() {
		long sweepStartDate = PreferenceManager.getDefaultSharedPreferences(
				this).getLong(PREF_PARKED_SWEEP_DATA_DATE, 0);
		return sweepStartDate;
	}

	@Override
	public void onClickParkAction(StreetSweeperData d) {
		onPark(d);
	}

	@Override
	public void onClickUnParkAction(StreetSweeperData d) {
		onUnPark(d);
	}

	protected void onPark(StreetSweeperData d) {
		LatLng p = clickedMarker.getPosition();

		placeParkedMarker(p);
		showSweepDetail(p, d, true);
		removeClickedMarker();

		this.sweepDataDetailFragment.setData(clickedData, true);
		Date sweepStartDate = this.sweepDataDetailFragment.getSweepStartDate();
		
		String moveByDay = getMoveByDay(sweepStartDate); 
		mTvDay.setText(moveByDay);

		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putLong(PREF_PARKED_SWEEP_DATA_ID, clickedData.getId())
				.putFloat(PREF_PARKED_SWEEP_DATA_LAT, (float) p.latitude)
				.putFloat(PREF_PARKED_SWEEP_DATA_LNG, (float) p.longitude)
				.putLong(PREF_PARKED_SWEEP_DATA_DATE, sweepStartDate.getTime())
				.putString(PREF_MOVE_BY_DAY, moveByDay)
				.commit();
	}

	private String getMoveByDay(Date sweepStartDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sweepStartDate);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

		String moveByDay;
		switch (dayOfWeek) {
		case 1:
			moveByDay = DAY_OF_WEEK_1;
			break;
		case 2:
			moveByDay = DAY_OF_WEEK_2;
			break;
		case 3:
			moveByDay = DAY_OF_WEEK_3;
			break;
		case 4:
			moveByDay = DAY_OF_WEEK_4;
			break;
		case 5:
			moveByDay = DAY_OF_WEEK_5;
			break;
		case 6:
			moveByDay = DAY_OF_WEEK_6;
			break;
		case 7:
			moveByDay = DAY_OF_WEEK_7;
			break;
		default:
			moveByDay = DAY_OF_WEEK_DEFAULT;
		}
		return moveByDay;
	}

	protected void onUnPark(StreetSweeperData d) {
		clickedData = d;
		LatLng p = parkedMarker.getPosition();
		placeClickedMarker(p);
		removeParkedMarker();

		// Void move-by day and save update
		mTvDay.setText(DAY_OF_WEEK_DEFAULT);
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putString(PREF_MOVE_BY_DAY, DAY_OF_WEEK_DEFAULT)
				.commit();

		showSweepDetail(p, d, false);
	}

	private Marker placeParkedMarker(LatLng p) {
		removeParkedMarker();
		parkedMarker = map.addMarker(new MarkerOptions().position(p).icon(
				BitmapDescriptorFactory.fromResource(R.drawable.ic_parkedmarker)));
						//.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
		return parkedMarker;
	}

	private void removeParkedMarker() {
		if (parkedMarker != null) {
			PreferenceManager.getDefaultSharedPreferences(this).edit()
					.remove(PREF_PARKED_SWEEP_DATA_ID)
					.remove(PREF_PARKED_SWEEP_DATA_LAT)
					.remove(PREF_PARKED_SWEEP_DATA_LNG).commit();
			parkedMarker.remove();
		}
	}

	private Marker placeClickedMarker(LatLng point) {
		removeClickedMarker();
		clickedMarker = map.addMarker(new MarkerOptions().position(point));
		return clickedMarker;
	}

	private void removeClickedMarker() {
		if (clickedMarker != null)
			clickedMarker.remove();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (clickedMarker != null
				&& marker.getId().equals(clickedMarker.getId())) {
			showSweepDetail(clickedMarker.getPosition(), clickedData, false);
		} else if (parkedMarker != null
				&& marker.getId().equals(parkedMarker.getId())) {
			StreetSweeperData d = getParkedData();
			showSweepDetail(parkedMarker.getPosition(), d, true);
		}
		return true;
	}

	private StreetSweeperData getParkedData() {
		long parkedDataId = PreferenceManager.getDefaultSharedPreferences(this)
				.getLong(PREF_PARKED_SWEEP_DATA_ID, -1);
		StreetSweeperData d = StreetSweeperData.getById(parkedDataId);
		return d;
	}

	@Override
	public boolean onMyLocationButtonClick() {
		myLocationButtonClicked = true;
		return false;
	}

}