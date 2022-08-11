package com.example.fasal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.cloud.CloudManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.esri.android.map.FeatureLayer;
import com.esri.android.toolkit.map.MapViewHelper;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;

public class MainActivity extends Activity {
	MapView mMapView = null;
	com.esri.android.map.MapView mapView;
	BaiduMap mBaiduMap = null;
	public LocationClient mLocationClient = null;
	public MyLocationListener myLocationListener;
	public BDLocation location = null;
	int modeFlag = 1;
	private boolean isFirstLocation = true;
	FeatureLayer featureLayer;
	int street = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		// 初始化控件
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		// 获取地图控件引用
		mBaiduMap = mMapView.getMap();
		// 开始定位
		initLocation();
		mLocationClient.start();

		// LBD云检索
		/**
		 * CloudManager:LBS云检索管理类 getInstance():获取唯一可用实例 init(CloudListener
		 * listener):初始化
		 * 需要实现CloudListener接口的onGetDetailSearchResult和onGetSearchResult方法
		 * */

		// 普通地图
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		/*mapView = (com.esri.android.map.MapView) findViewById(R.id.emapView);
		
		// shp数据加载
		try {

			ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(
					"/Amapshp/re.shp");
			Toast.makeText(this, "错", Toast.LENGTH_SHORT).show();
			featureLayer = new FeatureLayer(shapefileFeatureTable);
			mapView.addLayer(featureLayer);
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "/Amapshp/re.shp", Toast.LENGTH_SHORT).show();
		}*/
		LatLng cenpt = new LatLng(24.823585015525722, 102.84865491668704);
		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(15)
				.build();
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);

		// 改变地图状态
		mBaiduMap.setMapStatus(mMapStatusUpdate);


	}

	public String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		return sdDir.toString();

	}

	// 实现btnClick方法 卫星图切换
	public void btnClick(View v) {
		// 卫星地图切换
		Button btn = (Button) this.findViewById(R.id.button1);
		if (modeFlag == 1) {
			modeFlag = 2;
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			btn.setBackgroundResource(R.drawable.close);
			btn.setText("卫星");
			Toast.makeText(this, "乃好，更换卫星地图", Toast.LENGTH_SHORT).show();
		} else if (modeFlag == 2) {
			modeFlag = 1;
			Toast.makeText(this, "乃好，更换普通地图", Toast.LENGTH_SHORT).show();
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			btn.setText("普通");
			btn.setBackgroundResource(R.drawable.open);
		}// else{
			// modeFlag = 1;
			// mMapView.setMapCustomEnable(true);
		// }

	}

	// 实现实时交通图切换
	public void btnClick2(View v) {
		try {
			if (mBaiduMap.isTrafficEnabled()) {// 判断是否开启了交通图
				mBaiduMap.setTrafficEnabled(false);
				mBaiduMap.setBaiduHeatMapEnabled(true);
				Toast.makeText(this, "乃好，开启城市热点图", Toast.LENGTH_SHORT).show();
			} else if (mBaiduMap.isBaiduHeatMapEnabled()) {
				mBaiduMap.setBaiduHeatMapEnabled(false);
				Toast.makeText(this, "乃好，开启普通地图", Toast.LENGTH_SHORT).show();
			} else {
				mBaiduMap.setTrafficEnabled(true);
				Toast.makeText(this, "乃好，开启实时交通图", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	// 返回自己位置
	public void btnClick3(View v) {
		isFirstLocation = true;
		Toast.makeText(this, "返回自己的位置", Toast.LENGTH_SHORT).show();
	}

	// 开启城市热点图
	public void btnClick4(View v) {
		MapStatus mMapStatus = null;
		MapStatusUpdate mMapStatusUpdate = null;
		LatLng southwest;
		LatLng northeast;
		LatLngBounds bounds;
		// 定义Ground显示的图片
		BitmapDescriptor bdGround;
		// 定义Ground覆盖物选项
		OverlayOptions ooGround;
		switch (street) {
		case 1:
			mBaiduMap.clear();
			Toast.makeText(this, "乃，选择萌小鼠", Toast.LENGTH_SHORT).show();
			street = 2;
			southwest = new LatLng(24.889204, 102.833348);
			northeast = new LatLng(24.887244, 102.836196);
			bounds = new LatLngBounds.Builder().include(northeast)
					.include(southwest).build();
			// 定义Ground显示的图片
			bdGround = BitmapDescriptorFactory
					.fromResource(R.drawable.rat);
			// 定义Ground覆盖物选项
			ooGround = new GroundOverlayOptions()
					.positionFromBounds(bounds).image(bdGround)
					.transparency(0.8f);
			// 在地图中添加Ground覆盖物
			mBaiduMap.addOverlay(ooGround);
			mMapStatus = new MapStatus.Builder().target(southwest).zoom(18)
					.build();
			// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

			mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
			// 改变地图状态
			mBaiduMap.setMapStatus(mMapStatusUpdate);
			break;
		case 2:
			mBaiduMap.clear();
			Toast.makeText(this, "凡人,你被选中了  神之子——洪", Toast.LENGTH_SHORT).show();
			street = 3;
			southwest = new LatLng(24.827009,102.854768);
			northeast = new LatLng(24.826025,102.856223);
			bounds = new LatLngBounds.Builder().include(northeast)
					.include(southwest).build();
			// 定义Ground显示的图片
			bdGround = BitmapDescriptorFactory
					.fromResource(R.drawable.hong);
			// 定义Ground覆盖物选项
			ooGround = new GroundOverlayOptions()
					.positionFromBounds(bounds).image(bdGround)
					.transparency(0.8f);
			// 在地图中添加Ground覆盖物
			mBaiduMap.addOverlay(ooGround);
			mMapStatus = new MapStatus.Builder().target(southwest).zoom(19)
					.build();
			// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

			mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
			// 改变地图状态
			mBaiduMap.setMapStatus(mMapStatusUpdate);
			break;

		case 3:
			mBaiduMap.clear();
			Toast.makeText(this, "选择心形路线", Toast.LENGTH_SHORT).show();
			street = 4;
			List<LatLng> points1 = new ArrayList<LatLng>();
			points1.add(new LatLng(24.82997, 102.853098));
			points1.add(new LatLng(24.83049, 102.852532));
			points1.add(new LatLng(24.83049, 102.852532));
			points1.add(new LatLng(24.830884, 102.852896));
			points1.add(new LatLng(24.830884, 102.852896));
			points1.add(new LatLng(24.83056, 102.853156));
			points1.add(new LatLng(24.83056, 102.853156));
			points1.add(new LatLng(24.83097, 102.853588));
			points1.add(new LatLng(24.83097, 102.853588));
			points1.add(new LatLng(24.830605, 102.853745));
			points1.add(new LatLng(24.830605, 102.853745));
			points1.add(new LatLng(24.82997, 102.853098));

			// 构建分段颜色索引数组
			List<Integer> colors1 = new ArrayList<Integer>();
			colors1.add(Integer.valueOf(Color.RED));
			colors1.add(Integer.valueOf(Color.BLUE));
			colors1.add(Integer.valueOf(Color.YELLOW));
			colors1.add(Integer.valueOf(Color.GREEN));
			colors1.add(Integer.valueOf(Color.YELLOW));
			colors1.add(Integer.valueOf(Color.DKGRAY));

			OverlayOptions ooPolyline1 = new PolylineOptions().width(10)
					.colorsValues(colors1).points(points1);
			// 添加在地图中
			Polyline mPolyline1 = (Polyline) mBaiduMap.addOverlay(ooPolyline1);

			LatLng cenpt2 = new LatLng(24.82997, 102.853098);
			// 定义地图状态
			mMapStatus = new MapStatus.Builder().target(cenpt2).zoom(19)
					.build();
			// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

			mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
			// 改变地图状态
			mBaiduMap.setMapStatus(mMapStatusUpdate);
			break;

		case 4:
			mBaiduMap.clear();
			Toast.makeText(this, "人类,你被选中了  神之子——熊", Toast.LENGTH_SHORT).show();
			street = 5;
			southwest = new LatLng(24.827009,102.854768);
			northeast = new LatLng(24.826025,102.856223);
			bounds = new LatLngBounds.Builder().include(northeast)
					.include(southwest).build();
			// 定义Ground显示的图片
			bdGround = BitmapDescriptorFactory
					.fromResource(R.drawable.xiong);
			// 定义Ground覆盖物选项
			ooGround = new GroundOverlayOptions()
					.positionFromBounds(bounds).image(bdGround)
					.transparency(0.8f);
			// 在地图中添加Ground覆盖物
			mBaiduMap.addOverlay(ooGround);
			mMapStatus = new MapStatus.Builder().target(southwest).zoom(19)
					.build();
			// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

			mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
			// 改变地图状态
			mBaiduMap.setMapStatus(mMapStatusUpdate);
		
			break;

		case 5:
			mBaiduMap.clear();
			street = 1;
			List<LatLng> points = new ArrayList<LatLng>();
			points.add(new LatLng(24.8299, 102.859606));
			points.add(new LatLng(24.829306, 102.858811));
			points.add(new LatLng(24.829306, 102.858811));
			points.add(new LatLng(24.829371, 102.859674));
			points.add(new LatLng(24.829371, 102.859674));
			points.add(new LatLng(24.8299, 102.859606));

			// 构建分段颜色索引数组
			List<Integer> colors = new ArrayList<Integer>();
			colors.add(Integer.valueOf(Color.RED));
			colors.add(Integer.valueOf(Color.blue(50)));
			colors.add(Integer.valueOf(Color.YELLOW));
			colors.add(Integer.valueOf(Color.GREEN));

			OverlayOptions ooPolyline = new PolylineOptions().width(10)
					.colorsValues(colors).points(points);
			// 添加在地图中
			Polyline mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);

			LatLng cenpt = new LatLng(24.8299, 102.859606);
			// 定义地图状态
			mMapStatus = new MapStatus.Builder().target(cenpt).zoom(19).build();
			// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

			mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
			// 改变地图状态
			mBaiduMap.setMapStatus(mMapStatusUpdate);
			Toast.makeText(this, "选择三角形路线", Toast.LENGTH_SHORT).show();		
		break;
		default:
			break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	private void initLocation() {
		mLocationClient = new LocationClient(this);
		myLocationListener = new MyLocationListener();
		// 声明LocationClient类
		mLocationClient.registerLocationListener(myLocationListener);
		// 注册监听函数
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);
		// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

		option.setCoorType("bd09ll");
		// 可选，默认gcj02，设置返回的定位结果坐标系

		int span = 1000;
		option.setScanSpan(span);
		// 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

		option.setIsNeedAddress(true);
		// 可选，设置是否需要地址信息，默认不需要

		option.setOpenGps(true);
		// 可选，默认false,设置是否使用gps

		option.setLocationNotify(true);
		// 可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

		option.setIsNeedLocationDescribe(true);
		// 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

		option.setIsNeedLocationPoiList(true);
		// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

		option.setIgnoreKillProcess(false);
		// 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

		option.SetIgnoreCacheException(false);
		// 可选，默认false，设置是否收集CRASH信息，默认收集

		option.setEnableSimulateGps(false);
		// 可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

		mLocationClient.setLocOption(option);
	}

	private class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// 将获取的location信息给百度map
			MyLocationData data = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(10).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(data);

			InputStream is = getResources().openRawResource(R.drawable.marker);
			Bitmap mBitmap = BitmapFactory.decodeStream(is);
			Bitmap.createScaledBitmap(mBitmap, 20, 20, true); // 设置bitmap大小

			BitmapDescriptor loc = BitmapDescriptorFactory.fromBitmap(mBitmap);
			LatLng latlong = new LatLng(location.getLatitude(),
					location.getLongitude());
			OverlayOptions options = new MarkerOptions().position(latlong) // 设置marker的位置
					.icon(loc) // 设置marker图标
					.zIndex(5) // 设置marker所在层级
					.perspective(true).draggable(true); // 设置手势拖拽
			// 将marker添加到地图上
			mBaiduMap.addOverlay(options);

			if (isFirstLocation) {
				// 获取经纬度
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(ll);
				// mBaiduMap.setMapStatus(status);//直接到中间
				mBaiduMap.animateMapStatus(status);// 动画的方式到中间
				isFirstLocation = false;
			}
		}

	}

}