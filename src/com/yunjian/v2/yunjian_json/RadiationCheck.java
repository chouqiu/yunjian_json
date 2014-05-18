package com.yunjian.v2.yunjian_json;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

public class RadiationCheck {
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
	private RadiationAlarmListener mListener;
	private Activity activity;
	
	public RadiationCheck( RadiationAlarmListener listener, Activity act ) {
		mListener = listener;
		alarmbeep = new AlarmBeep(act);
		activity = act;
		
		myListener = new SensorEventListener(){
	        @Override   
	        public void onAccuracyChanged(Sensor sensor, int accuracy) {    
	            // 暂不需要          
	        }    
	        @Override   
	        public void onSensorChanged(SensorEvent event) {
	        	doSensorChange(event);
	        }
		};
		
		sm=(SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
		mSensor=sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		sm.registerListener(myListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_GAME);
	}
	
	private void doSensorChange(SensorEvent event) {
		if ( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
    		double diff_X = event.values[0];
    		double diff_Y = event.values[1];
    		double diff_Z = event.values[2];
    		
        	/*手机晃动检测*/    		
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
                    	// 无移动，也无告警
	                } else if ( acc_flag > 0 ) {
	                	// 有移动
	                	mListener.onMove(diff_X, diff_Y, diff_Z);
	                } else if ( alarm_flag > 0 ){
	                	// 有告警
	                	alarmbeep.playBeep();
	                	alarmbeep.playVibrator();
	                	mListener.onAlarm(alarm_avg[0], alarm_avg[1], alarm_avg[2]);
	                }
                    
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
        }
            
        //调用getRotaionMatrix获得变换矩阵R[]    
        ///SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);    
        ///SensorManager.getOrientation(R, values);    
        //经过SensorManager.getOrientation(R, values);得到的values值为弧度    
        //转换为角度    
        //values[0]=(float)Math.toDegrees(values[0]);
        ///textview.setText("x="+values[0]);
	}
}

class AlarmBeep {
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
