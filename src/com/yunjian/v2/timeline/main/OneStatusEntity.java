package com.yunjian.v2.timeline.main;

import java.util.List;

/**
 * �?��状�?实体�?
 * @author 三人行技术开发团�?
 */
public class OneStatusEntity {
	/* 状�?名称 */
	private String statusName;
	/* 预计完成时间 */
	private String completeTime;
	private String eventName;
	/* 二级状�?list */
	private List<TwoStatusEntity> twoList;
	
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	public String getStatusName() {
		return statusName;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	public String getCompleteTime() {
		return completeTime;
	}
	public void setCompleteTime(String completeTime) {
		this.completeTime = completeTime;
	}
	
	public List<TwoStatusEntity> getTwoList() {
		return twoList;
	}
	public void setTwoList(List<TwoStatusEntity> twoList) {
		this.twoList = twoList;
	}
	
	
	
}
