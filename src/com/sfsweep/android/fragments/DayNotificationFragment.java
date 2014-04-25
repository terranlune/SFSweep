package com.sfsweep.android.fragments;

import com.sfsweep.android.R;

public class DayNotificationFragment extends NotificationFragment {

	@Override
	public int getSpinnerValues() {
		return R.array.spn_options_days_in_advance; 
	}

}
