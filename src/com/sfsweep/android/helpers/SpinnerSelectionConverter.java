package com.sfsweep.android.helpers;

import android.util.Log;

/**
 * Class converts a String-based Spinner item to an int
 * 
 * Preconditions: Spinner String must contain only one numerical value, which represents
 * and integer
 */
public class SpinnerSelectionConverter {

	private int mSelectionAsInt; 
	
	public SpinnerSelectionConverter(String spinnerSelection) {
		mSelectionAsInt = convertToInt(spinnerSelection); 
	}

	private int convertToInt(String selection) {
		String selectionNumbersOnly = selection.replaceAll("[^0-9]", ""); 
		return Integer.parseInt(selectionNumbersOnly); 
	}
	
	public int getSpinnerItemAsInt() {
		return mSelectionAsInt; 
	}
	
}
