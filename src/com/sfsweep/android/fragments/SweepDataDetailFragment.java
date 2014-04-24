package com.sfsweep.android.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sfsweep.android.R;
import com.sfsweep.android.StreetSweeperData;
import com.sfsweep.android.StreetSweeperData.DateInterval;

/**
 * A placeholder fragment containing a simple view.
 */
public class SweepDataDetailFragment extends Fragment {

	private TextView tvStreetName;
	private TextView tvNextSweeping;
	private TextView tvSweepingInProgress;
	private StreetSweeperData data;

	public SweepDataDetailFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_sweep_data_detail,
				container, false);

		tvStreetName = (TextView) rootView.findViewById(R.id.tvStreetName);
		tvNextSweeping = (TextView) rootView.findViewById(R.id.tvNextSweeping);
		tvSweepingInProgress = (TextView) rootView
				.findViewById(R.id.tvSweepingInProgress);

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
		
		String range = new SimpleDateFormat("cccc ha", Locale.US).format(nextSweeping.start) + "-" +
				new SimpleDateFormat("ha", Locale.US).format(nextSweeping.end);
		
		String rel = (String) DateUtils
				.getRelativeTimeSpanString(
						nextSweeping.start.getTime(),
						new Date().getTime(),
						DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_DATE);
				
		tvNextSweeping.setText(String.format("%s (%s)", range, rel));
	}
}