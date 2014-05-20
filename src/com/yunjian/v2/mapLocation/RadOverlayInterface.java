package com.yunjian.v2.mapLocation;

import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public interface RadOverlayInterface {
	public abstract void onTapRadPoint(int idx, OverlayItem it);
	public abstract void onTapMapView(GeoPoint p);
}
