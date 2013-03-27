package com.yunjian.v2.yunjian_json;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Yunjian_json extends Activity {
	private TextView _info;
	private final static String _url = "http://106.187.41.90/zone_supervisor/sessions.json";
	private final static String _testagent = "351554052661692@460018882023767@0.14@android42";
	private NameValuePair _sess_key;
	private JSONObject _json_data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_yunjian_json);
		
		_json_data = null;
		_sess_key = null;
		_info = (TextView)findViewById(R.id.textView_rst);
		((Button)findViewById(R.id.btn_showlst)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				GetJson proc = new GetJson();
				// TODO Auto-generated method stub
				if ( _sess_key != null ) {
					//proc.initCookies(_sess_key.getName(), _sess_key.getValue(), "106.187.41.90");
					proc.initCookies("remember_token", _sess_key.getValue(), "106.187.41.90");
					String url = "http://106.187.41.90/zone_supervisor_zones.json";
					proc.execute(url, _testagent, "get");
				}
			}
		});
		
		((Button)findViewById(R.id.btn_camera)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent camera_activity = new Intent(Yunjian_json.this, Camera.class);
				Yunjian_json.this.startActivity(camera_activity);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		GetJson proc = new GetJson();
		proc.initHeaders("Content-Type", "application/json");
		if ( _sess_key != null ) {
			//proc.initCookies(_sess_key.getName(), _sess_key.getValue(), "106.187.41.90");
			proc.initCookies("remember_token", _sess_key.getValue(), "106.187.41.90");
			String url = "http://106.187.41.90/zone_supervisor_zones.json";
			proc.execute(url, _testagent, "get");
		} else {
			_json_data = new JSONObject();
			try {
				JSONObject sess = new JSONObject();
				sess.put("name", "ceshi001");
				sess.put("password", "8");
				_json_data.put("session", sess);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				_info.append("form json error: "+e.toString());
			}

			proc.execute(_url, _testagent, "post");
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_yunjian_json, menu);
		return true;
	}
	
	private class GetJson extends GetInfoTask {
		@Override
		protected void initPostValues() {
			//sess_params.add(new BasicNameValuePair("","{\"session\":{\"name\":\"ceshi001\",\"password\":\"8\"}}"));
			//post_param = "{\"session\":{\"name\":\"ceshi_ning\",\"password\":\"8\"}}";
			if ( _json_data != null ) {
				post_param = _json_data.toString();
			}
		}
		
		@Override
		protected void onPostExecGet(Boolean succ) {
			_info.setText("Get "+this.getHttpCode()+"\n");
			if ( succ ) {
				String data = this.toString();
				//_info.append("get result: \n"+data);
				JSONTokener jsParser = new JSONTokener(data);
				JSONObject ret = null;
				try {
					ret = (JSONObject)jsParser.nextValue();
					int total = ret.getInt("total_entries");
					_info.append("总量: "+total+"\n");
					if ( total > 0 ) {
						JSONArray orglst = ret.getJSONArray("zones");
						_info.append("zone_id: "+orglst.getJSONObject(0).getInt("zone_admin_id")+"\n");
						_info.append("name: "+orglst.getJSONObject(0).getString("name")+"\n");
						_info.append("des: "+orglst.getJSONObject(0).getString("des")+"\n");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					try {
						JSONObject err = ret.getJSONObject("errors");
						_info.append("errmsg \""+err.getJSONArray("base").getString(0)+"\"");
						_info.append("\ncookies: "+_sess_key.getName()+" "+_sess_key.getValue()+"\n");
					} catch (JSONException ee) {
						_info.append("json error: "+ee.toString()+"\n"+"ret: "+data);
					}
				}
			} else {
				_info.append("get errmsg: \n"+_errmsg);
			}
		}
		
		@Override
		protected void onPostExecPost(Boolean succ) {
			_info.setText("Post "+this.getHttpCode()+"\n");
			if ( succ ) {
				//_info.append("result: "+this.getHttpCode()+"\n"+this.toString());
				JSONTokener jsParser = new JSONTokener(this.toString());
				JSONObject ret = null;

				try {
					ret = (JSONObject)jsParser.nextValue();
					_info.append("result \n"+ret.getString("token_key")+": "+ret.getString("token_value"));
					_sess_key = new BasicNameValuePair(ret.getString("token_key"), ret.getString("token_value"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					try {
						JSONObject err = ret.getJSONObject("errors");
						_info.append("errmsg \""+err.getJSONArray("base").getString(0)+"\"");
					} catch (JSONException ee) {
						_info.append("json error: "+ee.toString()+"\n");
						_info.append("to json: "+_json_data.toString());
					}
				}
				
			} else {
				_info.append("errmsg: \n"+_errmsg);
			}
		}
	}

}
