package com.sfsweep.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sfsweep.android.R;

public abstract class NotificationFragment extends Fragment {

	private Spinner                    mSpnNotifier; 
	private ArrayAdapter<CharSequence> mAdapter;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState); 
		View v = inflater.inflate(R.layout.fragment_notification, parent, false); 
		
		setupWidgets(v); 
		setupListeners(); 
		return v;
	}
	
	private void setupWidgets(View v) {
		mSpnNotifier = (Spinner) v.findViewById(R.id.spnNotifier); 
		int spinnerValues = getSpinnerValues(); 
		mAdapter = ArrayAdapter.createFromResource(getActivity(), spinnerValues, 
				R.layout.spinner_item_custom);
		mAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom); 
		mSpnNotifier.setAdapter(mAdapter); 
	}
	
	private void setupListeners() {
		mSpnNotifier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override 
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO: Notify system alarm 
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		// TODO: Verify setOnItemLongClickListener() produces desired results (as opposed something 
		// like setOnLongClickListener() or a double-touch gesture.
		mSpnNotifier.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// Activate/deactivate spinner
				return true; 
			}
		});
	}
	
	/*
	 * Subclass should implement method to return resource value from R.java corresponding to 
	 * string array used to populate subclass's spinner
	 */
	public abstract int getSpinnerValues(); 
	
}
