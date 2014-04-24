package com.sfsweep.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.sfsweep.android.R;
import com.sfsweep.android.fragments.HubFragment.OnHubItemClickListener;

public class HubActivity extends ActionBarActivity implements OnHubItemClickListener {

	private static final int    ALARM_REQUEST     = 1,
							    ADDRESS_REQUEST   = 2,
								SETTINGS_REQUEST  = 3;
	
	private static final String ADDRESS_ACTIVITY  = "com.sfsweep.android.activities.AddressActivity",
	                            ALARM_ACTIVITY    = "com.sfsweep.android.activities.AlarmActivity",
	                            MAP_ACTIVITY      = "com.sfsweep.android.activities.MapActivity",
	                            SETTINGS_ACTIVITY = "com.sfsweep.android.activities.SettingsActivity";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hub); 
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.hub, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onHubItemClick(String hubFragmentType) {
		String destination = defineDestination(hubFragmentType); 
		Log.d("DEBUG", "destination is: " + destination); 
		Intent i = new Intent().setClassName(this, destination); 
		
		if (destination.equals(ADDRESS_ACTIVITY)) {
			startActivityForResult(i, ADDRESS_REQUEST); 
		} else if (destination.equals(ALARM_ACTIVITY)) {
			startActivityForResult(i, ALARM_REQUEST);
		} else if (destination.equals(SETTINGS_ACTIVITY)) {
			startActivityForResult(i, SETTINGS_REQUEST); 
		} else {
			// TODO: Call startActivity(i) if activity entered through system notification
			finish(); 	// destination is MapHubFragment
		}
	}
	
	private String defineDestination(String destination) {
		if (destination.startsWith("Address")) {
			destination = ADDRESS_ACTIVITY;
		} else if (destination.startsWith("Alarm")) {
			destination = ALARM_ACTIVITY;
		} else if (destination.startsWith("Map")) {
			destination = MAP_ACTIVITY; 
		} else {
			destination = SETTINGS_ACTIVITY; 
		}
		return destination; 
	}
	
}
