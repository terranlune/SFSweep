package com.sfsweep.android.helpers;

import java.util.Locale;

/**
 * Helper class converts String-based sweep data into int values
 * 
 * Preconditions: 
 * (1) sweepTimeRange String must contain as its first substring a day of the week, a 
 * whitespace, and a single-or two-digit am or pm 12-hour clock time, in that order 
 * (e.g., "Friday 10 am"). It also cannot contain the sequence "am" or "pm" (in any 
 * capitalization) except with respect to its clock time references. 
 *
 * (2) daysToNextSweep String must contain a single numerical value, which represents 
 * an integer, and its terminal substring must be "days."  
 */
public class StreetSweeperDataConverter {

	private int mSweepTimeRangeAsInt,
	            mDaysToNextSweepAsInt; 
	
	public StreetSweeperDataConverter(String sweepTimeRange, String daysToNextSweep) {
		mSweepTimeRangeAsInt = convertTimeRangeToInt(sweepTimeRange); 
		mDaysToNextSweepAsInt = convertNextSweepToInt(daysToNextSweep); 
	}

	public StreetSweeperDataConverter(String sweepData) {
		sweepData = sweepData.trim(); 
		
		// Determine whether sweepData represents sweepTimeRange or daysToNextSweep
		if (sweepData.endsWith("days")) {
			mDaysToNextSweepAsInt = convertNextSweepToInt(sweepData); 
		} else {
			mSweepTimeRangeAsInt = convertTimeRangeToInt(sweepData); 
		}
	}
	
	private int convertTimeRangeToInt(String sweepTimeRange) {
		// Determine whether am or pm
		boolean isPm = false,
				isMidnight = false; 
		sweepTimeRange = sweepTimeRange.toLowerCase(Locale.US);
		if (!sweepTimeRange.contains("am")) {
			isPm = true; 
		} else if (sweepTimeRange.contains("pm") && 
				sweepTimeRange.indexOf("pm") < sweepTimeRange.indexOf("am")) {
			isPm = true; 
		} else if (sweepTimeRange.contains("12")) {
			isPm = false; 
			isMidnight = true; 
		}
		
		// Convert to int
		sweepTimeRange = sweepTimeRange.trim();
		int index = sweepTimeRange.indexOf(" ");
		String truncated = sweepTimeRange.substring(0, index + 3);	
		String numbersOnly = truncated.replaceAll("[^0-9]", ""); 
		int startSweepTime;
		try {
			startSweepTime = Integer.parseInt(numbersOnly); 
		} catch (NumberFormatException e) {
			startSweepTime = 0; 
		}
		
		// Convert to 24-hour clock 
		if (isPm || isMidnight) {
			startSweepTime += 12;
		}
		return startSweepTime; 
	}
	
	private int convertNextSweepToInt(String daysToNextSweep) {
		String numbersOnly = daysToNextSweep.replaceAll("[^0-9]", ""); 
		return Integer.parseInt(numbersOnly); 
	}
	
	public int getSweepTimeRangeAsInt() {
		return mSweepTimeRangeAsInt; 
	}
	
	public int getDaysToNextSweepAsInt() {
		return mDaysToNextSweepAsInt; 
	}
		
}
