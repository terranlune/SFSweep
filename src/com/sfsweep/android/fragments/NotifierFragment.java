package com.sfsweep.android.fragments;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.sfsweep.android.adapters.NotifierIntervalAdapter;
import com.sfsweep.android.helpers.StreetSweeperDataConverter;

public class NotifierFragment extends Fragment {

	private static final String SPINNER_PREFERENCES = "spinner_preferences"; 
    private static final String SELECTED_INTERVAL   = "spinner_interval_selection";
    private static final String SELECTED_MINUTES    = "spinner_minutes_selection";
    private static final String SELECTED_HOURS      = "spinner_hours_selection";
    private static final String SELECTED_DAYS       = "spinner_days_selection";
    
    private static int sSystemCallsToOnItemSelected = 0; 	// This (ungainly) flag counteracts Android's firing onItemSelected() upon Spinner instantiation, rather than waiting for user interaction (see http://stackoverflow.com/questions/2562248/android-how-to-keep-onitemselected-from-firing-off-on-a-newly-instantiated-spin)
	
	private Spinner mSpnInterval, 
	                mSpnNumber;
	private ArrayAdapter<CharSequence> mNotifierIntervalAdapter, 
	                                   mNotifierMinutesAdapter,
	                                   mNotifierHoursAdapter,
	                                   mNotifierDaysAdapter; 
	private String mSpinnerSelectionKey; 
	private int    mSelectedInterval, 
	               mSelectedMinutes,
	               mSelectedHours,
	               mSelectedDays; 
	private SharedPreferences mPrefs; 
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
		View v = inflater.inflate(R.layout.fragment_notifier, parent, false); 
		
