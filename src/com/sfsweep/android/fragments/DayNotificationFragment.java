package com.sfsweep.android.fragments;

import android.content.SharedPreferences;
import android.widget.Spinner;

import com.sfsweep.android.R;

public class DayNotificationFragment extends NotificationFragment {

	private static final String DAY_SPINNER_POSITION = "day_spinner_position"; 
	
	@Override
	public int getSpinnerValues() {
		return R.array.spn_options_days_in_advance; 
	}

	@Override
	public void saveSpinnerSelection(SharedPreferences.Editor editor, Spinner spinner) {
		editor.putInt(DAY_SPINNER_POSITION, spinner.getSelectedItemPosition());
		editor.commit(); 
	}

	@Override
	public String getSpinnerSelectionKey() {
		return DAY_SPINNER_POSITION;
	}
	
}
