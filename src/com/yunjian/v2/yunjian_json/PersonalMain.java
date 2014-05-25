package com.yunjian.v2.yunjian_json;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.yunjian.v2.mapLocation.RadiationMainMap;
import com.yunjian.v2.mapLocation.ReportRadLocation;
import com.yunjian.v2.yunjian_json.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

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
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

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
	
 	private RatingBar mRb = null;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_personal);

		final View controlsView = findViewById(R.id.fullscreen_content);
		final View contentView = findViewById(R.id.main_layout);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
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

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
				//	mSystemUiHider.toggle();
				} else {
				//	mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		//findViewById(R.id.btn_find).setOnTouchListener(
			//	mDelayHideTouchListener);
		Button mb = (Button)findViewById(R.id.btn_find);
		mb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 切换到导航页
				Intent mapint = new Intent(PersonalMain.this, RadiationMainMap.class);
				startActivity(mapint);
			}	
		});
		
		mRb = (RatingBar)findViewById(R.id.rad_rate);
		mRb.setIsIndicator(true);
		mRb.setStepSize(1);
		mTrb = (TextView)findViewById(R.id.rad_info);
		mInfo = (TextView)controlsView;
		
		// 初始化辐射监控组件
		mAlarmListener = new RadiationAlarmListener() {
        	//private int mv_cnt = 0;
        	
			@Override
			public void onMove(double x, double y, double z) {
				// 提示移动中
			}

			@Override
			public void onAlarm(double x, double y, double z) {
				// 根据强度显示星星
				double max_avg = x>y ? x : y;
				max_avg = max_avg>z ? max_avg : z;
				
				int delayval = 0;
				
				if ( max_avg <= 1.5 ) {
					mRb.setRating(3.0f);
					mTrb.setText("小心");
					mInfo.setText("附近有辐射源，请小心");
					delayval = 3000;
				} else if ( max_avg <= 5 ) {
					mRb.setRating(4.0f);
					mTrb.setText("危险");
					mInfo.setText("附近辐射较强，请远离");
					delayval = 8000;
				} else {
					mRb.setRating(5.0f);
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
				
				delayedHide(delayval);
				mSystemUiHider.show();
			}
        };
        mAlarmCheck = new RadiationCheck(mAlarmListener, this);
        
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
		mRb.setRating(0.0f);
		mTrb.setText("安全");
		mInfo.setText("周围安全，请放心");
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
}

class RadiationAlarmModel {

	public RadiationAlarmModel() {}
	
	
}
