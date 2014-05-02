package com.sfsweep.android.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sfsweep.android.R;
import com.sfsweep.android.activities.MapActivity;
import com.sfsweep.android.adapters.NotifierIntervalAdapter;
import com.sfsweep.android.adapters.NotifierListAdapter;
import com.sfsweep.android.adapters.NotifierNumberAdapter;

public class Notifier extends LinearLayout {
	
	public final String NOTIFIER_LIST_PREFERENCES = "notifier_list_preferences" + Math.random(); // TODO: Figure out how really to avoid sharing the same SharedPreferences data among notifiers
	public final String SELECTED_INTERVAL = "notifier_interval_selection";
	public final String SELECTED_MINUTES  = "notifier_minutes_selection";
	public final String SELECTED_HOURS    = "notifier_hours_selection";
	public final String SELECTED_DAYS     = "notifier_days_selection";
	public final String IS_CHECKED        = "checkbox_is_checked";
	public final int    ALARM_REQUEST     = 1; 
    
    private static int sSystemCallsToOnItemSelected = 0; 	  // Flag neutralizes Android's firing onItemSelected() upon Spinner instantiation, rather than waiting for user interaction (see http://stackoverflow.com/questions/2562248/android-how-to-keep-onitemselected-from-firing-off-on-a-newly-instantiated-spin)
	
	private Activity mActivity; 
    private Spinner mSpnInterval; 							  // Lets user select a minute, hour or day interval for alarm notification. Determines range of values displayed by number spinner
	private Spinner mSpnNumber;								  // Lets user select a number value representing minutes, hours, or days
	private int mSelectedInterval;							  // Values for mSelectedInterval, mSelectedMinutes, mSelectedHours and mSelectedDays represent indices (e.g., to recover the actual minute value selected, add 1 to mSelectedMinutes)
	private int mSelectedMinutes;							 
	private int mSelectedHours;
	private int mSelectedDays; 
	private NotifierIntervalAdapter mNotifierIntervalAdapter;
	private NotifierNumberAdapter   mNotifierMinutesAdapter;
	private NotifierNumberAdapter   mNotifierHoursAdapter;
	private NotifierNumberAdapter   mNotifierDaysAdapter; 
	private NotifierListAdapter     mNotifierListAdapter; 
	private ListView mLvNotifiers; 
	private List mList;
	private ImageButton mBtnDeleteNotifier; 
	private CheckBox mCbActivateNotifier; 
	private SharedPreferences mPrefs; 
	private OnScheduleAlarmListener  mScheduleListener; 
	
	
	public Notifier(Context context) {
		super(context);  
	}
	
	public Notifier(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public Notifier(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public interface OnScheduleAlarmListener {
		public long onScheduleAlarm(); 
	}
	
	public void initializeNotifier(Activity activity, View v, OnScheduleAlarmListener scheduleListener,
			ListView listView, List list, NotifierListAdapter adapter) {
		mActivity            = activity;
		mScheduleListener    = scheduleListener; 
		mLvNotifiers         = listView; 
		mList                = list; 
		mNotifierListAdapter = adapter; 
		
		setupWidgets(v);
		setupListeners();
	}
	
	private void setupWidgets(View v) {
		mPrefs = mActivity.getSharedPreferences(NOTIFIER_LIST_PREFERENCES, 0); 
		
		// Set up interval spinner 
		mSpnInterval = (Spinner) v.findViewById(R.id.spnInterval); 
		ArrayList<CharSequence> list = createAdapterArray(R.array.spn_options_intervals);     	// Use of ArrayList enables mutability (e.g., changing interval label from singular to plural)
		mNotifierIntervalAdapter = new NotifierIntervalAdapter(mActivity, list, this); 
		mNotifierIntervalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
		mSpnInterval.setAdapter(mNotifierIntervalAdapter); 
		
			// Restore pre-selected interval, if any
		mSelectedInterval = mPrefs.getInt(SELECTED_INTERVAL, 0);
		mSpnInterval.setSelection(mSelectedInterval); 
		
		// Set up number spinner 
		mSpnNumber = (Spinner) v.findViewById(R.id.spnNumber); 
		updateNumberSpinner(); 				// Number spinner adjusted to reflect interval selection (viz., minutes, hours, days)
		
		// Set up delete button
		mBtnDeleteNotifier = (ImageButton) v.findViewById(R.id.btnDeleteNotifier); 
		mBtnDeleteNotifier.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Delete notifier (unless first)
//				if (mList.size() - 1 > 0) {
//					mList.remove(mLvNotifiers.getSelectedItemPosition()); 
//					mNotifierListAdapter.notifyDataSetChanged(); 
//				}
			}
		});
		
