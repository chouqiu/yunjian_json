package com.yunjian.v2.mapLocation;

import android.graphics.drawable.Drawable;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class RadiationOverlay extends ItemizedOverlay<OverlayItem> {
	private MapView mV;
	private Drawable mDefaultMarker;
	private RadOverlayInterface mTapListener = null;

	public RadiationOverlay(Drawable arg0, MapView arg1) {
		super(arg0, arg1);
		mV = arg1;
		mDefaultMarker = arg0;
		// 使用缺省构造即可
	}

	@Override
	protected boolean onTap(int arg0) {
		// 显示辐射最大值
		if ( mTapListener != null ) {
			mTapListener.onTapRadPoint(arg0, this.getItem(arg0));
		}
		return true;
		//return super.onTap(arg0);
	}

	@Override
	public boolean onTap(GeoPoint arg0, MapView arg1) {
		// 如果要处理这个事件，则返回true；否则返回false
		//super.onTap(arg0, arg1);
		//return false;
		
		if ( mTapListener != null ) {
			mTapListener.onTapMapView(arg0);
		}
	  
		return true;
	}
	
	public void registerTapListener( RadOverlayInterface tap ) {
		mTapListener = tap;
	}
	
	public OverlayItem AddOverlayItem( GeoPoint p, String title, String snippet, Drawable mark ) {
		OverlayItem it = new OverlayItem(p, title, snippet);
		if ( mark == null ) {
			it.setMarker(mDefaultMarker);
		} else {
			it.setMarker(mark);
		}
		
		this.addItem(it);
		mV.refresh();
		
		return it;
	}
	
	public void DelOverlayItem(OverlayItem it) {
		this.removeItem(it);
		mV.refresh();
	}
}
