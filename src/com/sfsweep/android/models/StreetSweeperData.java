package com.sfsweep.android.models;

import java.io.Serializable;
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
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLng;

@Table(name = "street_sweeper_data")
public class StreetSweeperData extends Model implements Serializable {

	private static final long serialVersionUID = 7312634491527815370L;

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

	private List<DateInterval> getSweepingsInMonth(Calendar cal) {
		List<DateInterval> result = new ArrayList<DateInterval>();
		SimpleDateFormat sdf = new SimpleDateFormat("F EEEE MM yyyy HH:mm",
				Locale.ENGLISH);
		for (int i : getSweptWeeksOfMonth()) {
			String weekDay = this.WeekDay.replace("Tues", "Tue");
			try {
				String sStart = String.format("%s %s %s %s %s", i, weekDay,
						cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR),
						this.FromHour);
				Date start = sdf.parse(sStart);

				String sEnd = String.format("%s %s %s %s %s", i, weekDay,
						cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR),
						this.ToHour);
				Date end = sdf.parse(sEnd);

				result.add(new DateInterval(start, end));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public class DateInterval {
		public Date start;
		public Date end;

		DateInterval(Date start, Date end) {
			this.start = start;
			this.end = end;
		}
	}

	public DateInterval nextSweeping(boolean includeInProgress) {
		DateInterval nextSweeping;
		List<DateInterval> upcomingSweepings = upcomingSweepings();
		try {
			nextSweeping = upcomingSweepings.get(0);
			if (nextSweeping.start.before(new Date())) {
				// Sweeping in progress
				if (includeInProgress) {
					nextSweeping = upcomingSweepings.get(1);
				}
			}
		} catch (IndexOutOfBoundsException e) {
			nextSweeping = null;
		}
		return nextSweeping;
	}

	public List<DateInterval> upcomingSweepings() {
		Calendar now = Calendar.getInstance();
		Calendar nextMonth = Calendar.getInstance();
		nextMonth.add(Calendar.MONTH, 1);

		List<DateInterval> result = new ArrayList<DateInterval>();

		if (this.WeekDay.equals("Holiday")) {
			// TODO: Support Holidays
		} else {
			for (DateInterval i : getSweepingsInMonth(now)) {
				if (i.end.after(now.getTime()))
					result.add(i);
			}
			for (DateInterval i : getSweepingsInMonth(nextMonth)) {
				if (i.end.after(now.getTime()))
					result.add(i);
			}
		}

		return result;
	}

	public LatLng nearestPoint(LatLng point) {
		double nearestDistance = Double.MAX_VALUE;
		LatLng nearestPoint = null;
		LatLng start = null;
		for (LatLng end : getCoordinates()) {
			if (start != null) {
				LatLng p = nearestPointOnLine(start, end, point, true);
				double d = distance(point, p);
				if (d < nearestDistance) {
					nearestDistance = d;
					nearestPoint = p;
				}
			}
			start = end;
		}
		return nearestPoint;
	}

	public static double distance(LatLng a, LatLng b) {
		return Math.sqrt((a.latitude - b.latitude) * (a.latitude - b.latitude)
				+ (a.longitude - b.longitude) * (a.longitude - b.longitude));
	}

	private static LatLng nearestPointOnLine(LatLng a, LatLng b, LatLng p,
			boolean clampToSegment) {

		double apx = p.latitude - a.latitude;
		double apy = p.longitude - a.longitude;
		double abx = b.latitude - a.latitude;
		double aby = b.longitude - a.longitude;

		double ab2 = abx * abx + aby * aby;
		double ap_ab = apx * abx + apy * aby;
		double t = ap_ab / ab2;
		if (clampToSegment) {
			if (t < 0) {
				t = 0;
			} else if (t > 1) {
				t = 1;
			}
		}
		return new LatLng(a.latitude + abx * t, a.longitude + aby * t);
	}

	public static StreetSweeperData getById(long parkedDataId) {
		return new Select().from(StreetSweeperData.class).where("id = ?", parkedDataId)
				.limit(1).executeSingle();	
	}
	
}
