package com.yunjian.v2.mapLocation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import com.yunjian.v2.API.AlarmBeep;
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
	private final static int UPDATE_TIME = 300;  // 更新威胁list的时间间隔
	private final static int UPDATE_LOC = 10000; // 更新坐标的时间间隔
	private int updatelocCount = 0;
	
	/**
	 * 设置定时器
	 */
	Timer timer = null;
	TimerTask task = null;
	
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
	
	private PopupOverlay pop = null;
	private View popView = null;
	private TextView mapInfo = null;
	
	private double lastLat = 0f, lastLon = 0f; // 上次拉取数据时的距离
	private int alarmDistance = 0; // 告警距离

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
            	//mRadListener.onTapMapView(BMapUtil.genGeoPoint(mLat, mLon));
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
        option.setScanSpan(UPDATE_LOC);//设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        
        
        // 设置陀螺仪告警监听
        mAlarmListener = new RadiationAlarmListener() {
        	private int mv_cnt = 0;
        	
			@Override
			public void onMove(double x, double y, double z, AlarmBeep alarm) {
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
				r.setReportParam(mLat, mLon, x, y, z, 0f, ReportRadLocation.TYPE_MF);
				r.go();
			}

			@Override
			public void onRadiationChange(double x, double y, double z, double fangcha, 
					int isAlarm, AlarmBeep alarm) {
				if ( isAlarm > 0 ) {
					//setLocationCenter(mLat, mLon, null);
					// 插入Overlay标志，并上报GeoPoint和当前辐射最大值
					mRadOverlay.AddOverlayItem(BMapUtil.genGeoPoint(mLat, mLon), "辐射点", "辐射点信息", null);
					ReportRadLocation r = new ReportRadLocation(RadiationMainMap.this.getApplicationContext());
					r.setReportParam(mLat, mLon, x, y, z, fangcha, ReportRadLocation.TYPE_MF);
					r.go();
				}
			}
        };
        mAlarmCheck = new RadiationCheck(mAlarmListener, this);
        
        /**
         * 设置告警标记Overlay
         */
        pop = new PopupOverlay(mMapView, new PopupClickListener() {
    		@Override
    		public void onClickedPopup(int arg0) {
    			// 暂时不做什么
    		}
    	});
        popView = ((LayoutInflater)RadiationMainMap.this.
    			getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
    			inflate(R.layout.activity_pop_mapinfo, null);
        mapInfo = (TextView)popView.findViewById(R.id.tv_mapinfo);
        
        mRadOverlay = new RadiationOverlay(this.getResources().getDrawable(R.drawable.icon_geo), mMapView);
        mRadListener = new RadOverlayInterface() {
			@Override
			public void onTapRadPoint(int idx, OverlayItem it) {
				// 显示辐射最大值，最后上报时间，上报人数
				RadPoint rp = mGetlist.GetPoint(idx);
				if ( rp != null && mapInfo != null ) {
					mapInfo.setText("上报人数: "+rp.report_count+"次\n"+"上报时间: "+PersonalMain.df.format(rp.lasttime*1000)+
							" "+PersonalMain.tf.format(rp.lasttime*1000)+"\n"+
							"位置: ");
					pop.showPopup(popView, it.getPoint(), 50);
				}
			}

			@Override
			public void onTapMapView(GeoPoint p) {
				// 隐藏弹窗
				pop.hidePop();
				/**
				 * 
				 * 暂时取消手工上报功能
				 * 
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
							r.setReportParam(mTapPoint.getLatitudeE6()/1E6, mTapPoint.getLongitudeE6()/1E6, 0f, 0f, 0f, 0f, ReportRadLocation.TYPE_MF);
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
				*/
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
				// 通过定时task拉取
				//mGetlist.getList(mMapView.getMapCenter());
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
    	mLocationClient.stop();
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
        
        // 获取配置参数
 		SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(this);
 		int maxval = shp.getInt("radVal", 9000);
 		int magcnt = Integer.parseInt(shp.getString("magList", "1"));
 		int alarmval = shp.getInt("alarmVal", 12);
 		boolean moveable = shp.getBoolean("moveable", false);
 		
 		this.alarmDistance = shp.getInt("distVal", 10);
 		
 		Log.d("radpref", "get max: "+maxval+" magcnt: "+magcnt+"alrm: "+alarmval+" mv: "+moveable);
 		
 		//registerReceiver(mBR, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 		mAlarmCheck.setMaxFangcha(maxval);
 		mAlarmCheck.setMagLimit(magcnt);
 		mAlarmCheck.setAlarmLimit(alarmval);
 		mAlarmCheck.setMoveable(moveable);
        
        // 启动定位服务
        mLocationClient.start();
        
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
		
		if ( lastLat == 0f ) {
			lastLat = mLat;
			lastLon = mLon;
		}
		
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
		
		if ( updatelocCount == 0 || 
				DistanceUtil.getDistance(BMapUtil.genGeoPoint(lastLat, lastLon), BMapUtil.genGeoPoint(mLat, mLon)) > 100.0f )
		{
			// 距离变化超过10m，或时间超过5分钟，则拉取威胁点列表
			mGetlist.getList(BMapUtil.genGeoPoint(mLat, mLon));
			
			// 并上报位置
			ReportRadLocation r = new ReportRadLocation(RadiationMainMap.this.getApplicationContext());
			r.setReportParam(mLat, mLon, 0f, 0f, 0f, 0f, ReportRadLocation.TYPE_RP);
			r.go();
		}
		
		updatelocCount = (updatelocCount+1) % (UPDATE_TIME*1000/UPDATE_LOC);
		
		
		/*
		 * 定位功能已包含定时器，不需要再设置
		 * 
		// 设置定时器
		if ( timer == null ) {
			timer = new Timer();
			task = new TimerTask() {

				@Override
				public void run() {
					// 定时触发拉取数据动作
					mGetlist.getList(mMapView.getMapCenter());
				}
				
			};
			
			// 1s后开始执行，每5分钟执行一次
			timer.schedule(task, 1000, 300000)；

		}
		 */		
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
		RadPoint pAlarm = null;
		GeoPoint p2 = BMapUtil.genGeoPoint(mLat, mLon);
		int cnt = 0;
		
		while ( it.hasNext() ) {
			RadPoint p = it.next();
			GeoPoint pp = BMapUtil.genGeoPoint(p.lat, p.lon);
			
			if ( DistanceUtil.getDistance(pp, p2) < alarmDistance ) {
				pAlarm = p;
				mRadOverlay.AddOverlayItem(pp, "告警点", "告警点信息", this.getResources().getDrawable(R.drawable.delete_icon));
				cnt++;
			} else {
				mRadOverlay.AddOverlayItem(pp, "辐射点", "辐射点信息", null);
			}
		}
		
		if ( pAlarm != null ) {
			if ( pAlarm.maxval < 20000 ) {
				// 延迟告警
				show_Long.Do();
			} else {
				show_Short.Do();
			}
			Toast.makeText(this, "您附近有"+cnt+"个风险点", Toast.LENGTH_LONG).show();
		} else {
			// test
			//show_Long.Do();
			//show_Short.Do();
		}
		
		// 显示当前位置
		GeoPoint pp = BMapUtil.genGeoPoint(mLat, mLon);
		mRadOverlay.AddOverlayItem(pp, "当前位置", "当前位置", this.getResources().getDrawable(R.drawable.nav_turn_via_1));
		
		lastLat = mLat;
		lastLon = mLon;
	}
	
	ThreadShow show_Short = new ThreadShow(10000, 2);
	ThreadShow show_Long = new ThreadShow(30000, 1);
	static int alarm_cnt = 0;
	
	Handler handler = new Handler() {  
        public void handleMessage(Message msg) {  
            switch(msg.what) {  
            case 1:
            	// 在提示栏提示告警
    			showDefaultNotification("您附近存在低量辐射", "请注意多活动、远离", R.drawable.arrow+(alarm_cnt++));
    			break;
            case 2:
            	showDefaultNotification("您附近存在高量辐射", "请即刻远离", R.drawable.icon_gcoding+(alarm_cnt++));
            	break;
            default:
            	break;
            }  
        };  
    };
    
    // 线程类  
    private class ThreadShow implements Runnable {
    	private int delay = 0;
    	private int id = 0;
    	private Thread last = null;
    	
    	public ThreadShow(int mill, int msgid) {
    		delay = mill;
    		id = msgid;
    	}
    	
    	public void Do() {
    		if ( last==null || !last.isAlive() ) {
    			last = new Thread(this);
    			last.start();
    		} else {
    			//Log.d("test", "test");
    		}
    	}
  
        @Override  
        public void run() {  
            // 定时发消息
            do {  
                try {  
                    Thread.sleep(delay);  
                    Message msg = handler.obtainMessage();
                    msg.what = id;  
                    handler.sendMessage(msg);  
                } catch (Exception e) {
                    e.printStackTrace();  
                }  
            } while (false); 
        }  
    }
    
	private void showDefaultNotification(String title, String content, int id) {
		Notification noti = new Notification();
		
		noti.icon = R.drawable.icon_geo;
		noti.tickerText = title;
		noti.when = System.currentTimeMillis();
		
		noti.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		
		Intent toIntent = new Intent(this, RadiationMainMap.class);
		toIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);// 返回之前的activity，不要创建新的
		
		/*
		 * 其中PendingIntent中的PendingIntent.FLAG_UPDATE_CURRENT属性的作用是如果我在从系统中提取一个PendingIntent，
		 * 而系统中有一个和你描述的PendingIntent对等的PendingInent, 那么系统会直接返回和该PendingIntent其实是同一token
		 * 的PendingIntent，而不是一个新的token的PendingIntent。如果我们使用了FLAG_UPDATE_CURRENT的话，新的Intent会更
		 * 新之前PendingIntent中的Intent对象数据，当然也会更新Intent中的Extras。
		 */
		PendingIntent contentIntent = PendingIntent.getActivity(RadiationMainMap.this, 0, toIntent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		noti.setLatestEventInfo(RadiationMainMap.this, title, content, contentIntent);
		//noti.contentIntent = contentIntent;
		
		NotificationManager notiM = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notiM.notify(id, noti);
	}
}
