package com.sfsweep.android.fragments;

import java.util.Date;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sfsweep.android.R;
import com.sfsweep.android.StreetSweeperData;

/**
 * A placeholder fragment containing a simple view.
 */
public class SweepDataDetailFragment extends Fragment {

	private TextView tvStreetName;
	private TextView tvNextSweeping;

	public SweepDataDetailFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_sweep_data_detail,
				container, false);

		tvStreetName = (TextView) rootView.findViewById(R.id.tvStreetName);
		tvNextSweeping = (TextView) rootView
				.findViewById(R.id.tvNextSweeping);

		return rootView;
	}

	public void updateUi(StreetSweeperData d) {
		if (d.BlockSide.equals("")) {
			tvStreetName.setText(d.Corridor);
		} else {
			tvStreetName.setText(String.format("%s (%s)", d.Corridor,
					d.BlockSide));
		}

		Date nextSweeping = d.nextSweeping();
		tvNextSweeping.setText(String.format("%s %s-%s (%s)", d.WeekDay,
				d.FromHour, d.ToHour, DateUtils.getRelativeTimeSpanString(
						nextSweeping.getTime(), new Date().getTime(),
						DateUtils.MINUTE_IN_MILLIS)));
	}
}