package com.sfsweep.android.fragments;

import com.sfsweep.android.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NotificationHubFragment extends HubFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState); 
		
		setupWidgets(); 
		return getInflatedView(); 
	}
	
	private void setupWidgets() {
		getItemText().setText("Notifications"); 
		getItemImage().setImageResource(R.drawable.ic_notification_90dp); 
	}
	
	@Override 
	public String getHubFragmentType() {
		return this.toString(); 
	}
	
}
