package com.yunjian.v2.yunjian_json;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Camera extends Activity {
	private CameraView _cv;
	private android.hardware.Camera _ca;
	//private Bitmap _map;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		
		FrameLayout fl = new FrameLayout(this);
		_cv = new CameraView(this);
		fl.addView(_cv);
		TextView tv = new TextView(this);
		tv.setTextColor(Color.argb(155, 255, 255, 255));
		tv.setTextSize(20);
		tv.setText("����������\nս������5\n�ȼ���һ��");
		fl.addView(tv);
		
		setContentView(fl);
	}
	
	class CameraView extends SurfaceView {
		private SurfaceHolder _holder = null;
		
		public CameraView(Context context) {
			super(context);
			
			// ����surface��holder
			_holder = this.getHolder();
			// ����SurfaceHolder.Callback����
			_holder.addCallback(new SurfaceHolder.Callback() {

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					if ( _ca != null ) {
						// ֹͣԤ��
						_ca.stopPreview();
						// �ͷ������Դ���ÿ�
						_ca.release();
					}
					_ca = null;
				}

				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					//��Ԥ����ͼ������ʱ�������
					_ca = android.hardware.Camera.open();
					try {
						//����Ԥ��
						_ca.setPreviewDisplay(_holder);
					} catch (IOException e) {
						// �ͷ������Դ���ÿ�
						_ca.release();
						_ca = null;
					}

				}

				//��surface��ͼ���ݷ����仯ʱ������Ԥ����Ϣ
				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
					
					if ( _ca != null ) {
						//��������������
						android.hardware.Camera.Parameters parameters = _ca.getParameters();
						//���ø�ʽ
						parameters.setPictureFormat(ImageFormat.JPEG);
						//����Ԥ����С�������ҵĲ��Ի���Milsstone�������õ���854x480
						for ( android.hardware.Camera.Size sz : parameters.getSupportedPreviewSizes() ) {
							if ( sz.width == 1280 ) {
								parameters.setPreviewSize(sz.width, sz.height);
								break;
							}
						}
						
						//�����Զ��Խ�
						parameters.setFocusMode("auto");
						//����ͼƬ����ʱ�ķֱ��ʴ�С
						for ( android.hardware.Camera.Size sz : parameters.getSupportedPictureSizes() ) {
							parameters.setPictureSize(sz.width, sz.height);
							break;
						}
						
						try {
							//������������øղ��趨�Ĳ���
							_ca.setParameters(parameters);
							_ca.startPreview();
							Toast.makeText(Camera.this, "size: "+parameters.getPreviewSize().width+"x"+
									parameters.getPreviewSize().height+"||"+parameters.getPictureSize().width+"x"+
									parameters.getPictureSize().height, Toast.LENGTH_LONG).show();
						} catch ( Exception e ) {
							Toast.makeText(Camera.this, "exp: "+e.toString(), Toast.LENGTH_LONG).show();
						}
						//��ʼԤ��
					}
				}
			});
			// ����Push�������ͣ�˵��surface������������Դ�ṩ�����������Լ���Canvas����ͼ����������������ͷ���ṩ����
			//_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}
}
