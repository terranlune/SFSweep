package com.sfsweep.android.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sfsweep.android.R;

public class AddressActivity extends ActionBarActivity {

	private TextView mTvAddressIntro;
	private Button   mBtnSweepDate;
	private Button   mBtnParkLocation;
	private Button   mBtnParkDate;
	
	private String   mFont = "Roboto-Light.ttf";
	private Typeface mTypeface;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_address);
		
		setupWidgets();
	}
	
	private void setupWidgets() {
		mTypeface = Typeface.createFromAsset(getAssets(), mFont);
		
		mTvAddressIntro = (TextView) findViewById(R.id.tvAddressIntro); 
		mTvAddressIntro.setTypeface(mTypeface); 
		
		mBtnSweepDate = (Button) findViewById(R.id.btnSweepDate); 
		mBtnSweepDate.setTypeface(mTypeface); 
		mBtnSweepDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Allow user to update sweep date
			}
		});
		
		mBtnParkLocation = (Button) findViewById(R.id.btnParkLocation);
		mBtnParkLocation.setTypeface(mTypeface); 
		mBtnParkLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Allow user to update park location
			}
		});
		
		mBtnParkDate = (Button) findViewById(R.id.btnParkDate); 
		mBtnParkDate.setTypeface(mTypeface); 
		mBtnParkDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Allow user to update park date
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.address, menu);
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

}
