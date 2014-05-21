package com.yunjian.v2.API;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class JsonHelper {
	private JSONObject mObj = null;
	private final String TAG			     = JsonHelper.class.getName();
	
	public static String getString(JSONObject o,String k)
	{
		if (o == null)
			return "";

		try {
			return o.getString(k);
		} catch (JSONException e) {
			return "";
		}
	}
	
	public static float getFloat(JSONObject o,String k)
	{
		if (o == null)
			return -1f;
		
		try {
			return (float) o.getDouble(k);
		} catch (JSONException e) {
			return -1f;
		}
	}
	
	public static long getLong(JSONObject o,String k)
	{
		if (o == null)
			return -1l;
		try {
			return o.getLong(k);
		} catch (JSONException e) {
			return -1;
		}
	}
	public static int getInt(JSONObject o,String k)
	{
		if (o == null)
			return -1;
		try {
			return o.getInt(k);
		} catch (JSONException e) {
			return -1;
		}
	}
	public static double getDouble(JSONObject o,String k)
	{
		if (o == null)
			return -1f;
		
		try {
			return  o.getDouble(k);
		} catch (JSONException e) {
			return -1f;
		}
	}
	
	public static JSONArray getArray(JSONObject o, String k)
	{
		if (o == null)
			return null;
		
		try {
			return o.getJSONArray(k);
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static JSONObject getObject(JSONObject o, String k)
	{
		if (o == null)
			return null;
		
		try {
			return o.getJSONObject(k);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setString(String k, String v) {
		if ( mObj == null ) mObj = new JSONObject();
		try {
			mObj.put(k, v);
		} catch (JSONException e) {
			Log.e(TAG,"获取(设置)数据出错["+k+"]:["+v+"]");
		}
	}
	
	public void setLong(String k, long v) {
		if ( mObj == null ) mObj = new JSONObject();
		try {
			mObj.put(k, v);
		} catch (JSONException e) {
			Log.e(TAG,"获取(设置)数据出错["+k+"]:["+v+"]");
		}
	}
	
	public void setDouble(String k, double v) {
		if ( mObj == null ) mObj = new JSONObject();
		try {
			mObj.put(k, v);
		} catch (JSONException e) {
			Log.e(TAG,"获取(设置)数据出错["+k+"]:["+v+"]");
		}
	}
	
	public void setArray(String k, ArrayList<?> lst) {
		if ( mObj == null ) mObj = new JSONObject();
		try {
			JSONArray arr = new JSONArray();
			Iterator<?> it = lst.iterator();
			while ( it.hasNext() ) {
				arr.put(it.next());
			}
			
			mObj.put(k, arr);
		} catch (JSONException e) {
			Log.e(TAG,"获取(设置)数据出错["+k+"]");
		}
	}
	
	public String toString() {
		if ( mObj != null ) {
			return mObj.toString();
		} else {
			return null;
		}
	}
}
