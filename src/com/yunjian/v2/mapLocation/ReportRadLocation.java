package com.yunjian.v2.mapLocation;

import java.util.ArrayList;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.yunjian.v2.API.JsonHelper;
import com.yunjian.v2.API.PostTask;


public class ReportRadLocation extends PostTask {
	private String mIMEI;
	private JsonHelper mJson = null;
	
	public final static String TYPE_MF = "MF";
	public final static String TYPE_EF = "EF";
	public final static String TYPE_RF = "RF";
	
	public ReportRadLocation(Context con) {
		super();
		TelephonyManager tm = (TelephonyManager)con.getSystemService(Context.TELEPHONY_SERVICE);
		mIMEI = tm.getDeviceId();
		if ( mIMEI == null ) {
			mIMEI = "default imei";
		}
	}
	
	public void setReportParam(double lat, double lon, double diffx, double diffy, double diffz, String type) {
		mJson = new JsonHelper();
		mJson.setString("IMEI", mIMEI);
		mJson.setDouble("lat", lat);
		mJson.setDouble("lon", lon);
		
		ArrayList<Double> arr = new ArrayList<Double>();
		arr.add(diffx);
		arr.add(diffy);
		arr.add(diffz);
		
		mJson.setArray("diff_val", arr);
		mJson.setString("type", type);
		mJson.setLong("time", System.currentTimeMillis() / 1000);
	}
	
	@Override
	protected String getPostParams() {
		// {IMEI:"2342sdf",lat:32.323,lon:43.343,time:1400923923,diff_val:[1.5,2.7,4.9],type:"MF/EF/RF"}
		if ( mJson != null ) {
			return mJson.toString();
		}
		return null;
	}

	@Override
	protected String getApiUrl() {
		// 返回上报链接
		return "http://183.60.118.96/monitor/monR.cgi";
	}

	@Override
	public void go() {
		// 执行上报
		execute();
	}
	
}
