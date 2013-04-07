package com.yunjian.v2.yunjian_json;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.media.FaceDetector;

public class Camera extends Activity {
	private CameraView _cv;
	private android.hardware.Camera _ca;
	private Bitmap _map = null;
	private FrameLayout _fl = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		
		_fl = new FrameLayout(this);
		_cv = new CameraView(this);
		_fl.addView(_cv);
		
		setContentView(_fl);
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
						//parameters.setFocusMode("auto");
						//����ͼƬ����ʱ�ķֱ��ʴ�С
						int minW=0, minH=0;
						for ( android.hardware.Camera.Size sz : parameters.getSupportedPictureSizes() ) {
							if ( minW == 0 || minW > sz.width ) {
								minW = sz.width;
							}
							if ( minH == 0 || minH > sz.height ) {
								minH = sz.height;
							}
						}
						parameters.setPictureSize(minW, minH);
						
						try {
							//������������øղ��趨�Ĳ���
							_ca.setDisplayOrientation(90);
							_ca.setParameters(parameters);
							_ca.startPreview();
							_ca.autoFocus( new android.hardware.Camera.AutoFocusCallback() {
								@Override
								public void onAutoFocus(boolean success, android.hardware.Camera camera) {
									// TODO Auto-generated method stub
									if ( success ) {
										Toast.makeText(Camera.this, "focus OK!", Toast.LENGTH_SHORT).show();
										_ca.takePicture(null, null, new PictureCallback() {
											@Override
											public void onPictureTaken(
													byte[] arg0,
													android.hardware.Camera arg1) {
												// TODO Auto-generated method stub
												_map = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
												if ( _map != null ) {
													FaceDetector.Face[] mFaces = new FaceDetector.Face[1];
													FaceDetector detector = new FaceDetector(_map.getWidth(),
															_map.getHeight(), mFaces.length);
													int num = detector.findFaces(_map, mFaces);
													if ( num > 0 ) {
														Toast.makeText(Camera.this, "get faces: "+num, 
																Toast.LENGTH_SHORT).show();
													
														TextView tv = new TextView(Camera.this);
														tv.setTextColor(Color.argb(155, 255, 255, 255));
														tv.setTextSize(20);
														tv.setText("����������\nս������5\n�ȼ���һ��");
														_fl.addView(tv);
													} else {
														TextView tv = new TextView(Camera.this);
														tv.setTextColor(Color.argb(155, 255, 255, 255));
														tv.setTextSize(20);
														tv.setText("������δ֪\nս������δ֪\n�ȼ������մ�ע�ⰲȫ��");
														_fl.addView(tv);
													}
													
												}
											}
										});
									}
								}
							});
							
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
