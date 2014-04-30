package com.sfsweep.android.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sfsweep.android.R;
import com.sfsweep.android.models.StreetSweeperData;
import com.sfsweep.android.models.StreetSweeperData.DateInterval;

/**
 * A placeholder fragment containing a simple view.
 */
public class SweepDataDetailFragment extends Fragment {

	private TextView tvStreetName;
	private TextView tvNextSweeping;
	private TextView tvSweepingInProgress;
	private StreetSweeperData data;
	private String mSweepTimeRange; 
	private String mDaysToNextSweep;

	private String   mFont="Roboto-Light.ttf";
	private Typeface mTypeface;
	
	public SweepDataDetailFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_sweep_data_detail,
				container, false);

		mTypeface = Typeface.createFromAsset(getActivity().getAssets(), mFont); 
		
		tvStreetName = (TextView) rootView.findViewById(R.id.tvStreetName);
		tvStreetName.setTypeface(mTypeface); 
		
		tvNextSweeping = (TextView) rootView.findViewById(R.id.tvNextSweeping);
		tvNextSweeping.setTypeface(mTypeface);
		
		tvSweepingInProgress = (TextView) rootView
				.findViewById(R.id.tvSweepingInProgress);
		tvSweepingInProgress.setTypeface(mTypeface); 
		return rootView;
	}

	public StreetSweeperData getData() {
		return this.data;
	}
	
	public void setData(StreetSweeperData d) {
		
		this.data = d;
		
		if (d.BlockSide.equals("")) {
			tvStreetName.setText(d.Corridor);
		} else {
			tvStreetName.setText(String.format("%s (%s)", d.Corridor,
					d.BlockSide));
		}

		// TODO: Refactor some of this into StreetSweeperData
		List<DateInterval> upcomingSweepings = d.upcomingSweepings();
		DateInterval nextSweeping = upcomingSweepings.get(0);
		if (nextSweeping.start.before(new Date())) {
			tvSweepingInProgress.setText(String.format(
					"Sweeping in progress!\n(ends %s)", DateUtils
							.getRelativeTimeSpanString(
									nextSweeping.end.getTime(),
									new Date().getTime(),
									DateUtils.MINUTE_IN_MILLIS)));
			tvSweepingInProgress.setVisibility(View.VISIBLE);

			try {
				nextSweeping = upcomingSweepings.get(1);
			} catch (IndexOutOfBoundsException e) {
				// This is a weird edge case, so just show the sweep we know
				// about
			}
		} else {
			tvSweepingInProgress.setVisibility(View.GONE);
		}
		
		/* [John: I enlarged the scope and made getters for the range (now mSweepTimeRange) 
		 * and rel (now mDaysToNextSweep) variables in order to set the system alarm. -Adam]
		 */
		mSweepTimeRange = new SimpleDateFormat("cccc ha", Locale.US).format(nextSweeping.start) + "-" +
				new SimpleDateFormat("ha", Locale.US).format(nextSweeping.end);
		
		mDaysToNextSweep = (String) DateUtils
				.getRelativeTimeSpanString(
						nextSweeping.start.getTime(),
						new Date().getTime(),
						DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_DATE);
				
		tvNextSweeping.setText(String.format("%s (%s)", mSweepTimeRange, mDaysToNextSweep));
	}
	
	public String getSweepTimeRange() {
		return mSweepTimeRange;
	}
	
	public String getDaysToNextSweep() {
		return mDaysToNextSweep; 
	}
	
}