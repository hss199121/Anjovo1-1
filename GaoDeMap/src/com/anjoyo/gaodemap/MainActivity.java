package com.anjoyo.gaodemap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amap.api.cloud.model.AMapCloudException;
import com.amap.api.cloud.model.CloudItem;
import com.amap.api.cloud.model.CloudItemDetail;
import com.amap.api.cloud.model.LatLonPoint;
import com.amap.api.cloud.search.CloudResult;
import com.amap.api.cloud.search.CloudSearch;
import com.amap.api.cloud.search.CloudSearch.OnCloudSearchListener;
import com.amap.api.cloud.search.CloudSearch.SearchBound;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;

public class MainActivity extends Activity implements OnMarkerClickListener,
		InfoWindowAdapter, OnCloudSearchListener, OnInfoWindowClickListener {
	private MapView mapView;
	private AMap mAMap;
	private CloudSearch mCloudSearch;
	private String mTableID = ""; // 用户tableid，从官网下载测试数据后在云图中新建地图并导入，获取相应的tableid
	private String mKeyWord = "公园"; // 搜索关键字
	private CloudSearch.Query mQuery;
	private LatLonPoint mCenterPoint = new LatLonPoint(39.942753, 116.428650); // 周边搜索中心点
	private LatLonPoint mPoint1 = new LatLonPoint(39.941711, 116.382248);
	private LatLonPoint mPoint2 = new LatLonPoint(39.884882, 116.359566);
	private LatLonPoint mPoint3 = new LatLonPoint(39.878120, 116.437630);
	private LatLonPoint mPoint4 = new LatLonPoint(39.941711, 116.382248);
	private PoiOverlay mPoiCloudOverlay;
	private List<CloudItem> mCloudItems;
	private Marker mCloudIDMarer;
	private String TAG = "AMapYunTuDemo";
	private ArrayList<CloudItem> items = new ArrayList<CloudItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		init();
	}

	public void searchByBound(View view) {
		items.clear();
		SearchBound bound = new SearchBound(new LatLonPoint(
				mCenterPoint.getLatitude(), mCenterPoint.getLongitude()), 4000);
		try {
			mQuery = new CloudSearch.Query(mTableID, mKeyWord, bound);
			mQuery.setPageSize(10);
			CloudSearch.Sortingrules sorting = new CloudSearch.Sortingrules("_id",
					false);
			mQuery.setSortingrules(sorting);
			mCloudSearch.searchCloudAsyn(mQuery);// 异步搜索
		} catch (AMapCloudException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (mAMap == null) {
			mAMap = mapView.getMap();
		}
		mCloudSearch = new CloudSearch(this);
		mCloudSearch.setOnCloudSearchListener(this);
		mAMap.setOnMarkerClickListener(this);
		mAMap.setOnInfoWindowClickListener(this);
		mAMap.setInfoWindowAdapter(this);
		mAMap.setOnInfoWindowClickListener(this);

	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onCloudItemDetailSearched(CloudItemDetail item, int rCode) {
		if (rCode == 0 && item != null) {
			if (mCloudIDMarer != null) {
				mCloudIDMarer.destroy();
			}
			mAMap.clear();
			LatLng position = AMapUtil.convertToLatLng(item.getLatLonPoint());
			mAMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(new CameraPosition(position, 18, 0, 30)));
			mCloudIDMarer = mAMap.addMarker(new MarkerOptions()
					.position(position)
					.title(item.getTitle())
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			items.add(item);
			Log.d(TAG, "_id" + item.getID());
			Log.d(TAG, "_location" + item.getLatLonPoint().toString());
			Log.d(TAG, "_name" + item.getTitle());
			Log.d(TAG, "_address" + item.getSnippet());
			Log.d(TAG, "_caretetime" + item.getCreatetime());
			Log.d(TAG, "_updatetime" + item.getUpdatetime());
			Log.d(TAG, "_distance" + item.getDistance());
			Iterator iter = item.getCustomfield().entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				Log.d(TAG, key + "   " + val);
			}
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onCloudSearched(CloudResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getQuery() != null) {
				if (result.getQuery().equals(mQuery)) {
					mCloudItems = result.getClouds();

					if (mCloudItems != null && mCloudItems.size() > 0) {
						mAMap.clear();
						mPoiCloudOverlay = new PoiOverlay(mAMap, mCloudItems);
						mPoiCloudOverlay.removeFromMap();
						mPoiCloudOverlay.addToMap();
						// mPoiCloudOverlay.zoomToSpan();
						for (CloudItem item : mCloudItems) {
							items.add(item);
							Log.d(TAG, "_id " + item.getID());
							Log.d(TAG, "_location "
									+ item.getLatLonPoint().toString());
							Log.d(TAG, "_name " + item.getTitle());
							Log.d(TAG, "_address " + item.getSnippet());
							Log.d(TAG, "_caretetime " + item.getCreatetime());
							Log.d(TAG, "_updatetime " + item.getUpdatetime());
							Log.d(TAG, "_distance " + item.getDistance());
							Iterator iter = item.getCustomfield().entrySet()
									.iterator();
							while (iter.hasNext()) {
								Map.Entry entry = (Map.Entry) iter.next();
								Object key = entry.getKey();
								Object val = entry.getValue();
								Log.d(TAG, key + "   " + val);
							}
						}
						if (mQuery.getBound().getShape()
								.equals(SearchBound.BOUND_SHAPE)) {// 圆形
							mAMap.addCircle(new CircleOptions()
									.center(new LatLng(mCenterPoint
											.getLatitude(), mCenterPoint
											.getLongitude())).radius(5000)
									.strokeColor(
									// Color.argb(50, 1, 1, 1)
											Color.RED)
									.fillColor(Color.argb(50, 1, 1, 1))
									.strokeWidth(25));

							mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
									new LatLng(mCenterPoint.getLatitude(),
											mCenterPoint.getLongitude()), 12));

						} else if (mQuery.getBound().getShape()
								.equals(SearchBound.POLYGON_SHAPE)) {
							mAMap.addPolygon(new PolygonOptions()
									.add(AMapUtil.convertToLatLng(mPoint1))
									.add(AMapUtil.convertToLatLng(mPoint2))
									.add(AMapUtil.convertToLatLng(mPoint3))
									.add(AMapUtil.convertToLatLng(mPoint4))
									.fillColor(Color.LTGRAY)
									.strokeColor(Color.RED).strokeWidth(1));
							LatLngBounds bounds = new LatLngBounds.Builder()
									.include(AMapUtil.convertToLatLng(mPoint1))
									.include(AMapUtil.convertToLatLng(mPoint2))
									.include(AMapUtil.convertToLatLng(mPoint3))
									.build();
							mAMap.moveCamera(CameraUpdateFactory
									.newLatLngBounds(bounds, 50));
						} else if ((mQuery.getBound().getShape()
								.equals(SearchBound.LOCAL_SHAPE))) {
							mPoiCloudOverlay.zoomToSpan();
						}

					} else {
						ToastUtil.show(this, R.string.no_result);
					}
				}
			} else {
				ToastUtil.show(this, R.string.no_result);
			}
		} else {
			ToastUtil.show(this, R.string.error_network);
		}
	}
	@Override
	public View getInfoContents(Marker arg0) {
		return null;
	}
	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}
	@Override
	public boolean onMarkerClick(Marker arg0) {
		return false;
	}
	@Override
	public void onInfoWindowClick(Marker arg0) {
	}
}
