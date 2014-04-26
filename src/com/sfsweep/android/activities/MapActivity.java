package com.sfsweep.android.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
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
import com.sfsweep.android.fragments.NotificationFragment.OnScheduleAlarmCallbacks;
import com.sfsweep.android.fragments.SweepDataDetailFragment;
import com.sfsweep.android.helpers.HeightAnimation;
import com.sfsweep.android.models.StreetSweeperData;

public class MapActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		OnCameraChangeListener, OnMapClickListener, OnScheduleAlarmCallbacks {

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
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
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
		
		animDuration = (int) (1000 / getResources().getDisplayMetrics().density);

	}

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
					.round(this.getWindow().getDecorView().getBottom() * 0.6f);
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
	
}