package com.kingdee.mobileexpenses.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kingdee.mobileexpenses.R;
import com.kingdee.mobileexpenses.entity.BizAccount;
import com.kingdee.mobileexpenses.ui.AddOrUpdateBillActivity;
import com.kingdee.mobileexpenses.ui.ApprovalOpinionDetailActivity;
import com.kingdee.mobileexpenses.ui.BillDetailActivity;
import com.kingdee.mobileexpenses.utils.KingdeeConstant;
import com.kingdee.mobileexpenses.utils.ToolUtils;
import com.umeng.analytics.MobclickAgent;

public class BizAccountBillAdapter extends ArrayListAdapter<BizAccount> {
	private final static int CONTENTTYPECOUNT = 2;
	private final static int TYPE_FIRST = 0;
	private final static int TYPE_SECOND = 1;

	public static abstract class ItemDeleteCallBack {
		public abstract void deleteItem(BizAccount biz);
	}

	private static ItemDeleteCallBack itemDeleteCallBack;

	public BizAccountBillAdapter(Activity context) {
		super(context);
	}

	public static ItemDeleteCallBack getItemDeleteCallBack() {
		return itemDeleteCallBack;
	}

	public static void setItemDeleteCallBack(
			ItemDeleteCallBack itemDeleteCallBack) {
		BizAccountBillAdapter.itemDeleteCallBack = itemDeleteCallBack;
	}

	@Override
	public int getViewTypeCount() {
		return CONTENTTYPECOUNT;
	}

	@Override
	public int getItemViewType(int position) {
		int billState = Integer.parseInt(mList.get(position).getBillState());
		if (billState >= 45) {// 审核通过
			return TYPE_SECOND;
		}
		// mList.get(position).getBillState().equals(KingdeeConstant.CHECKED)
		// if (mList.get(position).getBillState().equals(KingdeeConstant.DRAFT))
		// {// 草稿
		// return TYPE_FIRST;
		// } else if (mList.get(position).getBillState()
		// .equals(KingdeeConstant.SUBMIT)) {// 已提交
		// return TYPE_FIRST;
		// } else if (mList.get(position).getBillState()
		// .equals(KingdeeConstant.CHECKING)) {// 审批中
		// return TYPE_FIRST;
		// } else if (mList.get(position).getBillState()
		// .equals(KingdeeConstant.FAILED)) {// 审核失败
		// return TYPE_FIRST;
		// }
		return TYPE_FIRST;
	}

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

