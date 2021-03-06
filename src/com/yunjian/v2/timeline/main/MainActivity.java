package com.yunjian.v2.timeline.main;

import java.util.ArrayList;
import java.util.List;

import com.yunjian.v2.yunjian_json.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
/**
 * 时间轴
 * @author 三人行技术开发团队
 *
 */
public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private List<OneStatusEntity> oneList;
	private ExpandableListView expandlistView;
	private StatusExpandAdapter statusAdapter;
	//private BizAccountBillAdapter billAdapter;
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		context = this;
		expandlistView = (ExpandableListView) findViewById(R.id.expandlist);
		
		
		
		putInitData();
		
		statusAdapter = new StatusExpandAdapter(context, oneList);
		//billAdapter = new BizAccountBillAdapter(this);
		
		expandlistView.setAdapter(statusAdapter);
		expandlistView.setGroupIndicator(null); // 去掉默认带的箭头

		// 遍历所有group,将所有项设置成默认展开
		int groupCount = expandlistView.getCount();
		for (int i = 0; i < groupCount; i++) {
			expandlistView.expandGroup(i);
		}
		expandlistView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
	}

	private void putInitData() {
		String[] strArray = new String[]{"变电站", "发射塔", "地震"};
		String[] str1 = new String[]{"附近有变电站，请远离", "附近有发射塔，请远离", "云南曲靖发生5.2级地震，对您暂无影响"};
		//String[] str2 = new String[]{"您附近有低量辐射，请注意活动", "您附近有堵车，请注意", "您附近有传染病医院，请注意", "您附近道路有积水，请小心驾驶", "您附近空气质量不佳，请远离"};
		String[] str2 = new String[]{"低辐射", "堵车", "传染医院", "道路积水", "空气质量"};
		//String[] str3 = new String[]{"买方到银行抵押手续", "买方取他向权利证", "银行给卖方划尾款", "全部办结"};
		
		String[] timeStr2 = new String[]{"2013-11-01 13:16:22", "2013-11-02 13:16:22", 
				"2013-11-08 13:16:22", "2013-12-02 13:16:22", "2014-02-02 13:16:22", 
				"2014-03-05 13:16:22", "2013-04-06 13:16:22", "2014-05-02 13:16:22"};
		//String[] timeStr3 = new String[]{"", "", "", ""};
		
		oneList = new ArrayList<OneStatusEntity>();
		List<TwoStatusEntity> twoList = new ArrayList<TwoStatusEntity>();
		for ( int i=0, j=0, k=0; i<timeStr2.length; i++ ) {
			switch ( i ) {
			case 1:
			case 4:
			case 7:
				OneStatusEntity one = new OneStatusEntity();
				one.setStatusName(strArray[j]);
				one.setCompleteTime(timeStr2[i]);
				one.setEventName(str1[j]);
				one.setTwoList(twoList);
				j++;
				oneList.add(one);
				twoList = new ArrayList<TwoStatusEntity>();
				break;
			default:
				TwoStatusEntity two = new TwoStatusEntity();
				two.setStatusName(str2[k]);
				two.setCompleteTime(timeStr2[i]);
				two.setIsfinished(true);
				k++;
				twoList.add(two);
				break;
			}
		}
		
		/**
		 * 
		oneList = new ArrayList<OneStatusEntity>();
		for(int i=0 ; i<strArray.length ; i++){
			OneStatusEntity one = new OneStatusEntity();
			one.setStatusName(strArray[i]);
			List<TwoStatusEntity> twoList = new ArrayList<TwoStatusEntity>();
			String[] order = str1;
			String[] time = timeStr1;
			switch (i) {
			case 0:
				order = str1;
				time = timeStr1;
				Log.i(TAG, "str1");
				break;
			case 1:
				order = str2;
				time = timeStr2;
				Log.i(TAG, "str2");
				break;
			case 2:
				order = str3;
				time = timeStr3;
				Log.i(TAG, "str3");
				break;
			}
			
			for(int j=0 ; j<order.length ; j++){
				TwoStatusEntity two = new TwoStatusEntity();
				two.setStatusName(order[j]);
				if(time[j].equals("")){
					two.setCompleteTime("暂无");
					two.setIsfinished(false);
				}else{
					two.setCompleteTime(time[j]+" 完成");
					two.setIsfinished(true);
				}
				
				twoList.add(two);
			}
			one.setTwoList(twoList);
			oneList.add(one);
		}
		 */
		//Log.i(TAG, "二级状态："+oneList.get(0).getTwoList().get(0).getStatusName());
		
	}


}