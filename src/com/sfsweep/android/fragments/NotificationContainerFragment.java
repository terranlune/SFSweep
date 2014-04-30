package com.sfsweep.android.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sfsweep.android.R;

public class NotificationContainerFragment extends Fragment {

	private TextView mTvNotificationHead,
                     mTvNotificationTail;
	
	private String   mFont="Roboto-Light.ttf";
	private Typeface mTypeface;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState); 
		View v = inflater.inflate(R.layout.fragment_notification_drawer, parent, false); 
		
		setupWidgets(v); 
		return v; 
	}
	
	private void setupWidgets(View v) {
		mTypeface = Typeface.createFromAsset(getActivity().getAssets(), mFont); 
		
		mTvNotificationHead = (TextView) v.findViewById(R.id.tvNotificationHead);
		mTvNotificationHead.setTypeface(mTypeface);
		
		mTvNotificationTail = (TextView) v.findViewById(R.id.tvNotificationTail);
		mTvNotificationTail.setTypeface(mTypeface); 
	}
}