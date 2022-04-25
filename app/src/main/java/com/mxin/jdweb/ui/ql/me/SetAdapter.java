package com.mxin.jdweb.ui.ql.me;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mxin.jdweb.R;


import java.util.List;

/**
 * Created by Administrator on 2019/3/7 0007.
 */

public class SetAdapter extends BaseMultiItemQuickAdapter<SetBean, BaseViewHolder> {


	public SetAdapter(@Nullable List<SetBean> data) {
		super(data);
		addItemType(SetBean.ITEM_SMALL_ROW, R.layout.item_set_small);
		addItemType(SetBean.ITEM_BIG_ROW, R.layout.item_set_big);
	}

	@Override
	protected void convert(BaseViewHolder helper, SetBean item) {
		helper.setText(R.id.tv_name, item.getName());
		switch (item.getItemType()){
			case SetBean.ITEM_SMALL_ROW:
				if(item.getIconResourceId() != 0){
					helper.setImageResource(R.id.iv_icon, item.getIconResourceId());
				}
				break;
			case SetBean.ITEM_BIG_ROW:

				break;
		}
	}
}
