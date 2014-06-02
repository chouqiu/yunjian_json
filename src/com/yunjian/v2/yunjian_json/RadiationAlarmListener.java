package com.yunjian.v2.yunjian_json;

import com.yunjian.v2.API.AlarmBeep;

public interface RadiationAlarmListener {
	abstract public void onMove(double x, double y, double z, AlarmBeep alarm);
	abstract public void onAlarm(double x, double y, double z);
	abstract public void onRadiationChange( double x, double y, double z, double fangcha, int isAlarm, AlarmBeep alarm);
}
