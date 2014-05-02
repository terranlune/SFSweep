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

/**
 * Class implements an ArrayAdpater for notification intervals (viz., minutes, hours, days) displayed
 * by an associated spinner. Class allows for both plural and singular intervals (e.g., "minutes"  
 * and "minute")
 */
public class NotifierNumberAdapter extends ArrayAdapter<CharSequence> {

	private Context  mContext; 
	private List<CharSequence> mNumberList; 
	private TextView mTvSpnItem; 
	
	private String   mFont = "Roboto-Light.ttf"; 
	private Typeface mTypeface;
	
	
	public NotifierNumberAdapter(Context context, List<CharSequence> numberList) {
		super(context, 0, numberList);
		mNumberList = numberList; 
		mTypeface = Typeface.createFromAsset(context.getAssets(), mFont); 
		mContext  = context; 
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.spinner_item_custom, parent, false); 
		}
		
		mTvSpnItem = (TextView) convertView.findViewById(R.id.tvSpnItem); 
		mTvSpnItem.setTypeface(mTypeface); 
		mTvSpnItem.setText(mNumberList.get(position)); 
		return convertView; 
	}
	
	public TextView getTvSpnItem() {
		return mTvSpnItem;
	}
	
	public Context getAdapterContext() {
		return mContext; 
	}
	
}