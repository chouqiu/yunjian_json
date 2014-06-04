package com.yunjian.v2.API;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

import com.yunjian.v2.yunjian_json.R;

public class AlarmBeep {
	private Activity act;
	private SoundPool sp;
	private int playId = 0;
	private Vibrator vibrator;
	//private static final String TAG = "AlarmBeep";
	private Handler mHandler = null;
	private Thread mCurrent = null;
	
	public static final int MSG_ALARM_LOW = 1;
	public static final int MSG_ALARM_HIGH = 2;
	
	public AlarmBeep(Activity activity) {
		act = activity;
		act.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		//AudioManager audioService = (AudioManager)act.getSystemService(Context.AUDIO_SERVICE);
		vibrator = (Vibrator)act.getSystemService(Context.VIBRATOR_SERVICE);
		sp = new SoundPool(10, AudioManager.STREAM_SYSTEM, 0);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
		playId = sp.load(act, R.raw.beep, 1);
		// When the beep has finished playing, rewind to queue up another one.
		
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MSG_ALARM_LOW:
					playBeep(0.3f);
					break;
				case MSG_ALARM_HIGH:
					playBeep(0.3f);
					break;
				}
			}
		};
	}
	
	public void do_play(int lvl) {
		if ( mCurrent == null || ! mCurrent.isAlive() ) {
			mCurrent = new Thread(new PlayClass(lvl));
			mCurrent.start();
		}
	}
	
	public void playBeep(float volumn) {
		sp.play(playId, volumn, volumn, 1, 0, 1f);
	}
	
	public void playVibrator() {
		vibrator.vibrate(10);
	}
	
	private class PlayClass implements Runnable {
		private int _lvl = MSG_ALARM_LOW;
		private final static int _play_cnt = 3;
		private final static int ALARM_SHORT = 200;
		private final static int ALARM_LONG = 600;
		
		public PlayClass(int lvl) {
			_lvl = lvl;
		}
		
		@Override
		public void run() {
			// 循环播放
			while ( true ) {
				Message msg = new Message();
				msg.what = _lvl;
				
				for ( int i=0; i<_play_cnt; ++i ) {
					mHandler.sendMessage(msg);
					try {
						if ( _lvl == MSG_ALARM_LOW ) {
							Thread.sleep(ALARM_LONG);
						} else {
							Thread.sleep(ALARM_SHORT);
						}
					} catch (InterruptedException e) {
						Log.d("playClass", "sleep interrupt");
					}
				}
				
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}

