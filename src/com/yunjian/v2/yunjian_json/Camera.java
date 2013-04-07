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
			
			// 操作surface的holder
			_holder = this.getHolder();
			// 创建SurfaceHolder.Callback对象
			_holder.addCallback(new SurfaceHolder.Callback() {

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					if ( _ca != null ) {
						// 停止预览
						_ca.stopPreview();
						// 释放相机资源并置空
						_ca.release();
					}
					_ca = null;
				}

				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					//当预览视图创建的时候开启相机
					_ca = android.hardware.Camera.open();
					try {
						//设置预览
						_ca.setPreviewDisplay(_holder);
					} catch (IOException e) {
						// 释放相机资源并置空
						_ca.release();
						_ca = null;
					}

				}

				//当surface视图数据发生变化时，处理预览信息
				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
					
					if ( _ca != null ) {
						//获得相机参数对象
						android.hardware.Camera.Parameters parameters = _ca.getParameters();
						//设置格式
						parameters.setPictureFormat(ImageFormat.JPEG);
						//设置预览大小，这里我的测试机是Milsstone所以设置的是854x480
						for ( android.hardware.Camera.Size sz : parameters.getSupportedPreviewSizes() ) {
							if ( sz.width == 1280 ) {
								parameters.setPreviewSize(sz.width, sz.height);
								break;
							}
						}
						
						//设置自动对焦
						//parameters.setFocusMode("auto");
						//设置图片保存时的分辨率大小
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
							//给相机对象设置刚才设定的参数
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
														tv.setText("姓名：王岳\n战斗力：5\n等级：一级");
														_fl.addView(tv);
													} else {
														TextView tv = new TextView(Camera.this);
														tv.setTextColor(Color.argb(155, 255, 255, 255));
														tv.setTextSize(20);
														tv.setText("姓名：未知\n战斗力：未知\n等级：风险大，注意安全！");
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
						//开始预览
					}
				}
			});
			// 设置Push缓冲类型，说明surface数据由其他来源提供，而不是用自己的Canvas来绘图，在这里是由摄像头来提供数据
			//_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}
}
