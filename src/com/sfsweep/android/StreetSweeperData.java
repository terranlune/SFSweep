package com.sfsweep.android;

import java.util.ArrayList;
import java.util.List;

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
}
