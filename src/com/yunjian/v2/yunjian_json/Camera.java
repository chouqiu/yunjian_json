package com.yunjian.v2.yunjian_json;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera.Face;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Camera extends Activity {
	private CameraView _cv;
	private android.hardware.Camera _ca;
	//private Bitmap _map = null;
	private FrameLayout _fl = null;
	private TextView _tv = null;
	private SquareView _sv = null;
	private boolean _add_sv = false;
	private int _vw = 0, _vh = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		
		_sv = new SquareView(this);
		
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
						//parameters.setPreviewFormat(ImageFormat.RGB_565);
						parameters.setPictureFormat(ImageFormat.RGB_565);
						//����Ԥ����С�������ҵĲ��Ի���Milsstone�������õ���854x480
						for ( android.hardware.Camera.Size sz : parameters.getSupportedPreviewSizes() ) {
							if ( sz.width == 1280 ) {
								parameters.setPreviewSize(sz.width, sz.height);
								_vw = sz.width;
								_vh = sz.height;
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
						
						android.hardware.Camera.FaceDetectionListener fl = new android.hardware.Camera.FaceDetectionListener() {
							@Override
							public void onFaceDetection(Face[] faces,
									android.hardware.Camera camera) {
								// TODO Auto-generated method stub
								int num = faces.length;	
								if ( num > 0 ) {
									Toast.makeText(Camera.this, "get faces: "+num, 
											Toast.LENGTH_SHORT).show();
								
									camera.stopFaceDetection();
									
									if ( _tv == null ) {
										_tv = new TextView(Camera.this);
										_fl.addView(_tv);
										_add_sv = true;
									} else if ( _add_sv == true ) {
										_fl.removeView(_sv);
									}
									
									Rect r = faces[0].rect;
									Rect rr = new Rect(r);
									_vh = _cv.getHeight();
									_vw = _cv.getWidth();
									r.top = (int) ((r.top+1000f)/2000f*_vh);
									r.bottom = (int) ((r.bottom+1000f)/2000f*_vh);
									r.left = _vw-(int) ((r.left+1000f)/2000f*_vw);
									r.right = _vw-(int) ((r.right+1000f)/2000f*_vw);
									
									_tv.setTextColor(Color.argb(155, 255, 255, 255));
									_tv.setTextSize(20);
									_tv.setText("����������\nս������5\n�ȼ���һ��\n"+r.left+", "+r.top+", "
											+r.right+", "+r.bottom+"\n"+rr.left+", "+rr.top+", "
													+rr.right+", "+rr.bottom);
									
									_sv.setMatrix(r);
									_fl.addView(_sv);
									_add_sv = true;
								} else {
									if ( _tv == null ) {
										_tv = new TextView(Camera.this);
										_fl.addView(_tv);
									}
									
									if ( _add_sv == true ) {
										_fl.removeView(_sv);
										_add_sv = false;
									}
									_tv.setTextColor(Color.argb(155, 255, 255, 255));
									_tv.setTextSize(20);
									_tv.setText("������δ֪\nս������δ֪\n�ȼ������մ�ע�ⰲȫ��");
								}
									
							}
						};
						
						try {
							//������������øղ��趨�Ĳ���
							_ca.setDisplayOrientation(90);
							_ca.setParameters(parameters);
							_ca.setFaceDetectionListener(fl);
							_ca.startPreview();
							_ca.startFaceDetection();
							/*
							_ca.autoFocus( new android.hardware.Camera.AutoFocusCallback() {
								@Override
								public void onAutoFocus(boolean success, android.hardware.Camera camera) {
									// TODO Auto-generated method stub
									if ( success ) {
										//Toast.makeText(Camera.this, "focus OK!", Toast.LENGTH_SHORT).show();
										
										android.hardware.Camera.PictureCallback pc = new PictureCallback() {
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
										};
										_ca.takePicture(null, pc, pc);
									}
								}
							});
							*/
							
							
							
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
	
	public class SquareView extends View {
		private Paint p = null;
		Rect _r;
		
	    //private static final String TAG = "SquareView";

	    public SquareView(Context context) {
	        super(context);
	        p = new Paint();
	    }
	    
	    public void setMatrix(Rect r) {
	    	_r = r;
	    }

	    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	        //Log.d(TAG, "����");
	        // ��ɫ����
	        p.setColor(Color.YELLOW);
	        // ����
	        //float w = _w;
	        //float h = _h;
	        //if ( w <= 0.0 ) {
	        //	w = canvas.getWidth();
	        //	h = canvas.getHeight();
	        //}
	        // ���������ı�
	        //int margin = 90;
	        //canvas.drawRect(_r, p);
	        canvas.drawLine(_r.left, _r.top, _r.right, _r.top, p);
	        canvas.drawLine(_r.right, _r.top, _r.right, _r.bottom, p);
	        canvas.drawLine(_r.left, _r.bottom, _r.right, _r.bottom, p);
	        canvas.drawLine(_r.left, _r.top, _r.left, _r.bottom, p);
	        /*
	        canvas.drawLine(margin, margin, w - margin, margin, p);
	        canvas.drawLine(margin, h - margin, w - margin, h - margin, p);
	        canvas.drawLine(margin, margin, margin, h - margin, p);
	        canvas.drawLine(w - margin, margin, w - margin, h - margin, p);
	        */
	    }
	}
}
