package com.yunjian.v2.yunjian_json;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class GetInfoTask extends AsyncTask<String, Integer, Boolean> {
	protected String _errmsg;
	protected List<NameValuePair> sess_params;
	protected String post_param;
	protected HttpContext hcon;
	public byte[] result;
	protected String _type;
	protected CookieStore cs;
	private List<Header> _headers;
	protected HttpResponse _httpResp;
	
	GetInfoTask() {
		cs = new BasicCookieStore();
		sess_params = new ArrayList<NameValuePair>();
		post_param = new String();
		_headers = new ArrayList<Header>();
	}
	
	protected void initPostValues() {
		//sess_params.add(new BasicNameValuePair("session[name]",mEmail));
	}
	
	protected void initCookies(String key, String val, String domain) {
		BasicClientCookie bc1 = new BasicClientCookie(key, val);
		bc1.setVersion(0);
        bc1.setDomain(domain);
        bc1.setPath("/");
        cs.addCookie(bc1);
	}
	
	protected void initHeaders(String Key, String Val) {
		_headers.add(new BasicHeader(Key, Val));
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		// TODO: attempt authentication against a network service.
		String urlstr = params[0];
		String useragent = params[1];
		_type = new String(params[2]);
		
		_errmsg = new String();
		
		try {
			hcon = new BasicHttpContext();
			hcon.setAttribute(ClientContext.COOKIE_STORE, cs);
			
			HttpParams httpparam = new BasicHttpParams();
			HttpProtocolParams.setUserAgent(httpparam, useragent);
			
			
			if ( _type.equals("get") ) {
				HttpGet httpRequest = new HttpGet(urlstr);
				_httpResp = new DefaultHttpClient(httpparam).execute(httpRequest, hcon);
				for ( Header hh : _headers ) {
					httpRequest.setHeader(hh);
				}
				
				result = EntityUtils.toByteArray(_httpResp.getEntity());
			} else {
				initPostValues();
				HttpPost httpRequest = new HttpPost(urlstr);
				for ( Header hh : _headers ) {
					httpRequest.setHeader(hh);
				}
				if ( sess_params.size() > 0 ) {
					httpRequest.setEntity(new UrlEncodedFormEntity(sess_params,"UTF-8"));
				} else if ( post_param.length() > 0 ) {
					httpRequest.setEntity(new StringEntity(post_param,"UTF-8"));
				}
				
				_httpResp = new DefaultHttpClient(httpparam).execute(httpRequest, hcon);
				result = EntityUtils.toByteArray(_httpResp.getEntity());
			}

		} catch ( Exception e ) {
			_errmsg = "stage 3: "+e.toString();
			return false;
		}

		/*
		for (String credential : DUMMY_CREDENTIALS) {
			String[] pieces = credential.split(":");
			if (pieces[0].equals(mEmail)) {
				// Account exists, return true if the password matches.
				return pieces[1].equals(mPassword);
			}
		}
		*/

		// TODO: register the new account here.
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean succ) {
		//showProgress(false);
		
		if ( _type.equals("get") ) {
			onPostExecGet(succ);
			// TODO: get data
			//Bundle sess_data = new Bundle();
			//sess_data.putString("html", result);
			//Intent yunjianIntent = new Intent(LoginActivity.this,NetAppActivity.class);
			//yunjianIntent.putExtras(sess_data);
			
			//startActivity(yunjianIntent);
		} else {
			onPostExecPost(succ);
			// TODO: get data faild
		}
	}

	@Override
	protected void onCancelled() {
		//showProgress(false);
	}
	
	protected void onPostExecGet( Boolean succ ) {}
	protected void onPostExecPost( Boolean succ ) {}
	
	public String toString() {
		return new String(result);
	}
	
	public byte[] toByte() {
		return result;
	}
	
	public int getHttpCode() {
		if ( _httpResp != null ) {
			return _httpResp.getStatusLine().getStatusCode();
		} else {
			return -1;
		}
	}
}
