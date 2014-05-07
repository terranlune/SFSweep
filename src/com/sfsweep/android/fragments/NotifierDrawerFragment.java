package com.sfsweep.android.fragments;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.sfsweep.android.R;
import com.sfsweep.android.activities.MapActivity;
import com.sfsweep.android.views.AlarmNotifier;
import com.sfsweep.android.views.AlarmNotifier.OnScheduleAlarmListener;

public class NotifierDrawerFragment extends Fragment implements
		OnSeekBarChangeListener {

	private static final String NOTIFIER_PREFERENCES = "com.sfsweep.android.fragments.notifier_preferences";
	private static final String SELECTED_INTERVAL = "com.sfsweep.android.fragments.selected_interval";
	private static final String SELECTED_MINUTES = "com.sfsweep.android.fragments.selected_minutes";
	private static final String SELECTED_HOURS = "com.sfsweep.android.fragments.selected_hours";
	private static final String SELECTED_DAYS = "com.sfsweep.android.fragments.selected_days";
	private static final String MILLIS_BEFORE_SWEEPING = "millis_before_sweeping";
	private static final String PROGRESS_BEFORE_SWEEPING = "progress_before_sweeping";

	public static final int ALARM_REQUEST = 1;

	public static final String EXTRA_FROM_ALARM = "fromAlarm";
	public static final String EXTRA_NEXT_SWEEPING = "nextSweeping";

	private TextView mTvNotificationHead;
	private TextView mTvNotificationTail;
	private AlarmNotifier mNotifier;
	private OnScheduleAlarmListener mScheduleListener;
	private SharedPreferences mPrefs;

	private String mFont = "Roboto-Light.ttf";
	private Typeface mTypeface;
	private SeekBar mSbAlarm;
	private PendingIntent mAlarmIntent;
	private AlarmManager mAlarmManager;
	private long millisBeforeSweeping;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof OnScheduleAlarmListener) {
			mScheduleListener = (OnScheduleAlarmListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implement "
					+ OnScheduleAlarmListener.class.getName());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState);
		View v = inflater.inflate(R.layout.fragment_notifier_drawer, parent,
				false);

		setupWidgets(v);
		restorePreferences(v);
		return v;
	}

	private void setupWidgets(View v) {
		// Set up notification head and tail
		mTypeface = Typeface.createFromAsset(getActivity().getAssets(), mFont);
		// mTvNotificationHead = (TextView)
		// v.findViewById(R.id.tvNotificationHead);
		// mTvNotificationHead.setTypeface(mTypeface);

		mTvNotificationTail = (TextView) v
				.findViewById(R.id.tvNotificationTail);
		mTvNotificationTail.setTypeface(mTypeface);

		// Seekbar
		mSbAlarm = (SeekBar) v.findViewById(R.id.sbAlarm);
		mSbAlarm.setOnSeekBarChangeListener(this);

	}

	private void restorePreferences(View v) {
		mPrefs = getActivity().getSharedPreferences(NOTIFIER_PREFERENCES, 0);

		mSbAlarm.setProgress((int) mPrefs.getLong(PROGRESS_BEFORE_SWEEPING, 0));
	}

	public OnScheduleAlarmListener getOnScheduleAlarmListener() {
		return mScheduleListener;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		double x = 1.0f * progress / seekBar.getMax();
		double g = 0.0001 + Math.pow(x, 5);
		millisBeforeSweeping = Math.round(g * 1000 * 60 * 60 * 24 * 7);

		Resources res = getResources();
		if (progress == 0) {
			millisBeforeSweeping = 0;
			mTvNotificationTail.setText(R.string.alarm_off);
		} else if (millisBeforeSweeping < 1000 * 60 * 60) {
			long minutes = millisBeforeSweeping / 1000 / 60;
			mTvNotificationTail.setText(res.getQuantityString(
					R.plurals.minutes_before_sweep, (int) minutes,
					(int) minutes));
		} else if (millisBeforeSweeping < 1000 * 60 * 60 * 24) {
			long hours = millisBeforeSweeping / 1000 / 60 / 60;
			millisBeforeSweeping = hours * 1000 * 60 * 60;
			mTvNotificationTail.setText(res.getQuantityString(
					R.plurals.hours_before_sweep, (int) hours, (int) hours));
		} else {
			long days = millisBeforeSweeping / 1000 / 60 / 60 / 24;
			millisBeforeSweeping = days * 1000 * 60 * 60 * 24;
			mTvNotificationTail.setText(res.getQuantityString(
					R.plurals.days_before_sweep, (int) days, (int) days));
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		mPrefs.edit().putLong(PROGRESS_BEFORE_SWEEPING, seekBar.getProgress())
				.commit();
		mPrefs.edit().putLong(MILLIS_BEFORE_SWEEPING, millisBeforeSweeping)
				.commit();

		if (millisBeforeSweeping < 1000) {
			cancelSystemAlarm();
		} else {
			scheduleSystemAlarm(millisBeforeSweeping);
		}
	}

	public void scheduleSystemAlarm(long millisBeforeSweep) {
		Log.d("DEBUG",
				"\n\n************* In scheduleSystemAlarm() *************");

		// Get and parse street sweeping data
		long sweepStartDateInMillis;
		try {
			sweepStartDateInMillis = mScheduleListener.onScheduleAlarm();
		} catch (NullPointerException e) {
			throw new NullPointerException(
					"Must call setOnScheduleAlarmListener(...) "
							+ "on AlarmNotifier prior to attempting to schedule system alarm. "
							+ e.getMessage());
		}
		if (sweepStartDateInMillis == 0)
			return; // Abort if no parking history
		Date sweepStartDate = new Date(sweepStartDateInMillis);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sweepStartDate);
		calendar.add(Calendar.MILLISECOND, (int) (-1 * millisBeforeSweep));

		// Schedule system alarm
		mAlarmManager = (AlarmManager) getActivity().getSystemService(
				Context.ALARM_SERVICE);
		Intent intent = new Intent(getActivity(), MapActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra(EXTRA_FROM_ALARM, true);
		intent.putExtra(EXTRA_NEXT_SWEEPING, sweepStartDateInMillis);

		mAlarmIntent = PendingIntent.getActivity(getActivity(), ALARM_REQUEST,
				intent, PendingIntent.FLAG_ONE_SHOT);

		// Test with an alarm 30 seconds from now
		// calendar = Calendar.getInstance();
		// calendar.add(Calendar.SECOND, 10);
		// Log.d("New time", calendar.toString());

		mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				mAlarmIntent);

		Log.d("DEBUG", "***********************************\n\n");
	}

	public void cancelSystemAlarm() {
		Log.d("DEBUG", "\n\n************* In cancelSystemAlarm() *************");
		if (mAlarmManager != null) {
			mAlarmManager.cancel(mAlarmIntent);
		}

		Log.d("DEBUG", "***********************************\n\n");
	}

}