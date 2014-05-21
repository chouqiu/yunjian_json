package com.yunjian.v2.mapLocation;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.yunjian.v2.API.GetTask;
import com.yunjian.v2.API.JsonHelper;

public class GetRadiationList implements TaskListener {
	private long mLastUpdateTimestamp = 0;
	private GeoPoint mCenter = null;
	private ArrayList<RadPoint> mList = new ArrayList<RadPoint>();
	private GetRadiationListener mListener = null;
	
	public GetRadiationList(GetRadiationListener r) {
		mListener = r;
	}
	
	public void getList(GeoPoint p) {
		mCenter = p;
		mLastUpdateTimestamp = System.currentTimeMillis() / 1000;
		GetRadiationTask t = new GetRadiationTask(mCenter, mLastUpdateTimestamp, this);
		t.go();
	}

	@Override
	public void onGetList(String json) {
		//{list:[{lasttime:443434,lat:23.332,lon:44.342,maxval:65,report_count:8}],reqtime:234234}
		try {
			String rst = JsonHelper.getString(new JSONObject(json), "errorMsg");
			JSONObject lst = new JSONObject(rst);
			JSONArray arr = JsonHelper.getArray(lst, "list");
			long tm = JsonHelper.getLong(lst, "reqtime");
			
			if ( tm != mLastUpdateTimestamp ) {
				// 老的数据不更新
				Log.d("GetList", "invalid lasttime:"+tm+"//mLastUpdateTimestamp");
				return;
			}
			
			for ( int i=0; i<arr.length(); ++i ) {
				JSONObject obj = arr.getJSONObject(i);
				mList.add(new RadPoint(JsonHelper.getLong(obj, "lasttime"),
						JsonHelper.getDouble(obj, "lat"),
						JsonHelper.getDouble(obj, "lon"),
						JsonHelper.getInt(obj, "maxval"),
						JsonHelper.getInt(obj, "report_count") ));
			}
			
			mListener.onGetList(mList);
			
		} catch (JSONException e) {
			// 解析json错误
			Log.d("GetList", "invalid json result:"+json);
		}
	}
}

interface TaskListener {
	abstract public void onGetList(String json);
}

class GetRadiationTask extends GetTask {
	private GeoPoint mCurPoint;
	private TaskListener mL;
	private long mLast = 0;
	public static final double RAD = 1000.0; // 获取方圆多少m内的辐射点，单位m
	
	public GetRadiationTask(GeoPoint p, long last, TaskListener l) {
		mCurPoint = p;
		mL = l;
		mLast = last;
	}
	
	@Override
	protected String getApiUrl() {
		// 获取附近辐射点列表的url
		return "http://183.60.118.96/monitor/monG.cgi?lat="+mCurPoint.getLatitudeE6()/1E6+
				"&lon="+mCurPoint.getLongitudeE6()/1E6+"&radius="+RAD+"&last="+mLast;
	}

	@Override
	public void go() {
		// 拉取数据
		execute();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if ( result == true )
			mL.onGetList(this.getResult());
	}

	
}
