package com.yunjian.v2.yunjian_json;

public interface RadiationAlarmListener {
	abstract public void onMove(double x, double y, double z);
	abstract public void onAlarm(double x, double y, double z);
}
