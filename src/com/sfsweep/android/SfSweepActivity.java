package com.sfsweep.android;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Toast;

import com.activeandroid.query.From;
import com.activeandroid.query.Select;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class SfSweepActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		OnCameraChangeListener, OnMapClickListener {

	private SupportMapFragment mapFragment;
	private GoogleMap map;
	private LocationClient mLocationClient;
	private HashMap<StreetSweeperData, Polyline> cache;
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	long PARKING_DURATION_MILLIS = 1000 * 60 * 60 * 24 * 7;
	private boolean expanded = false;
	private int animDuration;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_demo_activity);
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
				map.setOnCameraChangeListener(this);
				map.setOnMapClickListener(this);

			} else {
				Toast.makeText(this, "Error - Map was null!!",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, "Error - Map Fragment was null!!",
					Toast.LENGTH_SHORT).show();
		}

		animDuration = (int) (1000 / getResources().getDisplayMetrics().density);

		cache = new HashMap<StreetSweeperData, Polyline>();
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
		fetchData(bounds);
	}

	public void fetchData(LatLngBounds bounds) {
		List<StreetSweeperData> l = getDataFromDb(bounds);
		updateCache(l);
		stylePolylines();
	}

	private void stylePolylines() {
		// Apply the new colors
		Date now = new Date();
		for (StreetSweeperData d : cache.keySet()) {
			Polyline line = cache.get(d);

			// Heatmap mode
			Date nextSweeping = d.nextSweeping();
			if (nextSweeping != null) {
				long diff = d.nextSweeping().getTime() - now.getTime();
				double percent = 1.0 * diff / PARKING_DURATION_MILLIS;
				int color = Color.rgb(0, Math.min(255, (int) (255 * percent)),
						0);
				line.setColor(color);
			} else {
				line.setColor(Color.MAGENTA);
			}
		}
	}

	private void updateCache(List<StreetSweeperData> l) {
		HashMap<StreetSweeperData, Polyline> newCache = new HashMap<StreetSweeperData, Polyline>();

		int addCount = 0;
		for (StreetSweeperData d : l) {
			if (cache.containsKey(d)) {
				newCache.put(d, cache.get(d));
				cache.remove(d);
			}else{
				PolylineOptions opts = new PolylineOptions();
				opts.addAll(d.getCoordinates());
				Polyline line = map.addPolyline(opts);
				newCache.put(d, line);
				addCount++;
			}
		}

		Log.e("fetchData", String.format("Cache size: %s (+%s,-%s)", newCache.size(), addCount, cache.size()));
		
		// Remove offscreen data
		for (Polyline p : cache.values()) {
			p.remove();
		}
		
		// Save the new cache
		cache = newCache;
	}

	private List<StreetSweeperData> getDataFromDb(LatLngBounds bounds) {
		double min_latitude = bounds.southwest.latitude;
		double max_latitude = bounds.northeast.latitude;
		double min_longitude = bounds.southwest.longitude;
		double max_longitude = bounds.northeast.longitude;

		double buffer_latitude = (max_latitude - min_latitude) / 2;
		double buffer_longitude = (max_longitude - min_longitude) / 2;

		Object[] args = { min_latitude - buffer_latitude,
				max_latitude + buffer_latitude,
				min_longitude - buffer_longitude,
				max_longitude + buffer_longitude };

		From query = new Select()
				.from(StreetSweeperData.class)
				.where("((min_latitude BETWEEN ?1 AND ?2 OR max_latitude BETWEEN ?1 AND ?2)"
						+ " AND (min_longitude BETWEEN ?3 AND ?4 OR max_longitude BETWEEN ?3 AND ?4))",
						args);
		List<StreetSweeperData> l = query.execute();
		return l;
	}

	@Override
	public void onMapClick(LatLng latLng) {

		if (expanded) {
			// Re-enable controls
			map.setMyLocationEnabled(true);
		} else {
			// Set the marker
			map.addMarker(new MarkerOptions().position(latLng));

			// Disable controls
			map.setMyLocationEnabled(false);

			// Zoom to the click
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
			map.animateCamera(cameraUpdate, animDuration, null);
		}

		View v = findViewById(R.id.container);
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

	public class HeightAnimation extends Animation {
		private final int targetHeight;
		private final int originalHeight;
		private final View view;

		public HeightAnimation(View view, int targetHeight) {
			this.view = view;
			this.targetHeight = targetHeight;
			this.originalHeight = view.getLayoutParams().height;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			int diff = targetHeight - originalHeight;
			int newHeight = Math.round(interpolatedTime * diff)
					+ originalHeight;
			view.getLayoutParams().height = newHeight;
			view.requestLayout();
		}

		@Override
		public void initialize(int width, int height, int parentWidth,
				int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
		}

		@Override
		public boolean willChangeBounds() {
			return true;
		}
	}

}