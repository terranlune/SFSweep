package com.sfsweep.android.fragments;

import android.content.SharedPreferences;
import android.widget.Spinner;

import com.sfsweep.android.R;

public class MinuteNotificationFragment extends NotificationFragment {

	private static final String MINUTE_SPINNER_POSITION = "minute_spinner_position";
	
	@Override
	public int getSpinnerValues() {
		return R.array.spn_options_minutes_in_advance; 
	}
	
	@Override
	public void saveSpinnerSelection(SharedPreferences.Editor editor, Spinner spinner) {
		editor.putInt(MINUTE_SPINNER_POSITION, spinner.getSelectedItemPosition());
		editor.commit(); 
	}

	@Override
	public String getSpinnerSelectionKey() {
		return MINUTE_SPINNER_POSITION;
	}
	
}
