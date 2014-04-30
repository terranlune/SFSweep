package com.sfsweep.android.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
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
	private Date mNextSweepStart;
	private String mDaysToNextSweep;

	private String mFont = "Roboto-Light.ttf";
	private Typeface mTypeface;
	private ImageView ivParkAction;
	private OnClickParkActionListener listener;
	private ImageView ivUnParkAction;

	public SweepDataDetailFragment() {
	}

	public interface OnClickParkActionListener {
		public void onClickParkAction();
		public void onClickUnParkAction();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnClickParkActionListener) {
			listener = (OnClickParkActionListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implement "
					+ OnClickParkActionListener.class.getName());
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
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

		ivParkAction = (ImageView) rootView.findViewById(R.id.ivParkAction);
		ivParkAction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickParkAction();
			}

		});
		ivUnParkAction = (ImageView) rootView.findViewById(R.id.ivUnParkAction);
		ivUnParkAction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickUnParkAction();
			}

		});

		tvSweepingInProgress = (TextView) rootView
				.findViewById(R.id.tvSweepingInProgress);
		tvSweepingInProgress.setTypeface(mTypeface);
		return rootView;
	}

	protected void updateButtonVisibility(boolean parked) {
		if (parked) {
			ivParkAction.setVisibility(View.INVISIBLE);
			ivUnParkAction.setVisibility(View.VISIBLE);
		}else{
			ivParkAction.setVisibility(View.VISIBLE);
			ivUnParkAction.setVisibility(View.INVISIBLE);
		}
	}
	
	protected void onClickParkAction() {
		if (listener != null) {
			listener.onClickParkAction();
		}
	}

	protected void onClickUnParkAction() {
		if (listener != null) {
			listener.onClickUnParkAction();
		}
	}

	public StreetSweeperData getData() {
		return this.data;
	}

	public void setData(StreetSweeperData d, boolean parked) {

		this.data = d;
		
		updateButtonVisibility(parked);

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

		mNextSweepStart = nextSweeping.start; 
		String range = new SimpleDateFormat("cccc ha", Locale.US)
				.format(mNextSweepStart)
				+ "-"
				+ new SimpleDateFormat("ha", Locale.US)
						.format(nextSweeping.end);

		mDaysToNextSweep = (String) DateUtils.getRelativeTimeSpanString(
				nextSweeping.start.getTime(), new Date().getTime(),
				DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_DATE);

		tvNextSweeping.setText(String.format("%s (%s)", range,
				mDaysToNextSweep));
	}

	public Date getNextSweepStart() {
		return mNextSweepStart;
	}

}