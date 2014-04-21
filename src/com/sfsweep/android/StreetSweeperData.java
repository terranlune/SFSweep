package com.sfsweep.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.android.gms.maps.model.LatLng;

@Table(name = "street_sweeper_data")
public class StreetSweeperData extends Model {

	@Column(name = "kml_id")
	public String kml_id;

	@Column(name = "name")
	public String name;

	@Column(name = "min_latitude")
	public double min_latitude;

	@Column(name = "min_longitude")
	public double min_longitude;

	@Column(name = "max_latitude")
	public double max_latitude;

	@Column(name = "max_longitude")
	public double max_longitude;

	@Column(name = "coordinates")
	public String coordinates;

	@Column(name = "CNN")
	public String CNN;

	@Column(name = "WeekDay")
	public String WeekDay;

	@Column(name = "BlockSide")
	public String BlockSide;

	@Column(name = "BlockSweepID")
	public String BlockSweepID;

	@Column(name = "CNNRightLeft")
	public String CNNRightLeft;

	@Column(name = "Corridor")
	public String Corridor;

	@Column(name = "FromHour")
	public String FromHour;

	@Column(name = "ToHour")
	public String ToHour;

	@Column(name = "Holidays")
	public String Holidays;

	@Column(name = "Week1OfMonth")
	public String Week1OfMonth;

	@Column(name = "Week2OfMonth")
	public String Week2OfMonth;

	@Column(name = "Week3OfMonth")
	public String Week3OfMonth;

	@Column(name = "Week4OfMonth")
	public String Week4OfMonth;

	@Column(name = "Week5OfMonth")
	public String Week5OfMonth;

	@Column(name = "LF_FADD")
	public String LF_FADD;

	@Column(name = "LF_TOADD")
	public String LF_TOADD;

	@Column(name = "RT_TOADD")
	public String RT_TOADD;

	@Column(name = "RT_FADD")
	public String RT_FADD;

	@Column(name = "STREETNAME")
	public String STREETNAME;

	@Column(name = "ZIP_CODE")
	public String ZIP_CODE;

	@Column(name = "NHOOD")
	public String NHOOD;

	@Column(name = "DISTRICT")
	public String DISTRICT;

	public List<LatLng> getCoordinates() {
		List<LatLng> result = new ArrayList<LatLng>();
		for (String i : coordinates.split(" ")) {
			String[] latLng = i.split(",");
			LatLng c = new LatLng(Double.parseDouble(latLng[0]),
					Double.parseDouble(latLng[1]));
			result.add(c);
		}
		return result;
	}

	private List<Integer> getSweptWeeksOfMonth() {
		List<Integer> result = new ArrayList<Integer>();
		if (this.Week1OfMonth.equals("Yes"))
			result.add(1);
		if (this.Week2OfMonth.equals("Yes"))
			result.add(2);
		if (this.Week3OfMonth.equals("Yes"))
			result.add(3);
		if (this.Week4OfMonth.equals("Yes"))
			result.add(4);
		if (this.Week5OfMonth.equals("Yes"))
			result.add(5);
		return result;
	}

	private List<Date> getSweepingsInMonth(Calendar cal) {
		List<Date> result = new ArrayList<Date>();
		SimpleDateFormat sdf = new SimpleDateFormat("F EEEE MM yyyy HH:mm",
				Locale.ENGLISH);
		for (int i : getSweptWeeksOfMonth()) {
			String weekDay = this.WeekDay.replace("Tues", "Tue");
			String s = String.format("%s %s %s %s %s", i, weekDay,
					cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR),
					this.FromHour);
			try {
				Date date = sdf.parse(s);
				result.add(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public Date nextSweeping() {
		Calendar now = Calendar.getInstance();
		Calendar nextMonth = Calendar.getInstance();
		nextMonth.add(Calendar.MONTH, 1);

		Date result = null;

		if (this.WeekDay.equals("Holiday")) {
			// TODO: Support Holidays
		} else {
			List<Date> sweepings = getSweepingsInMonth(now);
			sweepings.addAll(getSweepingsInMonth(nextMonth));
			for (Date date : sweepings) {
				if (date.after(now.getTime())) {
					if (result == null || date.before(result)) {
						result = date;
					}
				}
			}
		}

		return result;
	}
}
