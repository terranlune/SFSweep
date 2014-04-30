package com.sfsweep.android.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sfsweep.android.R;

public class NotifierDrawerFragment extends Fragment {

	private TextView    mTvNotificationHead,
                        mTvNotificationTail;
	private ImageButton mBtnAddNotifier,
	                    mBtnSubtractNotifier;
	
	private String   mFont="Roboto-Light.ttf";
	private Typeface mTypeface;
	
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
		
		mBtnAddNotifier = (ImageButton) v.findViewById(R.id.btnAddNotifier); 
		mBtnAddNotifier.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Add new notifier
			}
		});
		
		mBtnSubtractNotifier = (ImageButton) v.findViewById(R.id.btnSubtractNotifier); 
		mBtnSubtractNotifier.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Delete notifier after dialog (unless first) 
			}
		});
	}

}