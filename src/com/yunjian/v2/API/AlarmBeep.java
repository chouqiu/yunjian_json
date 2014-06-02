package com.yunjian.v2.API;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import com.yunjian.v2.yunjian_json.R;

public class AlarmBeep {
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
	
	public void playBeep(float volumn) {
		sp.play(playId, volumn, volumn, 1, 0, 1f);
	}
	
	public void playVibrator() {
		vibrator.vibrate(10);
	}
}

