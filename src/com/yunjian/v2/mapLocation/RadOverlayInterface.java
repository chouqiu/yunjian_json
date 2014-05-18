package com.yunjian.v2.mapLocation;

import com.baidu.platform.comapi.basestruct.GeoPoint;

public interface RadOverlayInterface {
	public abstract void onTapRadPoint(int idx);
	public abstract void onTapMapView(GeoPoint p);
}
