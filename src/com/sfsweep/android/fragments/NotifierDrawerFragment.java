package com.sfsweep.android.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.sfsweep.android.R;
import com.sfsweep.android.adapters.NotifierListAdapter;
import com.sfsweep.android.views.Notifier.OnScheduleAlarmListener;

public class NotifierDrawerFragment extends Fragment {

	private TextView mTvNotificationHead;
    private TextView mTvNotificationTail;
	private ImageButton mBtnAddNotifier;
	private ListView mLvNotifiers;
	private ArrayList mNotifiersArray; 
	private NotifierListAdapter mNotifiersAdapter; 
	private OnScheduleAlarmListener mScheduleListener; 
	
	private String mFont="Roboto-Light.ttf";
	private Typeface mTypeface;
	
	
	// TODO: Consider moving interface enforcement to Notifier
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
		return v; 
	}
	
	private void setupWidgets(View v) {
		mTypeface = Typeface.createFromAsset(getActivity().getAssets(), mFont); 
		
		mTvNotificationHead = (TextView) v.findViewById(R.id.tvNotificationHead);
		mTvNotificationHead.setTypeface(mTypeface);
		
		mTvNotificationTail = (TextView) v.findViewById(R.id.tvNotificationTail);
		mTvNotificationTail.setTypeface(mTypeface); 
		
		mLvNotifiers = (ListView) v.findViewById(R.id.lvNotifierList); 
		mNotifiersArray = new ArrayList(); // TODO: Rethink array as needed
		mNotifiersArray.add(1); 
		mNotifiersAdapter = new NotifierListAdapter(getActivity(), mNotifiersArray, mLvNotifiers, mScheduleListener); 
		mLvNotifiers.setAdapter(mNotifiersAdapter); 
		
		mBtnAddNotifier = (ImageButton) v.findViewById(R.id.btnAddNotifier); 
		mBtnAddNotifier.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Add new notifier
				mNotifiersArray.add(1); 
				mNotifiersAdapter.notifyDataSetChanged(); 
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
		// TODO: Consider whether to save preferences and/or schedule alarm in onPause() or onStop()
		// TODO: Consider refactoring into an AsyncTask
		// Save selections of interval and number spinners
//		SharedPreferences.Editor editor = mNotifiersAdapter.getNotifierListPreferences().edit(); 
//		editor.putInt(NotifierListAdapter.SELECTED_INTERVAL, mNotifiersAdapter.getSelectedInterval()) 
//		      .putInt(NotifierListAdapter.SELECTED_MINUTES,  mNotifiersAdapter.getSelectedMinutes())
//		      .putInt(NotifierListAdapter.SELECTED_HOURS,  mNotifiersAdapter.getSelectedHours())
//		      .putInt(NotifierListAdapter.SELECTED_DAYS,  mNotifiersAdapter.getSelectedDays())
//		      .putBoolean(NotifierListAdapter.IS_CHECKED, mNotifiersAdapter.getIsChecked())
//		      .commit(); 
	}
	
}