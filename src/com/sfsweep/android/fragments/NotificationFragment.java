package com.sfsweep.android.fragments;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sfsweep.android.R;
import com.sfsweep.android.activities.MapActivity;
import com.sfsweep.android.helpers.SpinnerSelectionConverter;
import com.sfsweep.android.helpers.StreetSweeperDataConverter;

public abstract class NotificationFragment extends Fragment {

	private static final String SPINNER_PREFERENCES = "spinner_preferences"; 
	
	private Spinner mSpnNotifier; 
	private ArrayAdapter<CharSequence> mAdapter;
	private String mSpinnerSelectionKey; 
	private int mSpinnerValue;
	private OnScheduleAlarmCallbacks mListener; 
	
	public interface OnScheduleAlarmCallbacks {
		public String onSweepTimeRange(); 
		public String onDaysToNextSweep();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity); 
		
		if (activity instanceof OnScheduleAlarmCallbacks) {
			mListener = (OnScheduleAlarmCallbacks) activity; 
		} else {
			throw new ClassCastException(activity.toString() + " must implement "
					+ "OnScheduleAlarmCallBacks interface"); 
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState); 
		View v = inflater.inflate(R.layout.fragment_notification, parent, false); 
		
		setupWidgets(v); 
		setupListeners(); 
		return v;
	}
	
	private void setupWidgets(View v) {
		mSpnNotifier = (Spinner) v.findViewById(R.id.spnNotifier); 
		int spinnerValues = getSpinnerValues(); 
		mAdapter = ArrayAdapter.createFromResource(getActivity(), spinnerValues, 
				R.layout.spinner_item_custom);
		mAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom); 
		mSpnNotifier.setAdapter(mAdapter); 
		
		// Restore saved spinner values
		SharedPreferences prefs = getActivity().getSharedPreferences(SPINNER_PREFERENCES, 0); 
		mSpinnerSelectionKey = getSpinnerSelectionKey();
		int position = prefs.getInt(mSpinnerSelectionKey, 0); 
		mSpnNotifier.setSelection(position); 
	}
	
	private void setupListeners() {
		mSpnNotifier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override 
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mSpinnerValue = getSelectedSpinnerItem(position); 
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		// TODO: Verify setOnItemLongClickListener() produces desired results (as opposed something 
		// like setOnLongClickListener() or a double-touch gesture.
		mSpnNotifier.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// Activate/deactivate spinner
				return true; 
			}
		});
	}
	
	private int getSelectedSpinnerItem(int position) {
		String selection = (String) mSpnNotifier.getItemAtPosition(position); 
		SpinnerSelectionConverter converter = new SpinnerSelectionConverter(selection); 
		return converter.getSpinnerItemAsInt(); 
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		// TODO: Consider whether to save preferences and/or schedule alarm in onPause() or onStop()
		// TODO: Consider refactoring into an AsyncTask
		SharedPreferences prefs = getActivity().getSharedPreferences(SPINNER_PREFERENCES, 0); 
		SharedPreferences.Editor editor = prefs.edit(); 
		saveSpinnerSelection(editor, mSpnNotifier); 
		
		// TODO: Consider associating with a "set timer" button
		scheduleSystemAlarm(); 
	}
	
	private void scheduleSystemAlarm() {		
		// Get and parse street sweeping data 
		String timeRange  = mListener.onSweepTimeRange(),
		       daysToNext = mListener.onDaysToNextSweep(); 
		
		StreetSweeperDataConverter converter = new StreetSweeperDataConverter(timeRange, daysToNext); 	
		int sweepDay    = converter.getDaysToNextSweepAsInt(),
			sweepHour   = converter.getSweepTimeRangeAsInt(),
			sweepMinute = 0; 
		
		// Get and parse spinner value  
		int daysInAdvance    = 0,
		    hoursInAdvance   = 0,
		    minutesInAdvance = 0;
		
		if (mSpinnerSelectionKey.startsWith("day")) {
			daysInAdvance = mSpinnerValue; 
		} else if (mSpinnerSelectionKey.startsWith("hour")) {
			hoursInAdvance = mSpinnerValue;
		} else {
			minutesInAdvance = mSpinnerValue; 
		}
		
		// Calculate scheduling time for system alarm
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis()); 
		int alarmDay    = sweepDay - daysInAdvance + calendar.get(Calendar.DAY_OF_YEAR),
		    alarmHour   = sweepHour - hoursInAdvance,
		    alarmMinute = sweepMinute - minutesInAdvance;
		
		// Schedule system alarm 
		Activity context = getActivity(); 
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); 
		Intent intent = new Intent(context, MapActivity.class); 
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0); 
		
		calendar.set(Calendar.DAY_OF_YEAR, alarmDay); 
		calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
		calendar.set(Calendar.MINUTE, alarmMinute); 
		
		// FIXME: For some reason, Eclipse refuses to recognize alarmMgr.setExact(...), which
		// is the API 19 update to alarmMgr.set(...). Unlike set(...), setExact(...) does not
		// allow the system to adjust delivery time.
		alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent); 
		
		// TODO: Ensure system alarm persists if device is shut down
		// TODO: Enable user to cancel system alarm
		// TODO: Ensure re-selecting notifications cancels and replaces prior system alarm
		context = null; 
	}
	
	/*
	 * Subclass should return resource value from R.java corresponding to string array 
	 * used to populate subclass's spinner
	 */
	public abstract int getSpinnerValues(); 

	/*
	 * Subclass should use editor argument to persist its spinner value in SharedPreferences
	 */
	public abstract void saveSpinnerSelection(SharedPreferences.Editor editor, Spinner spinner); 
	
	/*
	 * Subclass should return String key to its spinner value stored in SharedPreferences
	 */
	public abstract String getSpinnerSelectionKey(); 
	
}
