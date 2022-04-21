package com.mxin.jdweb.ui.ql.me;

import androidx.annotation.DrawableRes;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by Administrator on 2019/3/7 0007.
 */

public class SetBean implements MultiItemEntity {

	public static final int ITEM_SMALL_ROW = 0;

	public static final int ITEM_SMALL_SWITCH = 2;

	public static final int ITEM_BIG_ROW = 1;

	@DrawableRes
	int iconResourceId;

	String name;

	int itemType;

	//开关
	boolean isOFF;

	public SetBean() {
	}

	public SetBean(String name, int iconResourceId) {
		this.iconResourceId = iconResourceId;
		this.name = name;
	}

	@Override
	public int getItemType() {
		return itemType;
	}

	public int getIconResourceId() {
		return iconResourceId;
	}

	public void setIconResourceId(int iconResourceId) {
		this.iconResourceId = iconResourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isOFF() {
		return isOFF;
	}

	public SetBean setOFF(boolean OFF) {
		isOFF = OFF;
		return this;
	}

	public SetBean setItemType(int itemType) {
		this.itemType = itemType;
		return this;
	}
}
