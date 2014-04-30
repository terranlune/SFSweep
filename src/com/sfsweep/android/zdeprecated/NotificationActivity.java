package com.sfsweep.android.zdeprecated;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.TextView;

import com.sfsweep.android.R;

public class NotificationActivity extends ActionBarActivity {

	private TextView mTvAlarmIntro;
	private Spinner  mSpnDayInterval;
	private Spinner  mSpnDaysInAdvance;
	private Spinner  mSpnHoursInAdvance;
	
	private String   mFont = "Roboto-Light.ttf";
	private Typeface mTypeface;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		
		setupWidgets();
	}
	
	private void setupWidgets() {
		mTypeface = Typeface.createFromAsset(getAssets(), mFont); 
		
		mTvAlarmIntro = (TextView) findViewById(R.id.tvNotificationIntro); 
		mTvAlarmIntro.setTypeface(mTypeface); 
		
		mSpnDayInterval = (Spinner) findViewById(R.id.spnDayInterval); 
		
		mSpnDaysInAdvance = (Spinner) findViewById(R.id.spnDaysInAdvance); 
		
		mSpnHoursInAdvance = (Spinner) findViewById(R.id.spnHoursInAdvance); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.alarm, menu);
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
