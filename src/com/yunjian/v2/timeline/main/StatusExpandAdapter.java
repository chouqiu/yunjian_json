package com.yunjian.v2.timeline.main;

import java.util.List;

import com.yunjian.v2.yunjian_json.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class StatusExpandAdapter extends BaseExpandableListAdapter {
	//private static final String TAG = "StatusExpandAdapter";
	private LayoutInflater inflater = null;
	private List<OneStatusEntity> oneList;
	private Context context;
	
	private static final int USE_BIZ = 1;
	private static final int USE_ORG = 2;
	private int _type = USE_ORG;
	
	public StatusExpandAdapter(Context context, List<OneStatusEntity> oneList) {
		this.oneList = oneList;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return oneList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(oneList.get(groupPosition).getTwoList() == null){
			return 0;
		}else{
			return oneList.get(groupPosition).getTwoList().size();
		}
	}

	@Override
	public OneStatusEntity getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return oneList.get(groupPosition);
	}

	@Override
	public TwoStatusEntity getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		List<TwoStatusEntity> tl = oneList.get(groupPosition).getTwoList();
		if ( tl.size() > 0 ) {
			return tl.get(childPosition);
		} else {
			return null;
		}
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if ( _type == USE_ORG ) {
			GroupViewHolder holder = new GroupViewHolder();
			
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.one_status_item, null);
			}
			holder.groupName = (TextView) convertView.findViewById(R.id.one_status_name);
			holder.group_tiao = (TextView) convertView.findViewById(R.id.group_tiao);
			holder.eventName = (TextView) convertView.findViewById(R.id.one_complete_name);
			holder.statTime = (TextView) convertView.findViewById(R.id.one_status_time);
			//String html = "<b>"+oneList.get(groupPosition).getStatusName()+"</b><br/>"+
			//		oneList.get(groupPosition).getEventName()+"<br/>"+
			//		oneList.get(groupPosition).getCompleteTime();
			
			//holder.groupName.setText(Html.fromHtml(html));
			holder.groupName.setText(oneList.get(groupPosition).getStatusName());
			holder.eventName.setText(oneList.get(groupPosition).getCompleteTime());
			holder.statTime.setText(oneList.get(groupPosition).getEventName());
			
			//LayoutParams lp = convertView.getLayoutParams();
			//lp.height += 20;
			//convertView.setLayoutParams(lp);
			
			//if(oneList.get(groupPosition).getTwoList().get(0).isIsfinished()){
				holder.group_tiao.setBackgroundColor(context.getResources().getColor(R.color.yellow));
			//}else{
			//	holder.group_tiao.setBackgroundColor(context.getResources().getColor(R.color.grey));
			//}
			
		} else {
			/*
			FirstViewHolder firstholder = null;
	
			//String pre = (position - 1) < 0 ? null : "2014-06-02";
			//String current = "2014-06-03";
			// String next = (position + 1) >= getCount() ? null : ToolUtils
			// .dateFormat(mList.get(position + 1).getApplyDate(), "MM");
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.firststate_bill_item, parent, false);
				firstholder = new FirstViewHolder();
				initCommon(firstholder, convertView);
				firstholder.tv_department = (TextView) convertView
						.findViewById(R.id.tv_department);
				firstholder.tv_name = (TextView) convertView
						.findViewById(R.id.tv_name);
				firstholder.img_state = (ImageView) convertView
						.findViewById(R.id.img_state);
				firstholder.checkLayout = (LinearLayout) convertView
						.findViewById(R.id.checkLayout);
				convertView.setTag(firstholder);
	
			} else {
				firstholder = (FirstViewHolder) convertView.getTag();
				firstholder.time_line_top.setVisibility(View.VISIBLE);
				firstholder.time_line_bottom.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams lp_first = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				lp_first.setMargins(0, 0, 0, 0);
				firstholder.head_point.setLayoutParams(lp_first);
			}
			*/
		}
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		// TODO entity为空怎么办??
		if ( _type == USE_ORG ) {
			ChildViewHolder viewHolder = null;
			TwoStatusEntity entity = getChild(groupPosition, childPosition);
			if (convertView != null) {
				viewHolder = (ChildViewHolder) convertView.getTag();
			} else {
				viewHolder = new ChildViewHolder();
				convertView = inflater.inflate(R.layout.two_status_item, null);
				viewHolder.childName = (TextView) convertView.findViewById(R.id.two_status_name);
				viewHolder.twoStatusTime = (TextView) convertView.findViewById(R.id.two_status_time);
				viewHolder.twoStatusDate = (TextView) convertView.findViewById(R.id.two_status_date);
				//viewHolder.twoStatusTime = (TextView) convertView.findViewById(R.id.two_complete_time);
				viewHolder.tiao = (TextView) convertView.findViewById(R.id.tiao);
			}
			
			//String html = "<b>"+entity.getStatusName()+"</b><br/>"+entity.getCompleteTime();
			//viewHolder.childName.setText(Html.fromHtml(html));
			viewHolder.twoStatusTime.setText(entity.getEventName());
			viewHolder.twoStatusDate.setText(entity.getCompleteTime());
			viewHolder.childName.setText(entity.getStatusName());
			
			//viewHolder.childName.setText(entity.getStatusName());
			//viewHolder.twoStatusTime.setText(entity.getCompleteTime());
			
			if(entity.isIsfinished()){
				viewHolder.tiao.setBackgroundColor(context.getResources().getColor(R.color.yellow));
			}else{
				viewHolder.tiao.setBackgroundColor(context.getResources().getColor(R.color.grey));
			}
			
			convertView.setTag(viewHolder);
			
		} else {
			/*
			SecondViewHolder secondholder = null;
			
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.secondstate_bill_item, parent, false);
				secondholder = new SecondViewHolder();
				initCommon(secondholder, convertView);
				secondholder.check_amount = (TextView) convertView
						.findViewById(R.id.check_amount);
				convertView.setTag(secondholder);
			} else {
				secondholder = (SecondViewHolder) convertView.getTag();
				secondholder.time_line_top.setVisibility(View.VISIBLE);
				secondholder.time_line_bottom.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams lp_second = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				lp_second.setMargins(0, 0, 0, 0);
				secondholder.head_point.setLayoutParams(lp_second);
			}
			convertView.setTag(secondholder);
			*/
		}
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private class GroupViewHolder {
		public TextView groupName;
		public TextView eventName;
		public TextView statTime;
		public TextView group_tiao;
	}
	
	private class ChildViewHolder {
		public TextView childName;
		public TextView twoStatusTime;
		public TextView twoStatusDate;
		public TextView tiao;
	}

	/*
	public void initCommon(BaseViewHolder holder_, View convertView_) {
		holder_.time_line_top = (TextView) convertView_
				.findViewById(R.id.time_line_top);
		holder_.time_line_bottom = (TextView) convertView_
				.findViewById(R.id.time_line_bottom);
		holder_.head_point = (ImageView) convertView_
				.findViewById(R.id.head_point);
		holder_.right_layout = (LinearLayout) convertView_
				.findViewById(R.id.right_layout);

		holder_.tv_month = (TextView) convertView_.findViewById(R.id.tv_month);
		holder_.tv_day = (TextView) convertView_.findViewById(R.id.tv_day);
		holder_.tv_week = (TextView) convertView_.findViewById(R.id.tv_week);
		holder_.tv_case = (TextView) convertView_.findViewById(R.id.tv_case);
		holder_.right_layout = (LinearLayout) convertView_
				.findViewById(R.id.right_layout);
		holder_.main_background = (LinearLayout) convertView_
				.findViewById(R.id.main_background);
		holder_.detailcheckhistory = (LinearLayout) convertView_
				.findViewById(R.id.detailcheckhistory);
		holder_.state_text = (TextView) convertView_
				.findViewById(R.id.state_text);
		holder_.expense_amount = (TextView) convertView_
				.findViewById(R.id.expense_amount);
		holder_.expenseTypeName = (TextView) convertView_
				.findViewById(R.id.expenseTypeName);
		holder_.number = (TextView) convertView_.findViewById(R.id.number);
	}
	
	class BaseViewHolder {
		TextView tv_month, tv_day, tv_week, tv_case, time_line_bottom,
				time_line_top, expense_amount, state_text, expenseTypeName,
				number;
		ImageView head_point;
		LinearLayout right_layout, main_background, detailcheckhistory;
	}

	class FirstViewHolder extends BaseViewHolder {
		TextView tv_department, tv_name;
		ImageView img_state;
		LinearLayout checkLayout;
	}

	class SecondViewHolder extends BaseViewHolder {
		TextView check_amount;
	}
	*/
}
