package com.yunjian.v2.yunjian_json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.yunjian.v2.API.AlarmBeep;
import com.yunjian.v2.mapLocation.RadiationMainMap;
import com.yunjian.v2.mapLocation.ReportRadLocation;
import com.yunjian.v2.timeline.main.OneStatusEntity;
import com.yunjian.v2.timeline.main.StatusExpandAdapter;
import com.yunjian.v2.timeline.main.TwoStatusEntity;
import com.yunjian.v2.yunjian_json.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.PopupWindow;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.app.ActionBar.LayoutParams; 

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class PersonalMain extends Activity implements BDLocationListener {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = false;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	/**
	 * 辐射监控组件
	 */
	private RadiationCheck mAlarmCheck = null;
	private RadiationAlarmListener mAlarmListener = null;
	
 	//private RatingBar mRb = null;
 	private TextView mTrb = null;
 	private TextView mInfo = null;
 	
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
	
	private View popView;
	private PopupWindow popWin;
	private Button mbr;
	
	/**
	 * 时间轴视图
	 */
	private List<OneStatusEntity> oneList;
	private ExpandableListView expandlistView;
	private StatusExpandAdapter statusAdapter;
	private Context context;
	private final static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private final static SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_personal);

		final View controlsView = findViewById(R.id.fullscreen_content);
		final View contentView = findViewById(R.id.main_layout);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				0);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});
		mSystemUiHider.show();

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
			 	if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			 	
			 	if ( popWin.isShowing() ) {
			 		popWin.dismiss();
			 	}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		//findViewById(R.id.btn_find).setOnTouchListener(
			//	mDelayHideTouchListener);
		
		// 发现
		((ImageButton)findViewById(R.id.imgbtn_find)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 切换到导航页
				Intent mapint = new Intent(PersonalMain.this, RadiationMainMap.class);
				startActivity(mapint);
			}	
		});
		
		// 上报
		popView = ((LayoutInflater)PersonalMain.this.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
				inflate(R.layout.activity_pop_report, null);
		popWin = new PopupWindow(popView,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		//popWin.setTouchable(true);
		
		mbr = (Button)findViewById(R.id.btn_rpt);
		mbr.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 弹出popwin
				if ( ! popWin.isShowing() ) {
					int[] loc = new int[2];
					//popWin.showAsDropDown(mbr);
					//int a = mbr.getLeft();
					//int b = mbr.getTop();
					mbr.getLocationInWindow(loc);
					popWin.showAtLocation(mbr, Gravity.NO_GRAVITY, loc[0]-20, loc[1]-popView.getHeight()-100);
				} else {
					popWin.dismiss();
				}
			}  
		});
		
		//mRb = (RatingBar)findViewById(R.id.rad_rate);
		//mRb.setIsIndicator(true);
		//mRb.setStepSize(1);
		mTrb = (TextView)findViewById(R.id.title_stat);
		mInfo = (TextView)controlsView;
		
		// 初始化辐射监控组件
		mAlarmListener = new RadiationAlarmListener() {
        	//private int mv_cnt = 0;
			@Override
			public void onMove(double x, double y, double z, AlarmBeep alarms) {
				// 提示移动中
			}

			@Override
			public void onAlarm(double x, double y, double z) {
				// 根据强度显示星星
				double max_avg = x>y ? x : y;
				max_avg = max_avg>z ? max_avg : z;
				
				int delayval = 0;
				
				if ( max_avg <= 1.5 ) {
					addOneItem("低辐射");
					//mRb.setRating(3.0f);
					//mTrb.setText("小心");
					mInfo.setText("附近有辐射源，请小心");
					delayval = 3000;
				} else if ( max_avg <= 5 ) {
					//mRb.setRating(4.0f);
					addOneItem("中等辐射");
					mTrb.setText("危险");
					mInfo.setText("附近辐射较强，请远离");
					delayval = 8000;
				} else {
					//mRb.setRating(5.0f);
					addOneItem("高危辐射");
					mTrb.setText("严重");
					mInfo.setText("附近辐射严重，速走");
					delayval = 13000;
				}
				
				// 上报告警
				if ( mLat != 0f || mLon != 0f ) {
					ReportRadLocation r = new ReportRadLocation(PersonalMain.this.getApplicationContext());
					r.setReportParam(mLat, mLon, x, y, z, ReportRadLocation.TYPE_MF);
					r.go();
				}
				
				statusAdapter.notifyDataSetChanged();
				
				delayedHide(delayval);
				mSystemUiHider.show();
			}

			@Override
			public void onRadiationChange(double x, double y, double z, double fangcha, 
					int isAlarm, AlarmBeep alarm) {
				// 处理告警
				if ( isAlarm > 0 ) {
					alarm.playBeep(0.3f);
	            	alarm.playVibrator();
	            	addOneItem("电磁辐射");
	            	
	            	// 上报告警
					if ( mLat != 0f || mLon != 0f ) {
						ReportRadLocation r = new ReportRadLocation(PersonalMain.this.getApplicationContext());
						r.setReportParam(mLat, mLon, x, y, z, ReportRadLocation.TYPE_MF);
						r.go();
					}
					statusAdapter.notifyDataSetChanged();
					expandlistView.setSelection(0);
				}
			}
			
        };
        mAlarmCheck = new RadiationCheck(mAlarmListener, this);
        
        context = this;
		expandlistView = (ExpandableListView) findViewById(R.id.expandlist);
		
		
		
		putInitData();
		
		statusAdapter = new StatusExpandAdapter(context, oneList);
		//billAdapter = new BizAccountBillAdapter(this);
		
		expandlistView.setAdapter(statusAdapter);
		expandlistView.setGroupIndicator(null); // 去掉默认带的箭头

		// 遍历所有group,将所有项设置成默认展开
		int groupCount = expandlistView.getCount();
		for (int i = 0; i < groupCount; i++) {
			expandlistView.expandGroup(i);
		}
		expandlistView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				// TODO Auto-generated method stub
				return true;
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
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
			resetAlarmStat();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	private void resetAlarmStat() {
		//mRb.setRating(0.0f);
		//mTrb.setText("安全");
		mInfo.setText("周边安全");
	}

	@Override
	protected void onPause() {
		// 取消告警
		mAlarmCheck.unRegisterDevice();
		super.onPause();
	}

	@Override
	protected void onResume() {
		int reqret = 0;
        if (mLocationClient != null && mLocationClient.isStarted())
        	  reqret = mLocationClient.requestLocation();
        else 
        	 Log.d("LocSDK3", "locClient is null or not started");
        
        if ( reqret != 0 ) {
        	Log.d("LocSDK3", "locClient req failed: "+reqret);
        }
		
     // 获取配置参数
		SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(this);
		int maxval = shp.getInt("radVal", 9000);
		int magcnt = Integer.parseInt(shp.getString("magList", "1"));
		int alarmval = shp.getInt("alarmVal", 12);
		boolean moveable = shp.getBoolean("moveable", false);
		
		Log.d("radpref", "get max: "+maxval+" magcnt: "+magcnt+"alrm: "+alarmval+" mv: "+moveable);
		
		//registerReceiver(mBR, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		mAlarmCheck.setMaxFangcha(maxval);
		mAlarmCheck.setMagLimit(magcnt);
		mAlarmCheck.setAlarmLimit(alarmval);
		mAlarmCheck.setMoveable(moveable);
		mAlarmCheck.registerDevice();
		
		// 注册告警
		mAlarmCheck.registerDevice();
		super.onResume();
	}
	
	@Override
	public void onReceiveLocation(BDLocation arg0) {
		// 接收定位消息
		if ( arg0 == null ) {
			return;
		}
		
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
	}

	@Override
	public void onReceivePoi(BDLocation arg0) {
		// 什么不做
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// 添加测试菜单
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 1, "Debug页面");
		menu.add(0, 2, 1, "配置");
		//menu.add(0, 3, 1, "定位当前位置");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 跳到老的页面上
		switch (item.getItemId()) {
		case 1:
			Intent old_activity = new Intent(PersonalMain.this, Yunjian_json.class);
			this.startActivity(old_activity);
			break;
		case 2:
			Intent ss_activity = new Intent(PersonalMain.this, Settings.class);
			this.startActivity(ss_activity);
			break;
		case 3:
			//setLocationCenter(mLat, mLon, null);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	private void putInitData() {
		String[] strArray = new String[]{"变电站", "发射塔", "地震"};
		//String[] str1 = new String[]{"附近有变电站，请远离", "附近有发射塔，请远离", "云南曲靖发生5.2级地震，对您暂无影响"};
		String[] str1 = new String[]{"2013-11-01", "2013-12-02", "2014-05-02", "2014-05-16", "2014-06-02"};
		//String[] str2 = new String[]{"您附近有低量辐射，请注意活动", "您附近有堵车，请注意", "您附近有传染病医院，请注意", "您附近道路有积水，请小心驾驶", "您附近空气质量不佳，请远离"};
		String[] str2 = new String[]{"低辐射", "堵车", "传染医院", "道路积水", "空气质量"};
		//String[] str3 = new String[]{"买方到银行抵押手续", "买方取他向权利证", "银行给卖方划尾款", "全部办结"};
		
		String[] timeStr2 = new String[]{"09:02", "13:16", 
				"08:24", "10:13", "13:18", 
				"17:55", "20:38", "23:32"};
		//String[] timeStr3 = new String[]{"", "", "", ""};
		
		oneList = new ArrayList<OneStatusEntity>();
		List<TwoStatusEntity> twoList = new ArrayList<TwoStatusEntity>();
		for ( int i=0, j=0, k=0; i<timeStr2.length; i++ ) {
			switch ( i ) {
			case 1:
			case 4:
			case 7:
				OneStatusEntity one = new OneStatusEntity();
				one.setStatusName(strArray[j]);
				one.setCompleteTime(str1[j]);
				one.setEventName(timeStr2[i]);
				one.setTwoList(twoList);
				j++;
				oneList.add(one);
				twoList = new ArrayList<TwoStatusEntity>();
				break;
			default:
				TwoStatusEntity two = new TwoStatusEntity();
				two.setStatusName(str2[k]);
				two.setCompleteTime(str1[k]);
				two.setEventName(timeStr2[i]);
				two.setIsfinished(true);
				k++;
				twoList.add(two);
				break;
			}
		}
		
	}
	
	private OneStatusEntity addOneItem(String title) {
		OneStatusEntity one = new OneStatusEntity();
		one.setStatusName(title);
		
		Date now = new Date();
		one.setCompleteTime(df.format(now));
		one.setEventName(tf.format(now));
		one.setTwoList(new ArrayList<TwoStatusEntity>());
		
		oneList.add(0, one);
		
		return one;
	}
	
	private void addTwoItem(String title) {
		OneStatusEntity one = addOneItem("");
		
		TwoStatusEntity two = new TwoStatusEntity();
		two.setStatusName(title);
		two.setCompleteTime(one.getCompleteTime());
		two.setEventName(one.getEventName());
		two.setIsfinished(true);
		one.getTwoList().add(0, two);
	}
}
