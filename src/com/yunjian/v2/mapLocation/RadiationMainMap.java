package com.yunjian.v2.mapLocation;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import com.yunjian.v2.yunjian_json.PersonalMain;
import com.yunjian.v2.yunjian_json.R;
import com.yunjian.v2.yunjian_json.RadiationAlarmListener;
import com.yunjian.v2.yunjian_json.RadiationCheck;
import com.yunjian.v2.yunjian_json.Yunjian_json;

/**
 * 演示MapView的基本用法
 */
public class RadiationMainMap extends Activity implements BDLocationListener,GetRadiationListener {

	final static String TAG = "MainActivity";
	/**
	 *  MapView 是地图主控件
	 */
	private MapView mMapView = null;
	/**
	 *  用MapController完成地图控制 
	 */
	private MapController mMapController = null;
	
	/**
	 * 定位功能
	 */
	public String mTime;
	public int mType;
	public double mLat = 0f;
	public double mLon = 0f;
	public double mRad = 0f;
	public double mSpeed = 0f;
	public int mNumSat = 0;
	public String mAddr = "";
	private LocationClient mLocationClient = null;
	
	/**
	 * 监测辐射强度
	 */
	RadiationCheck mAlarmCheck = null;
	RadiationAlarmListener mAlarmListener = null;
	
	/**
	 * 图层上显示辐射位置
	 */
	RadiationOverlay mRadOverlay = null;
	RadOverlayInterface mRadListener = null;
	GeoPoint mTapPoint = null;
	OverlayItem mTapItem = null;
	GetRadiationList mGetlist = null;

