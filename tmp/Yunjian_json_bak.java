package com.yunjian.v2.yunjian_json;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Yunjian_json extends Activity {
	private TextView _info, _mag;
	//private final static String _url = "http://106.187.41.90/zone_supervisor/sessions.json";
	private final static String _testagent = "351554052661692@460018882023767@0.14@android42";
	private NameValuePair _sess_key;
	private JSONObject _json_data;
	
	private final static int max_length = 10;
	private final static double alarm_limit = 1.2;
	/*摇晃检测的时间间隔100ms*/ 
	private static final int UPDATE_INTERVAL = 100;
	/*摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感。*/  
    private static final double SHAKE_SHRESHOLD = 70; 
    private static final int CONTINUOUS_ALARM_CNT = 1; // 连续n次达到告警标准时才告警
	
	private float[][] magneticFieldValues=new float[3][max_length];
	private double[] normal_avg = new double[3];
	private double[] current_avg = new double[3];
	private double[] current_rate = new double[3];
	private double[] alarm_avg = new double[3];
	/* 连续告警次数统计，削除误报 */
	private int alarm_stat = 0;
	private SensorManager sm=null;
	private Sensor mSensor = null, aSensor = null;
	private SensorEventListener myListener =null;
	private int idx = 0, acc_flag = 0, alarm_flag = 0;
	private float[] accLast = new float[3];
	private long accLastTime = 0;
	private double accDiff = 0.0f;
	
	private AlarmBeep alarmbeep;
	private BroadcastReceiver mBR = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_yunjian_json);
		
		_json_data = null;
		_sess_key = null;
		_info = (TextView)findViewById(R.id.textView_rst);
		_mag = (TextView)findViewById(R.id.textView_mag);
		
		alarmbeep = new AlarmBeep(this);
		
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
		
		((Button)findViewById(R.id.btn_showlst)).setVisibility(View.INVISIBLE);
		
		((Button)findViewById(R.id.btn_camera)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent camera_activity = new Intent(Yunjian_json.this, Camera.class);
				Yunjian_json.this.startActivity(camera_activity);
			}
		});
		
		myListener = new SensorEventListener(){
	        @Override   
	        public void onAccuracyChanged(Sensor sensor, int accuracy) {    
	            // TODO Auto-generated method stub    
	                 
	        }    
	     
	        @Override   
	        public void onSensorChanged(SensorEvent event) {
	        	if ( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
	        		double diff_X = event.values[0];
	        		double diff_Y = event.values[1];
	        		double diff_Z = event.values[2];
	        		
	            	/*手机晃动检测*/
	        		//_info.setText("晃动情况："+event.values[0]+"//"+event.values[1]+"//"+event.values[2]+"\n");
	        		
	                long currentTime=System.currentTimeMillis();  
	                if( accLastTime!=0 ) {  
	                    long diffTime=currentTime-accLastTime;  
	                    if(diffTime>UPDATE_INTERVAL){  
	                        diff_X -= accLast[0];  
	                        diff_Y -= accLast[1];  
	                        diff_Z -= accLast[2];
	                        
	                        accDiff=Math.sqrt(diff_X*diff_X+diff_Y*diff_Y+diff_Z*diff_Z)/diffTime*10000;  
	                        if(accDiff>SHAKE_SHRESHOLD){  
	                            acc_flag = 1; 
	                        }else{  
	                            acc_flag = 0;  
	                        }
	                        
	                        if ( alarm_flag == 0 && acc_flag == 0 ) {
	    	                	_info.setTextColor(Color.argb(255, 0, 0, 0));
	    	                } else if ( acc_flag > 0 ) {
	    	                	_info.setTextColor(Color.argb(255, 0, 0, 255));
	    	                } else if ( alarm_flag > 0 ){
	    	                	_info.setTextColor(Color.argb(255, 255, 0, 0));
	    	                	alarmbeep.playBeep();
	    	                	alarmbeep.playVibrator();
	    	                }
	                		_info.setText("手机移动情况["+acc_flag+"]："+accDiff+"\n");
	                		_info.append("电磁异常告警["+alarm_flag+"]："+alarm_avg[0]+"//"+alarm_avg[1]+"//"+alarm_avg[2]+"\n");
	                        
	                        accLastTime = currentTime;
	                    	accLast[0] = event.values[0];
	    	                accLast[1] = event.values[1];
	    	                accLast[2] = event.values[2];
	                    }
	                } else {
	                	accLastTime=currentTime;
	                }  
		        }
	        	
	            if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){ 
	                magneticFieldValues[0][idx]=event.values[0];
	                magneticFieldValues[1][idx]=event.values[1];
	                magneticFieldValues[2][idx]=event.values[2];
	                
	                current_avg[0] += event.values[0];
	                current_avg[1] += event.values[1];
	                current_avg[2] += event.values[2];
 
	                _mag.setText("磁场波动："+magneticFieldValues[0][idx]+"//"+magneticFieldValues[1][idx]+"//"+magneticFieldValues[2][idx]+"\n");
	                idx = (idx+1) % max_length;
	                
	                if ( idx == 0 ) {
	                	for( int j=0; j<3; j++ ) {
	                		double sum = 0.0f;
	                		current_avg[j] /= max_length;
	                		
		                	for( int i=0; i<max_length; ++i) {
		                		sum += Math.pow(magneticFieldValues[j][i]-current_avg[j], 2);
		                	}
		                	current_avg[j] = Math.sqrt(sum/max_length);
	                	}
	                	
	                	current_rate[0] = current_avg[0]/normal_avg[0];
	                	current_rate[1] = current_avg[0]/normal_avg[1];
	                	current_rate[2] = current_avg[0]/normal_avg[2];
	                	
	                	int limit_cnt = 0;
	                	limit_cnt += (current_avg[0]>alarm_limit ? 1 : 0);
	                	limit_cnt += (current_avg[1]>alarm_limit ? 1 : 0);
	                	limit_cnt += (current_avg[2]>alarm_limit ? 1 : 0); 
	                	
	                	if( normal_avg[0] == 0.0f && normal_avg[1] == 0.0f && normal_avg[2] == 0.0f ) {
	                		normal_avg[0] = current_avg[0];
	                		normal_avg[1] = current_avg[1];
	                		normal_avg[2] = current_avg[2];
	                		alarm_flag = 0;
	                	} else if ( acc_flag > 0 ) {
	                		// 保持手机静止
	                		//_mag.setTextColor(Color.argb(255, 0, 0, 255));
	                		//_info.append("请保持手机静止："+accLast[0]+"//"+accLast[1]+"//"+accLast[2]+"\n");
	                		alarm_flag = 0;
	                		alarm_stat = 0;
	                		
	                	} else if ( limit_cnt >= 2 ) {
	                		alarm_avg[0] = current_avg[0];
	                		alarm_avg[1] = current_avg[1];
	                		alarm_avg[2] = current_avg[2];
	                		if ( ++alarm_stat >= CONTINUOUS_ALARM_CNT ) {
	                			alarm_flag = 1;
	                		}
	                		
	                	} else {
	                		alarm_flag = 0;
	                		alarm_stat = 0;
	                		//if ( normal_avg[0]==0 || normal_avg[1]==0 || normal_avg[2]==0 ||
	                		//		(current_rate[0]<=1 && current_rate[1]<=1 && current_rate[2]<=1) ) 
	                		//{
	                			// 清除误报噪声
	                			normal_avg[0] = current_avg[0];
		                		normal_avg[1] = current_avg[1];
		                		normal_avg[2] = current_avg[2];
	                		//}
	                	}
	                	
	                	current_avg[0] = 0.0f;
	                	current_avg[1] = 0.0f;
	                	current_avg[2] = 0.0f;
	                }
	                
	                _mag.setTextColor(Color.argb(255, 0, 0, 0));
                	_mag.append("磁场波动方差["+idx+"]："+normal_avg[0]+"//"+normal_avg[1]+"//"+normal_avg[2]+"\n");
                	_mag.append("磁场波动比例："+current_rate[0]+"//"+current_rate[1]+"//"+current_rate[2]);
                	
	            }
	                
	            //调用getRotaionMatrix获得变换矩阵R[]    
	            ///SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);    
	            ///SensorManager.getOrientation(R, values);    
	            //经过SensorManager.getOrientation(R, values);得到的values值为弧度    
	            //转换为角度    
	            //values[0]=(float)Math.toDegrees(values[0]);
	            ///textview.setText("x="+values[0]);
	        }
		};
		
		mBR = new BatteryReceiver();
		
		sm=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mSensor=sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		registerReceiver(mBR, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		sm.registerListener(myListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_GAME);
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
        sm.unregisterListener(myListener);
        this.unregisterReceiver(mBR);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_yunjian_json, menu);
		return true;
	}
	
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
	
	private class AlarmBeep {
		private Activity act;
		private SoundPool sp;
		private int playId = 0;
		private Vibrator vibrator;
		//private static final String TAG = "AlarmBeep";
		
		public AlarmBeep(Activity activity) {
			act = activity;
			act.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			//AudioManager audioService = (AudioManager)act.getSystemService(Context.AUDIO_SERVICE);
			vibrator = (Vibrator)act.getSystemService(Context.VIBRATOR_SERVICE);
			sp = new SoundPool(10, AudioManager.STREAM_SYSTEM, 0);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
			playId = sp.load(act, R.raw.beep, 1);
			// When the beep has finished playing, rewind to queue up another one. 
		}
		
		public void playBeep() {
			sp.play(playId, 0.5f, 0.5f, 1, 0, 1f);
		}
		
		public void playVibrator() {
			vibrator.vibrate(10);
		}
	}
	
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

}
