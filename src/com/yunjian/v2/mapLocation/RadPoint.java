package com.yunjian.v2.mapLocation;

public class RadPoint {
	public long lasttime = 0;
	public double lat = 0.0;
	public double lon = 0.0;
	public int maxval = 0;
	public int report_count = 0;
	
	public RadPoint(long tm, double la, double lo, int mv, int rc) {
		lasttime = tm;
		lat = la;
		lon = lo;
		maxval = mv;
		report_count = rc;
	}
}