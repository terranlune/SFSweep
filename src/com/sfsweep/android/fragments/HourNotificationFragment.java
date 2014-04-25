package com.sfsweep.android.fragments;

import com.sfsweep.android.R;


public class HourNotificationFragment extends NotificationFragment {

	@Override
	public int getSpinnerValues() {
		return R.array.spn_options_hours_in_advance;
	}

}
