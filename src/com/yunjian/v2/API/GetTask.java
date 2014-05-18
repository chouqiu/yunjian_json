package com.yunjian.v2.API;


import android.os.AsyncTask;

public abstract class GetTask extends  AsyncTask<String, Integer, Boolean>{

	private JsonHttpRequest mJsonHttpRequest				=	null;
	public GetTask()
	{
		mJsonHttpRequest			=	new JsonHttpRequest();
	}
	protected abstract String getApiUrl();
	public    abstract void   go();
	@Override
	protected Boolean doInBackground(String... params) {
		return mJsonHttpRequest.get(getApiUrl());
	}
	
	public String getResult()
	{
		return mJsonHttpRequest.getResult();
	}
	public String getErrorMsg()
	{
		return mJsonHttpRequest.getErrorMsg();
	}
	public void setCookie(String key,String val,String domain)
	{
		mJsonHttpRequest.setCookie(key, val, domain);
	}
}
