package com.sfsweep.android.helpers;

/**
 * Helper class converts a String-based Spinner item to an int. Class implementation 
 * assumes that the String may represent, among other things, a specified number of days
 * or weeks
 * 
 * Preconditions: Spinner String must contain only one numerical value, which represents
 * an integer. Only Strings whose numerical value represents weeks contain the substring
 * "week"
 */
public class SpinnerSelectionConverter {

	private int mSelectionAsInt; 
	
	public SpinnerSelectionConverter(String spinnerSelection) {
		mSelectionAsInt = convertToInt(spinnerSelection); 
	}

	private int convertToInt(String selection) {
		// Distinguish between days and weeks
		boolean isWeek = false; 
		if (selection.contains("week")) {
			isWeek = true; 
		}
		
		String selectionNumbersOnly = selection.replaceAll("[^0-9]", ""); 
		int spinnerValue = Integer.parseInt(selectionNumbersOnly); 
		
		if (isWeek) {
			spinnerValue = spinnerValue * 7;
		}
		return spinnerValue; 
	}
	
	public int getSpinnerItemAsInt() {
		return mSelectionAsInt; 
	}
	
}
