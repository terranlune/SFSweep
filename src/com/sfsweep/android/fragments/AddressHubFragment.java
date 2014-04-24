package com.sfsweep.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AddressHubFragment extends HubFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState); 
		getItemText().setText("Stub"); 
		return getInflatedView(); 
	}
	
	@Override 
	public String getHubFragmentType() {
		return this.toString(); 
	}
	
}
