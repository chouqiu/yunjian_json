package com.yunjian.v2.yunjian_json;

import java.text.DecimalFormat;

import com.yunjian.v2.API.AlarmBeep;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;

public class Yunjian_json extends Activity {
	private TextView _info, _mag;
	//private final static String _url = "http://106.187.41.90/zone_supervisor/sessions.json";
	//private final static String _testagent = "351554052661692@460018882023767@0.14@android42";
	//private NameValuePair _sess_key;
	//private JSONObject _json_data;
	//private BroadcastReceiver mBR = null;
	
	private RadiationAlarmListener mAlarmListener = null;
	private RadiationCheck mChecker = null;
	
	public DecimalFormat df = new DecimalFormat("##0.00");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_yunjian_json);
		
		//_json_data = null;
		//_sess_key = null;
		_info = (TextView)findViewById(R.id.textView_rst);
		_mag = (TextView)findViewById(R.id.textView_mag);
		
		// 设置滚动
		_mag.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		((Button)findViewById(R.id.btn_showlst)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//GetJson proc = new GetJson();
				// TODO Auto-generated method stub
				//if ( _sess_key != null ) {
					//proc.initCookies(_sess_key.getName(), _sess_key.getValue(), "106.187.41.90");
					//proc.initCookies("remember_token", _sess_key.getValue(), "106.187.41.90");
					//String url = "http://106.187.41.90/zone_supervisor_zones.json";
					//proc.execute(url, _testagent, "get");
				//}
				_mag.setText("");
			}
		});
		
		//((Button)findViewById(R.id.btn_showlst)).setVisibility(View.INVISIBLE);
		
		((Button)findViewById(R.id.btn_camera)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//Intent camera_activity = new Intent(Yunjian_json.this, Camera.class);
				//Yunjian_json.this.startActivity(camera_activity);
				
				// 起停监测
				if ( mChecker.isActive() ) {
					mChecker.unRegisterDevice();
				} else {
					mChecker.registerDevice();
				}
			}
		});
		
		mAlarmListener = new RadiationAlarmListener() {
			@Override
			public void onMove(double x, double y, double z, AlarmBeep alarm) {
				// 提示在移动
				//_info.setTextColor(Color.argb(255, 0, 0, 255));
				/**
				 * 默认再恢复为黑色
				 */
				//_info.setTextColor(Color.argb(255, 0, 0, 0));
				//_info.setText("手机移动情况["+acc_flag+"]："+accDiff+"\n");
			}

			@Override
			public void onAlarm(double x, double y, double z) {
				// 废弃
			}

			@Override
			public void onRadiationChange(double x, double y, double z,
					double fangcha, int isAlarm, AlarmBeep alarm) {
				// 设置告警
				if ( isAlarm > 0 ) {
					_info.setTextColor(Color.argb(255, 255, 0, 0));
	            	alarm.playBeep(0.3f);
	            	alarm.playVibrator();
					/*
					if ( fangcha < 50000 ) {
						alarm.do_play(AlarmBeep.MSG_ALARM_LOW);
					} else {
						alarm.do_play(AlarmBeep.MSG_ALARM_HIGH);
					}
					*/
				} else {
					_info.setTextColor(Color.argb(255, 0, 0, 0));
					//_mag.setTextColor(Color.argb(255, 0, 0, 0));
				}
            	
        		_info.setText("电磁异常告警["+isAlarm+"]\n"+df.format(x)+"//"+df.format(y)+"//"+df.format(z)+"\n");
        		_info.append("方差："+df.format(fangcha)+"\n———————————————————\n");
        		_info.append("告警阈值："+df.format(RadiationCheck.getAlarmLimit()));
        		_info.append("  方差阈值："+RadiationCheck.getMaxFangcha());
        		_info.append("  检测移动："+RadiationCheck.isMOVEABLE());
        		_info.append("  监测磁轴数："+RadiationCheck.getMagLimit());
        		
        		_mag.append("val["+isAlarm+"]："+df.format(x)+"/"+df.format(y)+"/"+df.format(z)+"\n");
        				//+"||"+df.format(x)+"/"+df.format(y)+"/"+df.format(z)+"\n");
                _mag.append("方差："+df.format(fangcha)+"\n");
                _mag.scrollTo(0, _mag.getHeight());
				
			}
			
		};
		mChecker = new RadiationCheck(mAlarmListener, this);
		
		//mBR = new BatteryReceiver();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// 获取配置参数
		SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(this);
		int maxval = shp.getInt("radVal", 9000);
		int magcnt = Integer.parseInt(shp.getString("magList", "1"));
		int alarmval = shp.getInt("alarmVal", 12);
		boolean moveable = shp.getBoolean("moveable", false);
		
		Log.d("radpref", "get max: "+maxval+" magcnt: "+magcnt+"alrm: "+alarmval+" mv: "+moveable);
		
		//registerReceiver(mBR, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		mChecker.setMaxFangcha(maxval);
		mChecker.setMagLimit(magcnt);
		mChecker.setAlarmLimit(alarmval);
		mChecker.setMoveable(moveable);
		mChecker.registerDevice();
		
		/*
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
		*/
	}
	
	@Override 
	//注意activity暂停的时候释放   
    protected void onPause() {      
        super.onPause();    
        mChecker.unRegisterDevice();
        //this.unregisterReceiver(mBR);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_yunjian_json, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 设置菜单
		switch(item.getItemId()) {
		case R.id.menu_settings:
			Intent ss = new Intent(Yunjian_json.this, Settings.class);
			this.startActivity(ss);
			break;
		default:
			super.onOptionsItemSelected(item);
			break;
		}
		return true;
	}
	
	/**
	 * 暂时不需要
	 * @author zhihui
	 *
	private class BatteryReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int current=intent.getExtras().getInt("level");//获得当前电量
            int total=intent.getExtras().getInt("voltage");//获得电压
            
            _mag.setTextColor(Color.argb(255, 0, 0, 0));
            _info.setText("现在的电压是"+total+"v, 电量:"+current+"\n\n");
            //ReadBatteryFile bf = new ReadBatteryFile(getApplicationContext());
    		//_info.append(new String(bf.get_current()));
    		//_info.append("\n\n");
        }
    }
     */
	
	/**
	 * 读取sysfs代码，暂时不用。
	 * @author zhihui
	 *
	private class ReadBatteryFile {
		//private Context _con;
		
		public ReadBatteryFile( Context context ) {
			super();
			//_con = context;
		}
		
		public byte[] get_current() {
			File f = new File("/sys/class/power_supply/battery/uevent");
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			
			try {
				FileInputStream fs;
				fs = new FileInputStream(f);
						
				byte[] buffer = new byte[1024];
				int len = 0;
				while((len = fs.read(buffer)) != -1){
					outStream.write(buffer, 0, len);
				}
				
				fs.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return outStream.toByteArray();
		}
	}
	*/
	
	/**
	 * 暂时不需要
	 * @author zhihui
	 *
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
					_info.append("鎬婚噺: "+total+"\n");
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
	 */
}
