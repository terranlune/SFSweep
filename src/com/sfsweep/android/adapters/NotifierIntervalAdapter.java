package com.sfsweep.android.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sfsweep.android.R;
import com.sfsweep.android.fragments.NotifierFragment;

/**
 * Class implements an ArrayAdpater for notification intervals (viz., minutes, hours, days) displayed
 * by an associated spinner. Class allows for both plural and singular intervals (e.g., "minutes"  
 * and "minute")
 */
public class NotifierIntervalAdapter extends ArrayAdapter<CharSequence> {

	private NotifierFragment mNotifier;
	private TextView         mTvSpnItem; 
	
	private String   mFont = "Roboto-Light.ttf"; 
	private Typeface mTypeface;
	
	
	public NotifierIntervalAdapter(Context context, List<CharSequence> intervalList, NotifierFragment notifier) {
		super(context, 0, intervalList);
		mNotifier = notifier;
		mTypeface = Typeface.createFromAsset(context.getAssets(), mFont); 
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.spinner_item_custom, null); 
		}
		
		mTvSpnItem = (TextView) convertView.findViewById(R.id.tvSpnItem); 
		mTvSpnItem.setTypeface(mTypeface); 
		
		switch (position) {
		case 0:
			if (mNotifier.getSelectedMinutes() != 0) {
				mTvSpnItem.setText("minutes");
			} else {
				mTvSpnItem.setText("minute"); 
			}
			break;
		case 1: 
			if (mNotifier.getSelectedHours() != 0) {
				mTvSpnItem.setText("hours");
			} else {
				mTvSpnItem.setText("hour"); 
			}
			break;
		default:
			if (mNotifier.getSelectedDays() != 0) {
				mTvSpnItem.setText("days");
			} else {
				mTvSpnItem.setText("day"); 
			}
		}
		return convertView;
	}
	
}
