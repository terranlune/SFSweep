package com.sfsweep.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sfsweep.android.R;

public class NotificationIconFragment extends Fragment {

	private Button mBtnNotify; 
	private OnNotificationIconClickListener mListener; 
	
	
	public interface OnNotificationIconClickListener {
		public void onNotificationIconClick(); 
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity); 
		
		if (activity instanceof OnNotificationIconClickListener) {
			mListener = (OnNotificationIconClickListener) activity;
		} else {
			throw new ClassCastException(activity.toString() + " must implement "
					+ "OnNotificationIconClicked interface"); 
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState);
		View v = inflater.inflate(R.layout.fragment_notification_icon, parent, false); 
		
		setupWidgets(v);
		return v; 
	}
	
	private void setupWidgets(View v) {
		mBtnNotify = (Button) v.findViewById(R.id.btnNotify);
		mBtnNotify.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onNotificationIconClick(); 
			}
		});
	}
	
}
