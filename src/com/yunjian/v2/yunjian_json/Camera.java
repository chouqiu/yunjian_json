package com.yunjian.v2.yunjian_json;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Camera extends Activity {
	private SurfaceView _cv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		
		FrameLayout fl = new FrameLayout(this);
		_cv = new SurfaceView(this);
		fl.addView(_cv);
		TextView tv = new TextView(this);
		tv.setText("«Î≈ƒ…„");
		fl.addView(tv);
		
		setContentView(fl);
	}
}
