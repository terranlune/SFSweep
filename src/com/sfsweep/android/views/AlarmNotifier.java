package com.sfsweep.android.views;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.sfsweep.android.R;
import com.sfsweep.android.activities.MapActivity;

public class AlarmNotifier extends Notifier {

	public static final String EXTRA_FROM_ALARM = "fromAlarm";
	public static final String EXTRA_NEXT_SWEEPING = "nextSweeping";

	public static final int ALARM_REQUEST = 1; 	
	
	private Activity mActivity; 
	private CheckBox mCbActivateNotifier;
	private AlarmManager mAlarmManager; 
	private PendingIntent mAlarmIntent;
	private OnScheduleAlarmListener mScheduleListener; 
	
	public interface OnScheduleAlarmListener {
		public long onScheduleAlarm(); 
	}
	
	public interface OnSetMoveByDayListener {
		public void onSetMoveByDay();
	}
	
	public AlarmNotifier(Context context) {
		super(context);  
	}
	
	public AlarmNotifier(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AlarmNotifier(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void initializeNotifier(Activity activity, View v) {
		super.initializeNotifier(activity, v); 
		mActivity = activity; 
		
		mCbActivateNotifier = (CheckBox) v.findViewById(R.id.cbActivateNotifier); 
		mCbActivateNotifier.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				updateActivationStatus(isChecked);
				if (isChecked) scheduleSystemAlarm();
				else           cancelSystemAlarm();
			}
		});
	}
	
	private void scheduleSystemAlarm() {		
			Log.d("DEBUG", "\n\n************* In scheduleSystemAlarm() *************");
	
		// Get and parse street sweeping data
		long sweepStartDateInMillis;
		try {
			sweepStartDateInMillis = mScheduleListener.onScheduleAlarm();
		} catch (NullPointerException e) {
			throw new NullPointerException("Must call setOnScheduleAlarmListener(...) "
					+ "on AlarmNotifier prior to attempting to schedule system alarm. " 
					+ e.getMessage());
		}
		if (sweepStartDateInMillis == 0) return; 	// Abort if no parking history
//			Log.d("DEBUG", "sweepStartDateInMillis: " + sweepStartDateInMillis); 
		Date sweepStartDate = new Date(sweepStartDateInMillis); 
//			Log.d("DEBUG", "sweepStartDate: " + sweepStartDate); 
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sweepStartDate);
		int sweepHour = calendar.get(Calendar.HOUR_OF_DAY); 
		int sweepDay  = calendar.get(Calendar.DAY_OF_YEAR);
		
//			Log.d("DEBUG", "Calendar initially set at next sweep time: " + calendar.toString()); 
		
//			Log.d("DEBUG", "sweepHour is: " + sweepHour + ", and sweepDay is: " + sweepDay); 
		
		// Calculate actual value selected from number spinner (i.e., convert from index value)
		int selectedMinute = getSelectedMinutes() + 1,
		    selectedHour   = getSelectedHours()   + 1,
		    selectedDay    = getSelectedDays()    + 1;
		
		// Calculate system alarm schedule
		int alarmMinute, alarmHour, alarmDay; 
		int defaultMinute = 0,					// Alarm defaults to on-the-hour timing for hour- and day-based notifications
		    defaultHour   = 12; 				// Alarm defaults to 12pm for day-based notifications
	
		switch (getSelectedInterval()) {
		case 0:
			alarmMinute = 60 - selectedMinute;
			if (sweepHour != 1)  alarmHour = sweepHour - 1;
			else                 alarmHour = 24;
			if (sweepHour != 24) alarmDay = sweepDay; 
			else                 alarmDay = sweepDay - 1; 
			break;
		case 1: 
			alarmMinute = defaultMinute;
			if (sweepHour < selectedHour) {
				alarmHour = 24 - (selectedHour - sweepHour); 
				alarmDay  = sweepDay - 1;
			}
			else if (sweepHour == selectedHour) {
				alarmHour = 24; 
				alarmDay  = sweepDay; 
			}
			else {
				alarmHour = sweepHour - selectedHour; 
				if (sweepHour != 24) alarmDay = sweepDay; 
				else                 alarmDay = sweepDay - 1;
			}
			break;
		default:
			alarmMinute = defaultMinute;
			alarmHour   = defaultHour;					
			alarmDay    = sweepDay - selectedDay; 
		}
//			Log.d("DEBUG", "getSelectedMinutes(): " + getSelectedMinutes() + ", getSelectedHours(): " + getSelectedHours() + ", getSelectedDays(): " + getSelectedDays()); 
//			Log.d("DEBUG", "selectedMinute: " + selectedMinute + ", selectedHour: " + selectedHour + ", selectedDay: " + selectedDay); 
			Log.d("DEBUG", "sweepMinute (=defaultMinute): " + defaultMinute + ", sweepHour: " + sweepHour + ", sweepDay: " + sweepDay); 
			Log.d("DEBUG", "alarmMinute: " + alarmMinute + ", alarmHour: " + alarmHour + ", alarmDay: " + alarmDay); 
		
		// Schedule system alarm 
			mAlarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE); 
			Intent intent = new Intent(mActivity, MapActivity.class); 
			intent.setAction(Intent.ACTION_MAIN); 
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.putExtra(EXTRA_FROM_ALARM, true);
			intent.putExtra(EXTRA_NEXT_SWEEPING, sweepStartDateInMillis);
			mAlarmIntent = PendingIntent.getActivity(mActivity, ALARM_REQUEST, intent,
					PendingIntent.FLAG_ONE_SHOT); 
			
			calendar.set(Calendar.DAY_OF_YEAR, alarmDay); 
			calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
			calendar.set(Calendar.MINUTE, alarmMinute); 
			
//			Log.d("DEBUG", "Alarm set at: " + calendar.toString()); 
			
			// Test with an alarm 30 seconds from now
			calendar = Calendar.getInstance();
			calendar.add(Calendar.SECOND, 10);
			Log.d("New time",calendar.toString());
			
			
			
			
			// FIXME: For some reason, Eclipse refuses to recognize alarmMgr.setExact(...), which
			// is the minSdkTarget API 19 update to alarmMgr.set(...). Unlike set(...), setExact(...) 
			// does not allow the system to adjust delivery time.
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), mAlarmIntent); 
			
			Log.d("DEBUG", "***********************************\n\n");
	}
	
	private void cancelSystemAlarm() {
		Log.d("DEBUG", "\n\n************* In cancelSystemAlarm() *************");
		if (mAlarmManager != null) {
			mAlarmManager.cancel(mAlarmIntent); 
		}
		
		Log.d("DEBUG", "***********************************\n\n");
	}	
	
	public void setOnScheduleAlarmListener(OnScheduleAlarmListener scheduleListener) {
		mScheduleListener = scheduleListener; 
	}
	
}
