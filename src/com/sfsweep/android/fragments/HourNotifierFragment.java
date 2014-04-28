package com.sfsweep.android.fragments;

import android.content.SharedPreferences;
import android.widget.Spinner;

import com.sfsweep.android.R;

public class HourNotifierFragment extends NotifierFragment {

	private static final String HOUR_SPINNER_POSITION = "hour_spinner_position"; 
	
	@Override
	public int getSpinnerValues() {
		return R.array.spn_options_hours_in_advance;
	}
	
	@Override
	public void saveSpinnerSelection(SharedPreferences.Editor editor, Spinner spinner) {
		editor.putInt(HOUR_SPINNER_POSITION, spinner.getSelectedItemPosition());
		editor.commit(); 
	}

	@Override
	public String getSpinnerSelectionKey() {
		return HOUR_SPINNER_POSITION;
	}
	
}
