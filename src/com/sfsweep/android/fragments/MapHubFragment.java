package com.sfsweep.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sfsweep.android.R;

public class MapHubFragment extends HubFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState); 
		
		setupWidgets();
		return getInflatedView(); 
	}
	
	private void setupWidgets() {
		getItemText().setText("Map"); 
		getItemImage().setImageResource(R.drawable.ic_map_90dp); 
	}
	
	@Override 
	public String getHubFragmentType() {
		return this.toString(); 
	}
	
}