	public void setHolderFiledValue(BaseViewHolder holder_,
			final BizAccount account) {
		// 月数显示
		holder_.tv_month.setText(ToolUtils.dateFormat(account.getApplyDate(),
				"yyyy.MM"));
		holder_.tv_day.setText(ToolUtils.dateFormat(account.getApplyDate(),
				"dd"));
		holder_.tv_week.setText(account.getApplyWeekDay());
		holder_.tv_case.setText(account.getCause());
		holder_.expense_amount.setText(ToolUtils.formatAmount(account
				.getAmount()));
		holder_.state_text.setText(account.getBillStateName());
		holder_.expenseTypeName.setText(account.getOperName());
		holder_.number.setText("(" + account.getNumber() + ")");
		if (account.getBillState().equals(KingdeeConstant.DRAFT)) {// 草稿
			holder_.tv_day.setTextColor(mContext.getResources().getColor(
					R.color.moth_unsubmit_color));
			holder_.head_point.setBackgroundResource(R.drawable.prepared);
			holder_.state_text.setTextColor(mContext.getResources().getColor(
					R.color.moth_unsubmit_color));
		} else if (account.getBillState().equals(KingdeeConstant.SUBMIT)) {// 已提交
			holder_.tv_day.setTextColor(mContext.getResources().getColor(
					R.color.moth_checking_color));
			holder_.head_point.setBackgroundResource(R.drawable.checking);
			holder_.state_text.setTextColor(mContext.getResources().getColor(
					R.color.moth_checking_color));
		} else if (account.getBillState().equals(KingdeeConstant.CHECKING)) {// 审批中
			holder_.tv_day.setTextColor(mContext.getResources().getColor(
					R.color.moth_checking_color));
			holder_.head_point.setBackgroundResource(R.drawable.checking);
			holder_.state_text.setTextColor(mContext.getResources().getColor(
					R.color.moth_checking_color));
		} else if (account.getBillState().equals(KingdeeConstant.FAILED)) {// 审核失败
			holder_.tv_day.setTextColor(mContext.getResources().getColor(
					R.color.moth_check_failedd_color));
			holder_.head_point.setBackgroundResource(R.drawable.checked_failed);
			holder_.state_text.setTextColor(mContext.getResources().getColor(
					R.color.moth_check_failedd_color));
		} else {// 审核通过
			holder_.tv_day.setTextColor(mContext.getResources().getColor(
					R.color.moth_check_passed_color));
			holder_.head_point.setBackgroundResource(R.drawable.checked_passed);
			holder_.state_text.setTextColor(mContext.getResources().getColor(
					R.color.moth_check_passed_color));
		}
		// if (account.getBillState().equals(KingdeeConstant.CHECKED))
		// 添加点击事件
		holder_.main_background.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (account.getBillState().equals(KingdeeConstant.DRAFT)
						|| account.getBillState()
								.equals(KingdeeConstant.SUBMIT)) {// ||
					// account.getBillState()
					// .equals(KingdeeConstant.SUBMIT) 金蝶产品不可以修改 // //

					MobclickAgent.onEvent(mContext,
							KingdeeConstant.ACTION_MOBELEXPENSEEDIT);// umeng统计
					Intent intent = new Intent(mContext,
							AddOrUpdateBillActivity.class);
					intent.putExtra(KingdeeConstant.BILLDETAIL_ACTION,
							KingdeeConstant.BILLDETAIL_UPDATE);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra(KingdeeConstant.BILL_ID, account.getId());// 单据id
					mContext.startActivity(intent);
				} else {// 查看
					MobclickAgent.onEvent(mContext,
							KingdeeConstant.ACTION_MOBELEXPENSEVIEW);// umeng统计
					Intent intent = new Intent(mContext,
							BillDetailActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra(KingdeeConstant.BILL_ID, account.getId());// 单据id
					mContext.startActivity(intent);
				}
			}
		});

		holder_.detailcheckhistory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(mContext,
						ApprovalOpinionDetailActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(KingdeeConstant.REQUEST_ID, account.getId());// 单据id
				mContext.startActivity(intent);
			}
		});

		holder_.main_background
				.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View arg0) {
						if (account.getBillState()
								.equals(KingdeeConstant.DRAFT)) {
							if (itemDeleteCallBack != null) {
								itemDeleteCallBack.deleteItem(account);// 删除单据
							}
						}
						return false;
					}
				});
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FirstViewHolder firstholder = null;
		SecondViewHolder secondholder = null;
		int contentViewType = getItemViewType(position);
		String pre = (position - 1) < 0 ? null : ToolUtils.dateFormat(mList
				.get(position - 1).getApplyDate(), "MM");
		String current = ToolUtils.dateFormat(mList.get(position)
				.getApplyDate(), "MM");
		// String next = (position + 1) >= getCount() ? null : ToolUtils
		// .dateFormat(mList.get(position + 1).getApplyDate(), "MM");
		if (convertView == null) {
			switch (contentViewType) {
			case TYPE_FIRST:
				convertView = mContext.getLayoutInflater().inflate(
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
				break;

			case TYPE_SECOND:
				convertView = mContext.getLayoutInflater().inflate(
						R.layout.secondstate_bill_item, parent, false);
				secondholder = new SecondViewHolder();
				initCommon(secondholder, convertView);
				secondholder.check_amount = (TextView) convertView
						.findViewById(R.id.check_amount);
				convertView.setTag(secondholder);
				break;
			}
		} else {
			switch (contentViewType) {
			case TYPE_FIRST:
				firstholder = (FirstViewHolder) convertView.getTag();
				firstholder.time_line_top.setVisibility(View.VISIBLE);
				firstholder.time_line_bottom.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams lp_first = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				lp_first.setMargins(0, 0, 0, 0);
				firstholder.head_point.setLayoutParams(lp_first);
				break;
			case TYPE_SECOND:
				secondholder = (SecondViewHolder) convertView.getTag();
				secondholder.time_line_top.setVisibility(View.VISIBLE);
				secondholder.time_line_bottom.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams lp_second = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				lp_second.setMargins(0, 0, 0, 0);
				secondholder.head_point.setLayoutParams(lp_second);
				break;
			}
		}
		// 赋值 以及逻辑控制
		switch (contentViewType) {
		case TYPE_FIRST:
			int firstheight = mContext.getResources()
					.getDrawable(R.drawable.checking_normal).getMinimumHeight();
			setHolderFiledValue(firstholder, mList.get(position));
			// 审批环节
			if ((mList.get(position).getApproveNote() == null && mList.get(
					position).getApprover() == null)
					|| mList.get(position).getApproveNote().length() <= 0) {
				firstholder.img_state.setVisibility(View.GONE);
				firstholder.checkLayout.setVisibility(View.GONE);
			} else {
				firstholder.img_state.setVisibility(View.GONE);
				firstholder.checkLayout.setVisibility(View.VISIBLE);
				firstholder.tv_department.setText(mList.get(position)
						.getApproveNote());
				firstholder.tv_name.setText(mList.get(position).getApprover());
			}
			if (current.equals(pre)) {
				firstholder.tv_month.setVisibility(View.GONE);
				// 根据状态设置状态表示图片圆点
				firstholder.right_layout.setPadding(
						0,
						mContext.getResources().getDimensionPixelSize(
								R.dimen.first_right_layout_paddingtop_equal),
						0, 0);
				firstholder.time_line_bottom.setHeight(firstheight
						- mContext.getResources().getDimensionPixelSize(
								R.dimen.first_time_line_minuse_height));
				if (position == 0) {
					firstholder.time_line_top.setVisibility(View.INVISIBLE);
					if (position == getCount() - 1) {
						firstholder.time_line_bottom
								.setVisibility(View.INVISIBLE);
					}
				} else if (position == getCount() - 1) {
					firstholder.time_line_bottom.setVisibility(View.INVISIBLE);
				}
			} else {
				firstholder.tv_month.setVisibility(View.VISIBLE);
				firstholder.right_layout.setPadding(
						0,
						mContext.getResources().getDimensionPixelSize(
								R.dimen.first_right_layout_paddingtop_equal),
						0, 0);
				firstholder.time_line_bottom.setHeight(firstheight
						- mContext.getResources().getDimensionPixelSize(
								R.dimen.first_time_line_minuse_height));
				if (position == 0) {
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					lp.setMargins(
							0,
							mContext.getResources().getDimensionPixelSize(
									R.dimen.pointer_paddingtop_equal), 0, 0);
					firstholder.right_layout
							.setPadding(
									0,
									mContext.getResources()
											.getDimensionPixelSize(
													R.dimen.first_right_layout_paddingtop_equal)
											+ mContext
													.getResources()
													.getDimensionPixelSize(
															R.dimen.pointer_paddingtop_equal),
									0, 0);
					firstholder.head_point.setLayoutParams(lp);
					firstholder.time_line_top.setVisibility(View.INVISIBLE);
					if (position == getCount() - 1) {
						firstholder.time_line_bottom
								.setVisibility(View.INVISIBLE);
					}
				} else if (position == getCount() - 1) {
					firstholder.time_line_bottom.setVisibility(View.INVISIBLE);
				}
			}
			break;

		case TYPE_SECOND:
			int secondheight = mContext.getResources()
					.getDrawable(R.drawable.checked_normald).getMinimumHeight();
			setHolderFiledValue(secondholder, mList.get(position));
			secondholder.check_amount.setText(ToolUtils.formatAmount(mList.get(
					position).getFamountencashed()));
			if (current.equals(pre)) {
				secondholder.tv_month.setVisibility(View.GONE);
				secondholder.right_layout.setPadding(
						0,
						mContext.getResources().getDimensionPixelSize(
								R.dimen.first_right_layout_paddingtop_equal),
						0, 0);
				secondholder.time_line_bottom.setHeight(secondheight
						- mContext.getResources().getDimensionPixelSize(
								R.dimen.second_time_line_minuse_height));
				if (position == 0) {
					secondholder.time_line_top.setVisibility(View.INVISIBLE);
					if (position == getCount() - 1) {
						secondholder.time_line_bottom
								.setVisibility(View.INVISIBLE);
					}
				} else if (position == getCount() - 1) {
					secondholder.time_line_bottom.setVisibility(View.INVISIBLE);
				}
			} else {
				secondholder.tv_month.setVisibility(View.VISIBLE);
				secondholder.right_layout.setPadding(
						0,
						mContext.getResources().getDimensionPixelSize(
								R.dimen.first_right_layout_paddingtop_equal),
						0, 0);
				secondholder.time_line_bottom.setHeight(secondheight
						- mContext.getResources().getDimensionPixelSize(
								R.dimen.second_time_line_minuse_height));
				if (position == 0) {
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					lp.setMargins(
							0,
							mContext.getResources().getDimensionPixelSize(
									R.dimen.pointer_paddingtop_equal), 0, 0);
					secondholder.right_layout
							.setPadding(
									0,
									mContext.getResources()
											.getDimensionPixelSize(
													R.dimen.first_right_layout_paddingtop_equal)
											+ mContext
													.getResources()
													.getDimensionPixelSize(
															R.dimen.pointer_paddingtop_equal),
									0, 0);
					secondholder.head_point.setLayoutParams(lp);
					secondholder.time_line_top.setVisibility(View.INVISIBLE);
					if (position == getCount() - 1) {
						secondholder.time_line_bottom
								.setVisibility(View.INVISIBLE);
					}
				} else if (position == getCount() - 1) {
					secondholder.time_line_bottom.setVisibility(View.INVISIBLE);
				}
			}
			break;
		}
		return convertView;
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
}
