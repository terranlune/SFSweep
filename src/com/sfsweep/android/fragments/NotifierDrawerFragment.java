package com.sfsweep.android.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sfsweep.android.R;
import com.sfsweep.android.views.AlarmNotifier;
import com.sfsweep.android.views.AlarmNotifier.OnScheduleAlarmListener;

public class NotifierDrawerFragment extends Fragment {
	
	private static final String NOTIFIER_PREFERENCES = "com.sfsweep.android.fragments.notifier_preferences";
	private static final String SELECTED_INTERVAL = "com.sfsweep.android.fragments.selected_interval";
	private static final String SELECTED_MINUTES = "com.sfsweep.android.fragments.selected_minutes";
	private static final String SELECTED_HOURS = "com.sfsweep.android.fragments.selected_hours";
	private static final String SELECTED_DAYS = "com.sfsweep.android.fragments.selected_days";
	
	private TextView mTvNotificationHead;
    private TextView mTvNotificationTail;
	private AlarmNotifier mNotifier; 
    private OnScheduleAlarmListener mScheduleListener; 
	private SharedPreferences mPrefs;
	
	private String mFont = "Roboto-Light.ttf";
	private Typeface mTypeface;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity); 
		
		if (activity instanceof OnScheduleAlarmListener) {
			mScheduleListener = (OnScheduleAlarmListener) activity; 
		} else {
			throw new ClassCastException(activity.toString() + " must implement "
					+ OnScheduleAlarmListener.class.getName()); 
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState); 
		View v = inflater.inflate(R.layout.fragment_notifier_drawer, parent, false); 
		
		setupWidgets(v); 
		restorePreferences(v);
		return v; 
	}
	
	private void setupWidgets(View v) {
		// Set up notification head and tail
		mTypeface = Typeface.createFromAsset(getActivity().getAssets(), mFont); 
		mTvNotificationHead = (TextView) v.findViewById(R.id.tvNotificationHead);
		mTvNotificationHead.setTypeface(mTypeface);
		
		mTvNotificationTail = (TextView) v.findViewById(R.id.tvNotificationTail);
		mTvNotificationTail.setTypeface(mTypeface); 
		
		// Set up notifier 
		mNotifier = (AlarmNotifier) v.findViewById(R.id.notifier); 
		mNotifier.setOnScheduleAlarmListener(mScheduleListener);
		mNotifier.initializeNotifier(getActivity(), v); 
	}
	
	private void restorePreferences(View v) {
		mPrefs = getActivity().getSharedPreferences(NOTIFIER_PREFERENCES, 0); 
		
		int selectedInterval = mPrefs.getInt(SELECTED_INTERVAL, 0);
		mNotifier.setSelectedInterval(selectedInterval); 
		
		int selectedMinutes = mPrefs.getInt(SELECTED_MINUTES, 0);
		mNotifier.setSelectedMinutes(selectedMinutes); 
		
		int selectedHours = mPrefs.getInt(SELECTED_HOURS, 0); 
		mNotifier.setSelectedHours(selectedHours); 
		
		int selectedDays = mPrefs.getInt(SELECTED_DAYS, 0); 
		mNotifier.setSelectedDays(selectedDays); 
		
		mNotifier.updateNotifier(v); 
	}

	@Override
	public void onStop() {
		super.onStop();
		
		// Save notifier values
		mPrefs.edit()
				.putInt(SELECTED_INTERVAL, mNotifier.getSelectedInterval())
				.putInt(SELECTED_MINUTES, mNotifier.getSelectedMinutes())
				.putInt(SELECTED_HOURS, mNotifier.getSelectedHours())
				.putInt(SELECTED_DAYS, mNotifier.getSelectedDays())
				.commit(); 
		// TODO: Consider refactoring into an AsyncTask
	}
	
	public OnScheduleAlarmListener getOnScheduleAlarmListener() {
		return mScheduleListener; 
	}
	
}