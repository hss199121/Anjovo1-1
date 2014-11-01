/**
 * 
 */
package com.anjoyo.gaodemap;

import com.amap.api.cloud.model.LatLonPoint;
import com.amap.api.maps.model.LatLng;

public class AMapUtil {


	public static boolean isEmptyOrNullString(String s) {
		return (s == null) || (s.trim().length() == 0);
	}

	public static LatLonPoint convertToLatLonPoint(LatLng latlon) {
		return new LatLonPoint(latlon.latitude, latlon.longitude);
	}

	public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
		return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
	}
}
