package com.sfsweep.android.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sfsweep.android.R;
import com.sfsweep.android.views.Notifier;
import com.sfsweep.android.views.Notifier.OnScheduleAlarmListener;

//TODO: Determine list parameterization
public class NotifierListAdapter extends ArrayAdapter<Notifier> {

	private OnScheduleAlarmListener mScheduleListener;
	private Activity mActivity;
	private List mList; 
	private ListView mLvNotifiers; 
	
    public NotifierListAdapter(Context context, List list, ListView listView, OnScheduleAlarmListener listener) {	
		super(context, 0, list);
		mActivity = (FragmentActivity) context; // TODO: Impose conditions
		mList = list; 
		mLvNotifiers = listView; 
		mScheduleListener = listener; 
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.view_notifier, parent, false);  // TODO: Consider .inflate(R.layout.view_notifier, null)
		}
		
		Notifier notifier = (Notifier) convertView.findViewById(R.id.notifier); 
		notifier.initializeNotifier(mActivity, convertView, mScheduleListener, mLvNotifiers, mList, this);
		
		return convertView;
	}
	
}