		// Set up check box
		mCbActivateNotifier = (CheckBox) v.findViewById(R.id.cbActivateNotifier); 
		boolean isChecked = mPrefs.getBoolean(IS_CHECKED, false);
		mCbActivateNotifier.setChecked(isChecked);
	}
	
	private ArrayList<CharSequence> createAdapterArray(int resourceId) {
		CharSequence[] values = mActivity.getResources().getStringArray(resourceId);
		ArrayList<CharSequence> list = new ArrayList<CharSequence>();
		list.addAll(Arrays.asList(values)); 
		return list; 
	}
	
	private void updateNumberSpinner() {
		// Retrieve pre-selected interval and create appropriate number adapter
		ArrayAdapter<CharSequence> adapter; 
		switch (mSelectedInterval) {
		case 0: 	// Minute-based interval (passim)
			if (mNotifierMinutesAdapter == null) {
				ArrayList<CharSequence> list = createAdapterArray(R.array.spn_options_minutes);
				mNotifierMinutesAdapter = new NotifierNumberAdapter(mActivity, list);  
				mNotifierMinutesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
			}
			adapter = mNotifierMinutesAdapter; 
			break;
		case 1:		// Hour-based interval (passim)
			if (mNotifierHoursAdapter == null) {
				ArrayList<CharSequence> list = createAdapterArray(R.array.spn_options_hours); 
				mNotifierHoursAdapter = new NotifierNumberAdapter(mActivity, list); 
				mNotifierHoursAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
			}
			adapter = mNotifierHoursAdapter;
			break;
		default: 	// Day-based interval (passim) 
			if (mNotifierDaysAdapter == null) {
				ArrayList<CharSequence> list = createAdapterArray(R.array.spn_options_days); 
				mNotifierDaysAdapter = new NotifierNumberAdapter(mActivity, list); 
				mNotifierDaysAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom); 
			}
			adapter = mNotifierDaysAdapter; 
		}
		mSpnNumber.setAdapter(adapter); 
		
			// Restore pre-selected number, if any
		mSelectedMinutes = mPrefs.getInt(SELECTED_MINUTES, 0);
		mSelectedHours   = mPrefs.getInt(SELECTED_HOURS, 0);
		mSelectedDays    = mPrefs.getInt(SELECTED_DAYS, 0); 
		int numberPosition = 0;
		switch (mSelectedInterval) {
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
		formatSpinners(); 
	}
	
	private void formatSpinners() {
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mSpnNumber.getLayoutParams();
		// TODO: Investigate invalidate() method
		
			// Calculate dp-to-pixel conversion factor
		DisplayMetrics metrics = new DisplayMetrics(); 
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics); 
		final float logicalDensity = metrics.density;
		int rightMarginPixels = (int) Math.ceil(4 * logicalDensity);	// dp values (here and in switch below) taken from trial-and-error results presented in res/layout/notifier 
			
			// Calculate and set margins
		switch (mSelectedInterval) {
		case 0:
			// FIXME: Formatting is correct only after (i) selecting a value (eg, "12 minutes"), (ii) selecting
			// a different interval (eg, "hours"), and then (iii) returning to the original interval 
			if (mSelectedMinutes == 0) {
				params.setMargins(-1 * (int) Math.ceil(11 * logicalDensity), 0, rightMarginPixels, 0); 
			} else if (0 < mSelectedMinutes && mSelectedMinutes < 9) {
				params.setMargins(-1 * (int) Math.ceil(11 * logicalDensity), 0, rightMarginPixels, 0);
			} else {
				params.setMargins((int) Math.ceil(5 * logicalDensity), 0, rightMarginPixels, 0); 
			}
			break;
		case 1:
			if (mSelectedHours == 0) { 
				params.setMargins((int) Math.ceil(10 * logicalDensity), 0, rightMarginPixels, 0); 
			} else if (0 < mSelectedHours && mSelectedHours < 9) {
				params.setMargins((int) Math.ceil(3 * logicalDensity), 0, rightMarginPixels, 0);
			} else {
				params.setMargins((int) Math.ceil(21 * logicalDensity), 0, rightMarginPixels, 0); 
			}
			break;
		default:
			if (mSelectedDays == 0) {
				params.setMargins((int) Math.ceil(15 * logicalDensity), 0, rightMarginPixels, 0); 
			} else if (0 < mSelectedDays && mSelectedDays < 9) {
				params.setMargins((int) Math.ceil(3 * logicalDensity), 0, rightMarginPixels, 0);
			} else {
				params.setMargins((int) Math.ceil(19 * logicalDensity), 0, rightMarginPixels, 0); 
			}
		}
		mSpnNumber.setLayoutParams(params); 
	}
	
	private void setupListeners() {
		mSpnInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position != mSelectedInterval) {
					// Save currently selected interval
					mSelectedInterval = position;
					mPrefs.edit().putInt(SELECTED_INTERVAL, position).commit(); 
					
					// Update number spinner values to match interval
					updateNumberSpinner(); 
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
		
		mCbActivateNotifier.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				if (isChecked) scheduleSystemAlarm();
//				else           cancelSystemAlarm();
//				reformatOnActiveStatusChange(isChecked);
			}
		});
	}
	
	public void reformatOnActiveStatusChange(boolean notifierActive) {
		TextView intervalTvSpnItem = mNotifierIntervalAdapter.getTvSpnItem();
		Context  intervalContext   = mNotifierIntervalAdapter.getAdapterContext(); 
		TextView numberTvSpnItem;
		Context  numberContext; 
		
		switch (mSelectedInterval) {
		case 0: 
			numberTvSpnItem = mNotifierMinutesAdapter.getTvSpnItem();
			numberContext   = mNotifierMinutesAdapter.getAdapterContext(); 
			break;
		case 1:
			numberTvSpnItem = mNotifierHoursAdapter.getTvSpnItem();
			numberContext   = mNotifierHoursAdapter.getAdapterContext(); 
			break;
		default:
			numberTvSpnItem = mNotifierDaysAdapter.getTvSpnItem();
			numberContext   = mNotifierDaysAdapter.getAdapterContext(); 
		}

		if (notifierActive) { 	
			intervalTvSpnItem.setTextColor(intervalContext.getResources().getColor(R.color.sfsweep_orange)); 
			numberTvSpnItem.setTextColor(numberContext.getResources().getColor(R.color.sfsweep_orange)); 
		} else {
			intervalTvSpnItem.setTextColor(intervalContext.getResources().getColor(R.color.platinum)); 
			numberTvSpnItem.setTextColor(numberContext.getResources().getColor(R.color.platinum));
		}
	}
	
	private void scheduleSystemAlarm() {		
//		Log.d("DEBUG", "******************* In scheduleSystemAlarm() *******************");
	
		// Get and parse street sweeping data
		long sweepStartDateInMillis = mScheduleListener.onScheduleAlarm();
		if (sweepStartDateInMillis == 0) return; 	// Abort if no parking history
//		Log.d("DEBUG", "sweepStartDateInMillis: " + sweepStartDateInMillis); 
		Date sweepStartDate = new Date(sweepStartDateInMillis); 
//		Log.d("DEBUG", "sweepStartDate: " + sweepStartDate); 
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sweepStartDate);
		int sweepHour = calendar.get(Calendar.HOUR_OF_DAY); 
		int sweepDay  = calendar.get(Calendar.DAY_OF_YEAR);
