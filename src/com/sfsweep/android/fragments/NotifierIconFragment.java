package com.sfsweep.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.sfsweep.android.R;

public class NotifierIconFragment extends Fragment {

	private ImageButton mBtnNotify; 
	private OnNotifierIconClickListener mListener; 
	
	
	public interface OnNotifierIconClickListener {
		public void onNotifierIconClick(); 
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity); 
		
		if (activity instanceof OnNotifierIconClickListener) {
			mListener = (OnNotifierIconClickListener) activity;
		} else {
			throw new ClassCastException(activity.toString() + " must implement "
					+ "OnNotifierIconClicked interface"); 
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState);
		View v = inflater.inflate(R.layout.fragment_notifier_icon, parent, false); 
		
		setupWidgets(v);
		return v; 
	}
	
	private void setupWidgets(View v) {
		mBtnNotify = (ImageButton) v.findViewById(R.id.btnNotify);
		mBtnNotify.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onNotifierIconClick(); 
			}
		});
	}
	
}
