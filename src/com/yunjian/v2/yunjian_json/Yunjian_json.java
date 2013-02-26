package com.yunjian.v2.yunjian_json;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class Yunjian_json extends Activity {
	private TextView _info;
	private final static String _url = "http://106.187.41.90/worker/sessions.json";
	private final static String _testagent = "351554052661692@460018882023767@0.14@android42";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_yunjian_json);
		
		_info = (TextView)findViewById(R.id.textView_rst);
		GetJson proc = new GetJson();
		proc.initHeaders("Content-Type", "application/json");
		
		proc.execute(_url, _testagent, "post");
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
			post_param = "{\"session\":{\"name\":\"ceshi001\",\"password\":\"8\"}}";
		}
		
		@Override
		protected void onPostExecGet(Boolean succ) {
			_info.setText("Get "+this.getHttpCode()+"\n");
			if ( succ ) {
				_info.append("result: \n"+this.toString());
			} else {
				_info.append("errmsg: \n"+_errmsg);
			}
		}
		
		@Override
		protected void onPostExecPost(Boolean succ) {
			_info.setText("Post "+this.getHttpCode()+"\n");
			if ( succ ) {
				//_info.append("result: "+this.getHttpCode()+"\n"+this.toString());
				JSONTokener jsParser = new JSONTokener(this.toString());

				try {
					JSONObject ret = (JSONObject)jsParser.nextValue();
					JSONObject err = ret.getJSONObject("errors");
					if ( err != null ) {
						_info.append("errmsg \""+err.getJSONArray("base").getString(0)+"\"");
					} else {
						_info.append("result \n"+ret.getString("token_key")+": "+ret.getString("token_value"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					_info.append("json error: "+e.toString());
				}
				
			} else {
				_info.append("errmsg: \n"+_errmsg);
			}
		}
	}

}
