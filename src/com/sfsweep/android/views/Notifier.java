package com.sfsweep.android.views;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.sfsweep.android.R;
import com.sfsweep.android.adapters.NotifierIntervalAdapter;
import com.sfsweep.android.adapters.NotifierNumberAdapter;

public class Notifier extends LinearLayout {
	
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
	private CheckBox mCbActivateNotifier; 

	/*
	 * Constructors make calls to superclass as specified by definition of LinearLayout
	 */
	public Notifier(Context context) {
		super(context);  
	}
	
	public Notifier(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public Notifier(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void initializeNotifier(Activity activity, View v) {
		mActivity = activity;
		
		setupWidgets(v);
		setupListeners();
	}
	
	public void updateNotifier(View v) {
		setupWidgets(v); 
	}
	
	private void setupWidgets(View v) {
//		// Set up interval spinner 
//		mSpnInterval = (Spinner) v.findViewById(R.id.spnInterval); 
//		ArrayList<CharSequence> list = createAdapterArray(R.array.spn_options_intervals);     	// Use of ArrayList enables mutability (e.g., changing interval label from singular to plural)
//		mNotifierIntervalAdapter = new NotifierIntervalAdapter(mActivity, list, this); 
//		mNotifierIntervalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
//		mSpnInterval.setAdapter(mNotifierIntervalAdapter); 
//		
//		// Set up number spinner 
//		mSpnNumber = (Spinner) v.findViewById(R.id.spnNumber); 
//		updateNumberSpinner(); 				// Number spinner adjusted to reflect interval selection (viz., minutes, hours, days)
//		
//		// Set up check box
//		mCbActivateNotifier = (CheckBox) v.findViewById(R.id.cbActivateNotifier); 
//		mCbActivateNotifier.setChecked(false); 
	}
	
	private ArrayList<CharSequence> createAdapterArray(int resourceId) {
		CharSequence[] values = mActivity.getResources().getStringArray(resourceId);
		ArrayList<CharSequence> list = new ArrayList<CharSequence>();
		list.addAll(Arrays.asList(values)); 
		return list; 
	}
	
	private void updateNumberSpinner() {
		// Retrieve pre-selected interval and associate appropriate number adapter
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
	}
	
	private void setupListeners() {
		mSpnInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { 
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					Log.d("DEBUG", "********** Inside mSpnInterval.onItemSelected **********");
					Log.d("DEBUG", "mSelectedInterval initially is: " + mSelectedInterval); 
					
				if (position != mSelectedInterval) {
					// Save current selection 
						Log.d("DEBUG", "Updated mSelectedInterval is: " + mSelectedInterval); 
					
						switch (position) {
						case 0: 
							// TODO persist to SP
							break;
						case 1:
							// TODO persist to SP
							break;
						default:
							// TODO persist to SP
						}
					mSelectedInterval = position;
					
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
					Log.d("DEBUG", "********** Inside mSpnNumber.onItemSelected **********");
					Log.d("DEBUG", "mSelectedInterval is: " + mSelectedInterval);
					
				// Update currently selected number 
				switch (mSelectedInterval) {
				case 0:
					if ((position != mSelectedMinutes) && (sSystemCallsToOnItemSelected > 1 || 
							(sSystemCallsToOnItemSelected == 1 && mSelectedMinutes == 0))) {		// When app is first installed, Android calls onItemSelected() once upon creation, prior to user interaction (hence the second disjunctive condition). Thereafter, Android calls onItemSelected() twice prior to user interaction 
						mSelectedMinutes = position;
							Log.d("DEBUG", "mSelectedMinutes is: " + mSelectedMinutes); 
					}
					sSystemCallsToOnItemSelected++; 
					break;
				case 1: 
					if (position != mSelectedHours) {
						mSelectedHours = position;
					}
						Log.d("DEBUG", "mSelectedHours is: " + mSelectedHours); 
					break;
				default:
					if (position != mSelectedDays) {
						mSelectedDays = position;
					}
						Log.d("DEBUG", "mSelectedDays is: " + mSelectedDays); 
				}
				
				// Update NotifierIntervalAdapter from singular to plural and vice versa as needed
				mNotifierIntervalAdapter.notifyDataSetChanged(); 	
				
				// Save current selection
					// TODO: Persist to SP
				}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		mCbActivateNotifier.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				reformatOnActiveStatusChange(isChecked);
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
	
	public void updateActivationStatus(boolean active) {
		reformatOnActiveStatusChange(active); 
	}
	
	public Spinner getIntervalSpinner() {
		return mSpnInterval;
	}
	
	public Spinner getNumberSpinner() {
		return mSpnNumber; 
	}
	
	public CheckBox getCheckBox() {
		return mCbActivateNotifier; 
	}
	
	public void setCheckBox(boolean isChecked) {
		mCbActivateNotifier.setChecked(isChecked); 
	}

	public int getSelectedInterval() {
		return mSelectedInterval; 
	}
	
	public void setSelectedInterval(int selectedInterval) {
		mSelectedInterval = selectedInterval;
	}
	
	public int getSelectedMinutes() {
		return mSelectedMinutes;
	}
	
	public void setSelectedMinutes(int selectedMinutes) {
		mSelectedMinutes = selectedMinutes;
	}
	
	public int getSelectedHours() {
		return mSelectedHours;
	}
	
	public void setSelectedHours(int selectedHours) {
		mSelectedHours = selectedHours;
	}
	
	public int getSelectedDays() {
		return mSelectedDays;
	}

	public void setSelectedDays(int selectedDays) {
		mSelectedDays = selectedDays;
	}
	
	public boolean getIsChecked() {
		return mCbActivateNotifier.isChecked();
	}
	
}