		setupWidgets(v); 
		setupListeners(); 
		return v;
	}
	
	private void setupWidgets(View v) {
		// Set up interval spinner 
		mSpnInterval = (Spinner) v.findViewById(R.id.spnInterval); 
			// Implement using ArrayList to enable mutability (e.g., for purposes of changing 
			// interval from singular to plural)
		CharSequence[] intervals = {"minutes", "hours", "days"};
		ArrayList<CharSequence> list = new ArrayList<CharSequence>(); 
		list.addAll(Arrays.asList(intervals)); 
		mNotifierIntervalAdapter = new NotifierIntervalAdapter(getActivity(), list, this); 
		mNotifierIntervalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
		mSpnInterval.setAdapter(mNotifierIntervalAdapter); 
		
			// Restore pre-selected interval, if any
		mPrefs = getActivity().getSharedPreferences(SPINNER_PREFERENCES, 0); 
		int intervalPosition = mPrefs.getInt(SELECTED_INTERVAL, 0);
		mSpnInterval.setSelection(intervalPosition); 
		
		// Set up number spinner
		mSpnNumber = (Spinner) v.findViewById(R.id.spnNumber); 
		updateNumberSpinner(intervalPosition, mPrefs);
	}
	
	private void updateNumberSpinner(int intervalPosition, SharedPreferences prefs) {
		// Retrieve pre-selected interval and create appropriate number adapter
		ArrayAdapter<CharSequence> adapter; 
		switch (intervalPosition) {
		case 0: 
			if (mNotifierMinutesAdapter == null) {
				mNotifierMinutesAdapter = ArrayAdapter.createFromResource(getActivity(), 
					R.array.spn_options_minutes, R.layout.spinner_item_custom); 
				mNotifierMinutesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
			}
			adapter = mNotifierMinutesAdapter; 
			break;
		case 1: 
			if (mNotifierHoursAdapter == null) {
				mNotifierHoursAdapter = ArrayAdapter.createFromResource(getActivity(), 
					R.array.spn_options_hours, R.layout.spinner_item_custom);
				mNotifierHoursAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
			}
			adapter = mNotifierHoursAdapter;
			break;
		default: 
			if (mNotifierDaysAdapter == null) {
				mNotifierDaysAdapter = ArrayAdapter.createFromResource(getActivity(), 
					R.array.spn_options_days, R.layout.spinner_item_custom); 
				mNotifierDaysAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom); 
			}
			adapter = mNotifierDaysAdapter; 
		}
		mSpnNumber.setAdapter(adapter); 
		
			// Restore pre-selected number, if any
		mSelectedMinutes = prefs.getInt(SELECTED_MINUTES, 0);
		mSelectedHours   = prefs.getInt(SELECTED_HOURS, 0);
		mSelectedDays    = prefs.getInt(SELECTED_DAYS, 0); 
		int numberPosition = 0;
		switch (intervalPosition) {
		case 0:
			numberPosition = mSelectedMinutes;
			break;
		case 1: 
			numberPosition = mSelectedHours;
			break;
		default:
			numberPosition = mSelectedDays; 
		}
		mSpnNumber.setSelection(numberPosition); 
		adapter.notifyDataSetChanged(); 
	}
	
	private void setupListeners() {
		mSpnInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position != mSelectedInterval) {
					// Save currently selected interval
					mSelectedInterval = position;
					mPrefs.edit().putInt(SELECTED_INTERVAL, mSelectedInterval).commit(); 
					
					// Update number spinner values to match interval
					updateNumberSpinner(position, mPrefs); 
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
					
		mSpnNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override 
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// Save currently selected number
				switch (mSelectedInterval) {
				case 0:
					if (sSystemCallsToOnItemSelected > 1 || 
							(sSystemCallsToOnItemSelected == 1 && mSelectedMinutes == 0)) {		// When app is first installed, Android calls onItemSelected() once upon creation, prior to user interaction (hence the second disjunctive condition). Thereafter, Android calls onItemSelected() twice prior to user interaction 
						mSelectedMinutes = position;
					}
					sSystemCallsToOnItemSelected++; 
					mPrefs.edit().putInt(SELECTED_MINUTES, mSelectedMinutes).commit();
					break;
				case 1: 
					mSelectedHours = position;
					mPrefs.edit().putInt(SELECTED_HOURS, mSelectedHours).commit();
					break;
				default:
					mSelectedDays = position;
					mPrefs.edit().putInt(SELECTED_DAYS, mSelectedDays).commit(); 
				}
				mNotifierIntervalAdapter.notifyDataSetChanged(); 	// Update NotifierIntervalAdapter from singular to plural and vice versa as needed
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		// TODO: Consider whether to save preferences and/or schedule alarm in onPause() or onStop()
		// TODO: Consider refactoring into an AsyncTask
		// Save selections of interval and number spinners
		SharedPreferences prefs = getActivity().getSharedPreferences(SPINNER_PREFERENCES, 0); 
		SharedPreferences.Editor editor = prefs.edit(); 
		editor.putInt(SELECTED_INTERVAL, mSelectedInterval) 
		      .putInt(SELECTED_MINUTES, mSelectedMinutes)
		      .putInt(SELECTED_HOURS, mSelectedHours)
		      .putInt(SELECTED_DAYS, mSelectedDays)
		      .commit(); 
		
		// TODO: Consider associating with a "set timer" button
		// TODO: Update scheduleSystemAlarm() in line with revised notifiers
//		scheduleSystemAlarm(); 
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
			daysInAdvance = mSelectedMinutes; 
		} else if (mSpinnerSelectionKey.startsWith("hour")) {
			hoursInAdvance = mSelectedMinutes;
		} else {
			minutesInAdvance = mSelectedMinutes; 
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
		// is the minSdkTarget API 19 update to alarmMgr.set(...). Unlike set(...), setExact(...) 
		// does not allow the system to adjust delivery time.
		alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent); 
		
		// TODO: Ensure system alarm persists if device is shut down
		// TODO: Enable user to cancel system alarm
		// TODO: Ensure re-selecting notifications cancels and replaces prior system alarm
		context = null; 
	}
	
	public int getSelectedMinutes() {
		return mSelectedMinutes;
	}
	
	public int getSelectedHours() {
		return mSelectedHours;
	}
	
	public int getSelectedDays() {
		return mSelectedDays;
	}
	
}