	/**
	 *  MKMapViewListener 用于处理地图事件回调
	 */
	MKMapViewListener mMapListener = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 使用地图sdk前需先初始化BMapManager.
         * BMapManager是全局的，可为多个MapView共用，它需要地图模块创建前创建，
         * 并在地图地图模块销毁后销毁，只要还有地图模块在使用，BMapManager就不应该销毁
         */
        DemoApplication app = (DemoApplication)this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(getApplicationContext());
            /**
             * 如果BMapManager没有初始化则初始化BMapManager
             */
            app.mBMapManager.init(new DemoApplication.MyGeneralListener());
        }
        /**
          * 由于MapView在setContentView()中初始化,所以它需要在BMapManager初始化之后
          */
        setContentView(R.layout.activity_main);
        mMapView = (MapView)findViewById(R.id.bmapView);
        /**
         * 获取地图控制器
         */
        mMapController = mMapView.getController();
        /**
         *  设置地图是否响应点击事件  .
         */
        mMapController.enableClick(true);
        /**
         * 设置地图缩放级别
         */
        mMapController.setZoom(12);
        
        /**
         * 初始化辐射点列表拉取类
         */
        mGetlist = new GetRadiationList(this);
        
        /**
         * 初始化上报button
         */
        ((Button) findViewById(R.id.btn_report)).setOnClickListener(new Button.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	mRadListener.onTapMapView(BMapUtil.genGeoPoint(mLat, mLon));
            }

        });
        
        /**
         * 设置定位监听功能
         */
        mLocationClient = new LocationClient(this.getApplicationContext());
        mLocationClient.registerLocationListener(this);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度，默认值gcj02
        option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        // 启动定位服务
        mLocationClient.start();
        
        // 设置陀螺仪告警监听
        mAlarmListener = new RadiationAlarmListener() {
        	private int mv_cnt = 0;
        	
			@Override
			public void onMove(double x, double y, double z) {
				// 提示移动中
				if ( mv_cnt == 0 ) {
					//Toast.makeText(RadiationMainMap.this, "请尽量保持手机静止", Toast.LENGTH_SHORT).show();
				}
				mv_cnt = (mv_cnt+1) % 10;
			}

			@Override
			public void onAlarm(double x, double y, double z) {
				setLocationCenter(mLat, mLon, null);
				// 插入Overlay标志，并上报GeoPoint和当前辐射最大值
				mRadOverlay.AddOverlayItem(BMapUtil.genGeoPoint(mLat, mLon), "辐射点", "辐射点信息", null);
				ReportRadLocation r = new ReportRadLocation(RadiationMainMap.this.getApplicationContext());
				r.setReportParam(mLat, mLon, x, y, z, ReportRadLocation.TYPE_MF);
				r.go();
			}
        };
        mAlarmCheck = new RadiationCheck(mAlarmListener, this);
        
        /**
         * 设置告警标记Overlay
         */
        mRadOverlay = new RadiationOverlay(this.getResources().getDrawable(R.drawable.icon_geo), mMapView);
        mRadListener = new RadOverlayInterface() {
        	private PopupOverlay pop = new PopupOverlay(mMapView, new PopupClickListener() {
				@Override
				public void onClickedPopup(int arg0) {
					// 暂时不做什么
				}
        	});
        	private View popView = ((LayoutInflater)RadiationMainMap.this.
        			getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
        			inflate(R.layout.activity_yunjian_json, null);
			@Override
			public void onTapRadPoint(int idx, OverlayItem it) {
				// 显示辐射最大值，最后上报时间，上报人数
				pop.showPopup(popView, it.getPoint(), 2);
			}

			@Override
			public void onTapMapView(GeoPoint p) {
				// 确认是否手工上报
				//mTapItem = mRadOverlay.AddOverlayItem(p, "手动添加", "手动添加辐射点", RadiationMainMap.this.getResources().getDrawable(R.drawable.icon_gcoding));
				mTapPoint = p;
				
				// 这里移动后会清除所有item，与下面的removeItem冲突，暂时先去掉
				//RadiationMainMap.this.setLocationCenter(p, null);
				
				AlertDialog.Builder builder = new Builder(RadiationMainMap.this);
				builder.setMessage("确认手工添加辐射点？");
				builder.setTitle("提示");
				builder.setPositiveButton("确认", new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {		
						if ( mTapPoint != null ) {
							//if ( mTapItem != null ) mRadOverlay.removeItem(mTapItem);
							mRadOverlay.AddOverlayItem(mTapPoint, "辐射点", "辐射点信息", null);
							ReportRadLocation r = new ReportRadLocation(RadiationMainMap.this.getApplicationContext());
							r.setReportParam(mTapPoint.getLatitudeE6()/1E6, mTapPoint.getLongitudeE6()/1E6, 0f, 0f, 0f, ReportRadLocation.TYPE_MF);
							r.go();
						}
						
						arg0.dismiss();
					}
				});
				
				builder.setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if ( mTapItem != null )
							mRadOverlay.DelOverlayItem(mTapItem);
						
						dialog.dismiss();
					}
				});
				builder.create().show();
				
			}
        	
        };
        mRadOverlay.registerTapListener(mRadListener);
        mMapView.getOverlays().clear();  
        mMapView.getOverlays().add(mRadOverlay);  
       
        /**
         * 将地图移动至指定点
         * 使用百度经纬度坐标，可以通过http://api.map.baidu.com/lbsapi/getpoint/index.html查询地理坐标
         * 如果需要在百度地图上显示使用其他坐标系统的位置，请发邮件至mapapi@baidu.com申请坐标转换接口
         * 默认天安门
         */
        setLocationCenter(39.945, 116.404, null);
        
        
        /**
    	 *  MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
    	 */
        mMapListener = new MKMapViewListener() {
			@Override
			public void onMapMoveFinish() {
				/**
				 * 在此处理地图移动完成回调
				 * 缩放，平移等操作完成后，此回调被触发
				 */
			}
			
			@Override
			public void onClickMapPoi(MapPoi mapPoiInfo) {
				/**
				 * 在此处理底图poi点击事件
				 * 显示底图poi名称并移动至该点
				 * 设置过： mMapController.enableClick(true); 时，此回调才能被触发
				 * 
				 */
				String title = "";
				if (mapPoiInfo != null){
					title = mapPoiInfo.strText;
					Toast.makeText(RadiationMainMap.this,title,Toast.LENGTH_SHORT).show();
					mMapController.animateTo(mapPoiInfo.geoPt);
				}
			}

			@Override
			public void onGetCurrentMap(Bitmap b) {
				/**
				 *  当调用过 mMapView.getCurrentMap()后，此回调会被触发
				 *  可在此保存截图至存储设备
				 */
			}

			@Override
			public void onMapAnimationFinish() {
				/**
				 *  地图完成带动画的操作（如: animationTo()）后，此回调被触发
				 */
				// 拉取辐射点列表
				mGetlist.getList(mMapView.getMapCenter());
			}
            /**
             * 在此处理地图载完成事件 
             */
			@Override
			public void onMapLoadFinish() {
				Toast.makeText(RadiationMainMap.this, 
						       "地图加载完成", 
						       Toast.LENGTH_SHORT).show();
				
			}
		};
		mMapView.regMapViewListener(DemoApplication.getInstance().mBMapManager, mMapListener);
    }
    
    @Override
    protected void onPause() {
    	/**
    	 *  MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
    	 */
        mMapView.onPause();
        mAlarmCheck.unRegisterDevice();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
    	/**
    	 *  MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
    	 */
        mMapView.onResume();
        
        int reqret = 0;
        if (mLocationClient != null && mLocationClient.isStarted())
        	  reqret = mLocationClient.requestLocation();
        else 
        	 Log.d("LocSDK3", "locClient is null or not started");
        
        if ( reqret != 0 ) {
        	Log.d("LocSDK3", "locClient req failed: "+reqret);
        }
        mAlarmCheck.registerDevice();
        
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
    	/**
    	 *  MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
    	 */
        mMapView.destroy();
        super.onDestroy();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	mMapView.onSaveInstanceState(outState);
    	
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	mMapView.onRestoreInstanceState(savedInstanceState);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// 添加测试菜单
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 1, "Debug页面");
		menu.add(0, 2, 1, "定位当前位置");
		menu.add(0, 3, 1, "测试页面");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 跳到老的页面上
		switch (item.getItemId()) {
		case 1:
			Intent old_activity = new Intent(RadiationMainMap.this, Yunjian_json.class);
			this.startActivity(old_activity);
			break;
		case 2:
			setLocationCenter(mLat, mLon, null);
			break;
		case 3:
			Intent testActivity = new Intent(RadiationMainMap.this, PersonalMain.class);
			this.startActivity(testActivity);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void setLocationCenter(double lat, double lon, MapController con) {
    	GeoPoint p ;
        Intent  intent = getIntent();
        
        if ( intent.hasExtra("x") && intent.hasExtra("y") ){
        	//当用intent参数时，设置中心点为指定点
        	Bundle b = intent.getExtras();
        	p = new GeoPoint(b.getInt("y"), b.getInt("x"));
        }else{
        	//设置中心点为天安门
        	 p = BMapUtil.genGeoPoint(lat, lon);
        }
        
        setLocationCenter(p, con);
    }
	
	private void setLocationCenter(GeoPoint p, MapController con) {
		if ( con == null )
			//mMapController.setCenter(p);
			mMapController.animateTo(p);
		else
			//con.setCenter(p);
			con.animateTo(p);
	}

	@Override
	public void onReceiveLocation(BDLocation arg0) {
		// 接收定位消息
		if ( arg0 == null ) {
			return;
		}
		boolean bCenterflag = false;
		
		if ( mLat == 0f && mLon == 0f )
			bCenterflag = true;
		
		mTime = arg0.getTime();
		mType = arg0.getLocType();
		mLat = arg0.getLatitude();
		mLon = arg0.getLongitude();
		mRad = arg0.getRadius();
		
		mSpeed = 0f;
		mNumSat = 0;
		mAddr = "";
		if ( mType == BDLocation.TypeGpsLocation ) {
			mSpeed = arg0.getSpeed();
			mNumSat = arg0.getSatelliteNumber();
		}else if ( mType == BDLocation.TypeNetWorkLocation ) {
			mAddr = arg0.getAddrStr();
		}
		
		if ( bCenterflag )
			setLocationCenter(mLat, mLon, null);
	}

	@Override
	public void onReceivePoi(BDLocation arg0) {
		// 下个版本sdk即将取消
		
	}

	@Override
	public void onGetList(ArrayList<RadPoint> arr) {
		mRadOverlay.clearOverlayItems();
		// 在图上画点
		Iterator<RadPoint> it = arr.iterator();
		while ( it.hasNext() ) {
			RadPoint p = it.next();
			mRadOverlay.AddOverlayItem(BMapUtil.genGeoPoint(p.lat, p.lon), "辐射点", "辐射点信息", null);
		}
	}
    
}
