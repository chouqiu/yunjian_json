package com.yunjian.v2.mapLocation;

import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.graphics.Bitmap;
import android.view.View;

public class BMapUtil {
    	
	/**
	 * 从view 得到图片
	 * @param view
	 * @return
	 */
	public static Bitmap getBitmapFromView(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache(true);
        return bitmap;
	}
	
	public static GeoPoint genGeoPoint( double lat, double lon ) {
		return new GeoPoint((int)(lat * 1E6), (int)(lon * 1E6));
	}
}
