package com.sfsweep.android.fragments;

import com.sfsweep.android.R;

public class MinuteNotificationFragment extends NotificationFragment {

	@Override
	public int getSpinnerValues() {
		return R.array.spn_options_minutes_in_advance; 
	}

}
