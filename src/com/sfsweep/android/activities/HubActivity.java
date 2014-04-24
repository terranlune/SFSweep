package com.sfsweep.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.internal.hf;
import com.sfsweep.android.R;
import com.sfsweep.android.fragments.AddressHubFragment;
import com.sfsweep.android.fragments.AlarmHubFragment;
import com.sfsweep.android.fragments.HubFragment;
import com.sfsweep.android.fragments.HubFragment.OnHubItemClickListener;
import com.sfsweep.android.fragments.MapHubFragment;

public class HubActivity extends ActionBarActivity implements OnHubItemClickListener {

	private static final int ALARM_REQUEST   = 1,
							 ADDRESS_REQUEST = 2;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hub); 
		
		setupFragments();
	}
	
	private void setupFragments() {
		FragmentManager fm = getSupportFragmentManager(); 
		
		MapHubFragment     hfMap     = new MapHubFragment();   
		AlarmHubFragment   hfAlarm   = new AlarmHubFragment();
		AddressHubFragment hfAddress = new AddressHubFragment(); 
		
		fm.beginTransaction().add(R.id.hubFragmentContainer, hfMap)
						     .add(R.id.hubFragmentContainer, hfAlarm)
						     .add(R.id.hubFragmentContainer, hfAddress)
						     .commit(); 
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
		Intent i = new Intent().setClassName(this, destination); 
		
		if (destination.equals("AddressActivity")) {
			startActivityForResult(i, ADDRESS_REQUEST); 
		} else if (destination.equals("AlarmActivity")) {
			startActivityForResult(i, ALARM_REQUEST);
		} else {
			startActivity(i); 	// destination is MapHubFragment
		}
	}
	
	private String defineDestination(String destination) {
		if (destination.startsWith("Address")) {
			destination = "AddressActivity";
		} else if (destination.startsWith("Alarm")) {
			destination = "AlarmActivity";
		} else {
			destination = "MapActivity"; 
		}
		return destination; 
	}
	
}