//		Log.d("DEBUG", "Calendar initially set at next sweep time: " + calendar.toString()); 
		
//		Log.d("DEBUG", "sweepHour is: " + sweepHour + ", and sweepDay is: " + sweepDay); 
		
		// Calculate actual value selected from number spinner (i.e., convert from index value)
		int selectedMinute = mSelectedMinutes + 1,
		    selectedHour   = mSelectedHours   + 1,
		    selectedDay    = mSelectedDays    + 1;
		
		// Calculate system alarm schedule
		int alarmMinute, alarmHour, alarmDay;  
		int defaultMinute = 0,						// Alarm defaults to on-the-hour timing for hour- and day-based notifications
		    defaultHour   = 12; 					// Alarm defaults to 12pm for day-based notifications
		
		switch (mSelectedInterval) {
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
//		Log.d("DEBUG", "mSelectedMinutes: " + mSelectedMinutes + ", mSelectedHours: " + mSelectedHours + ", mSelectedDays: " + mSelectedDays); 
//		Log.d("DEBUG", "selectedMinute: " + selectedMinute + ", selectedHour: " + selectedHour + ", selectedDay: " + selectedDay); 
//		Log.d("DEBUG", "sweepMinute: " + defaultMinute + ", sweepHour: " + sweepHour + ", sweepDay: " + sweepDay); 
//		Log.d("DEBUG", "alarmMinute: " + alarmMinute + ", alarmHour: " + alarmHour + ", alarmDay: " + alarmDay); 
		
		// Schedule system alarm 
		AlarmManager alarmMgr = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE); 
		Intent intent = new Intent(mActivity, MapActivity.class); 
		PendingIntent alarmIntent = PendingIntent.getBroadcast(mActivity, ALARM_REQUEST, intent,
				PendingIntent.FLAG_ONE_SHOT); 
		
		calendar.set(Calendar.DAY_OF_YEAR, alarmDay); 
		calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
		calendar.set(Calendar.MINUTE, alarmMinute); 
		
//		Log.d("DEBUG", "Alarm set at: " + calendar.toString()); 
		
		// FIXME: For some reason, Eclipse refuses to recognize alarmMgr.setExact(...), which
		// is the minSdkTarget API 19 update to alarmMgr.set(...). Unlike set(...), setExact(...) 
		// does not allow the system to adjust delivery time.
		alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent); 
		
		// TODO: Ensure system alarm persists if device is shut down
//		Log.d("DEBUG", "**************************************");
	}
	
	private void cancelSystemAlarm() {
		// TODO
	}
	
	public SharedPreferences getNotifierListPreferences() {
		return mPrefs; 
	}
	
	public int getSelectedInterval() {
		return mSelectedInterval; 
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
	
	public boolean getIsChecked() {
		return mCbActivateNotifier.isChecked();
	}

}
