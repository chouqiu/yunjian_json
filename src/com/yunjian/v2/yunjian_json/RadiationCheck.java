package com.yunjian.v2.yunjian_json;

import com.yunjian.v2.API.AlarmBeep;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RadiationCheck {
	private static int max_length = 10;
	private static double alarm_limit = 1.2;
	/*摇晃检测的时间间隔100ms*/ 
	private static final int UPDATE_INTERVAL = 100;
	/*摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感。*/  
    private static final double SHAKE_SHRESHOLD = 70; 
    private static int CONTINUOUS_ALARM_CNT = 3; // 连续n次达到告警标准时才告警
    
    public static int MAX_FANGCHA = 9000;
    private static int MAG_LIMIT = 1;
    private static boolean MOVEABLE = true;
	
	private float[][] magneticFieldValues=new float[3][max_length];
	private double[] normal_avg = new double[3];
	private double[] current_avg = new double[3];
	//private double[] current_rate = new double[3];
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
	
	private boolean bIsActive = false;
	
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
	}
	
	public void unRegisterDevice() {
		sm.unregisterListener(myListener);
		bIsActive = false;
	}
	
	public void registerDevice() {
		sm.registerListener(myListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_GAME);
		
		bIsActive = true;
	}
	
	public boolean isActive() {
		return bIsActive;
	}
	
	public void setMaxFangcha( int max ) {
		MAX_FANGCHA = max;
	}
	
	public void setMagLimit( int id ) {
		MAG_LIMIT = id;
	}
	
	public void setMoveable( boolean mv ) {
		MOVEABLE = mv;
	}
	
	public void setAlarmLimit( int limit ) {
		alarm_limit = limit / 10.0f;
	}
	
	public void setComputeLength( int len ) {
		max_length = len;
	}
	
	public static int getComputeLength() {
		return max_length;
	}

	public static double getAlarmLimit() {
		return alarm_limit;
	}

	public static int getMaxFangcha() {
		return MAX_FANGCHA;
	}

	public static int getMagLimit() {
		return MAG_LIMIT;
	}

	public static boolean isMOVEABLE() {
		return MOVEABLE;
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
                    	if ( MOVEABLE )
                    		acc_flag = 1;
                    	else
                    		acc_flag = 0;
                    }else{  
                        acc_flag = 0;  
                    }
                    
                    if ( alarm_flag == 0 && acc_flag == 0 ) {
                    	// 无移动，也无告警
	                } else if ( acc_flag > 0 ) {
	                	// 有移动
	                	mListener.onMove(diff_X, diff_Y, diff_Z, alarmbeep);
	                } else if ( alarm_flag > 0 ){
	                	// 有告警
	                	//alarmbeep.playBeep();
	                	//alarmbeep.playVibrator();
	                	//mListener.onAlarm(alarm_avg[0], alarm_avg[1], alarm_avg[2]);
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
            	
            	//current_rate[0] = current_avg[0]/normal_avg[0];
            	//current_rate[1] = current_avg[0]/normal_avg[1];
            	//current_rate[2] = current_avg[0]/normal_avg[2];
            	
            	int limit_cnt = 0;
            	limit_cnt += (current_avg[0]>alarm_limit ? 1 : 0);
            	limit_cnt += (current_avg[1]>alarm_limit ? 1 : 0);
            	limit_cnt += (current_avg[2]>alarm_limit ? 1 : 0);
            	
            	double fangcha = fangcha(current_avg, normal_avg, 1);
            	
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
            	//} else if ( fangcha > SHAKE_SHRESHOLD )
            	} else if ( limit_cnt >= MAG_LIMIT && fangcha > MAX_FANGCHA ) {
            		alarm_avg[0] = current_avg[0];
            		alarm_avg[1] = current_avg[1];
            		alarm_avg[2] = current_avg[2];
            		
            		alarm_flag = 1;
            		if ( fangcha > MAX_FANGCHA ) {
            			alarm_flag |= 2;
            		}
            		
            	} else {
            		alarm_flag = 0;
            		//if ( normal_avg[0]==0 || normal_avg[1]==0 || normal_avg[2]==0 ||
            		//		(current_rate[0]<=1 && current_rate[1]<=1 && current_rate[2]<=1) ) 
            		//{
            			// 清除误报噪声
            			normal_avg[0] = current_avg[0];
                		normal_avg[1] = current_avg[1];
                		normal_avg[2] = current_avg[2];
            		//}
            	}
            	
            	alarm_stat = (alarm_stat+1) % CONTINUOUS_ALARM_CNT;
            	if ( alarm_stat == 0 ) {
            		mListener.onRadiationChange(current_avg[0], current_avg[1], current_avg[2], fangcha, alarm_flag, alarmbeep);
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
	
	private double fangcha(double[] X, double[] Y, int interval) {
		double diff_X = X[0] - Y[0];
		double diff_Y = X[1] - Y[1];
		double diff_Z = X[2] - Y[2];
		
		return Math.sqrt(diff_X*diff_X+diff_Y*diff_Y+diff_Z*diff_Z)/interval*10000.0f;
	}
}
