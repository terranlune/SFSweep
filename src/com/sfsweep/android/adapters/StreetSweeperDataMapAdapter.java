package com.sfsweep.android.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.graphics.Color;
import android.util.Log;
import android.util.Pair;

import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sfsweep.android.models.StreetSweeperData;
import com.sfsweep.android.models.StreetSweeperData.DateInterval;

public class StreetSweeperDataMapAdapter {

	private long PARKING_DURATION_MILLIS = 1000 * 60 * 60 * 24 * 7;
	private static final int LINE_WIDTH = 20;
	private HashMap<StreetSweeperData, Polyline> cache;
	private HashMap<String, List<StreetSweeperData>> cnnCache;
	private GoogleMap map;
	
	public StreetSweeperDataMapAdapter(GoogleMap map) {
		this.map = map;
		cache = new HashMap<StreetSweeperData, Polyline>();
	}
	
	public void fetchData(LatLngBounds bounds) {
		List<StreetSweeperData> l = getDataFromDb(bounds);
		updateCache(l);
		stylePolylines();
	}

	private void stylePolylines() {

		for (String CNN : cnnCache.keySet()) {

			// Sort by next sweep
			List<StreetSweeperData> l = cnnCache.get(CNN);
			Collections.sort(l, new NextSweepingComparator());

			// Color the first one
			StreetSweeperData nextSweepingData = l.remove(0);
			Polyline line = cache.get(nextSweepingData);
			line.setVisible(true);
			int color = getHeatmapColor(nextSweepingData, false);
			line.setColor(color);

			// Hide the remaining ones
			for (StreetSweeperData d : l) {
				cache.get(d).setVisible(false);
			}

		}
	}

	private int getHeatmapColor(StreetSweeperData d, boolean includeInProgress) {
		if (d == null) {
			return Color.MAGENTA;
		}
		DateInterval nextSweeping = d.nextSweeping(includeInProgress);
		if (nextSweeping == null) {
			return Color.MAGENTA;
		}
		long diff = nextSweeping.start.getTime() - new Date().getTime();
		double percent = 1.0 * diff / PARKING_DURATION_MILLIS;
		int color = Color.rgb(0, Math.min(255, (int) (255 * percent)), 0);
		return color;
	}

	public class NextSweepingComparator implements
			Comparator<StreetSweeperData> {
		@Override
		public int compare(StreetSweeperData d1, StreetSweeperData d2) {
			DateInterval di1 = d1.nextSweeping(false);
			DateInterval di2 = d2.nextSweeping(false);
			if (di1 == null && di2 == null) {
				return 0;
			}else if (di1 == null) {
				return 1;
			}else if (di2 == null) {
				return -1;
			}else{
				return -1*d1.nextSweeping(false).start.compareTo(d2
						.nextSweeping(false).start);
			}
		}
	}

	private void updateCache(List<StreetSweeperData> l) {
		HashMap<StreetSweeperData, Polyline> newCache = new HashMap<StreetSweeperData, Polyline>();

		int addCount = 0;
		for (StreetSweeperData d : l) {
			if (cache.containsKey(d)) {
				newCache.put(d, cache.get(d));
				cache.remove(d);
			} else {
				PolylineOptions opts = new PolylineOptions();
				opts.width(LINE_WIDTH);
				opts.addAll(d.getCoordinates());
				Polyline line = map.addPolyline(opts);
				newCache.put(d, line);
				addCount++;
			}
		}

		Log.e("updateCache", String.format("Cache size: %s (+%s,-%s)",
				newCache.size(), addCount, cache.size()));

		// Remove offscreen data
		for (Polyline p : cache.values()) {
			p.remove();
		}

		// Save the new cache
		cache = newCache;

		// Group the data by CNN
		cnnCache = new HashMap<String, List<StreetSweeperData>>();
		for (StreetSweeperData d : cache.keySet()) {
			if (!cnnCache.containsKey(d.CNN)) {
				cnnCache.put(d.CNN, new ArrayList<StreetSweeperData>());
			}
			cnnCache.get(d.CNN).add(d);
		}
	}

	private List<StreetSweeperData> getDataFromDb(LatLngBounds bounds) {
		double min_latitude = bounds.southwest.latitude;
		double max_latitude = bounds.northeast.latitude;
		double min_longitude = bounds.southwest.longitude;
		double max_longitude = bounds.northeast.longitude;

		double buffer_latitude = (max_latitude - min_latitude) / 2;
		double buffer_longitude = (max_longitude - min_longitude) / 2;

		Object[] args = { min_latitude - buffer_latitude,
				max_latitude + buffer_latitude,
				min_longitude - buffer_longitude,
				max_longitude + buffer_longitude };

		From query = new Select()
				.from(StreetSweeperData.class)
				.where("((min_latitude BETWEEN ?1 AND ?2 OR max_latitude BETWEEN ?1 AND ?2)"
						+ " AND (min_longitude BETWEEN ?3 AND ?4 OR max_longitude BETWEEN ?3 AND ?4))",
						args);
		List<StreetSweeperData> l = query.execute();
		return l;
	}

	public Pair<StreetSweeperData, LatLng> findNearestData(LatLng point) {
		double nearestDistance = Double.MAX_VALUE;
		LatLng nearestPoint = null;
		StreetSweeperData nearestData = null;
		for (StreetSweeperData d : cache.keySet()) {

			if (!cache.get(d).isVisible()) {
				continue;
			}

			LatLng p = d.nearestPoint(point);
			double distance = StreetSweeperData.distance(point, p);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestPoint = p;
				nearestData = d;
			}
		}
		return new Pair<StreetSweeperData, LatLng>(nearestData, nearestPoint);
	}

}
