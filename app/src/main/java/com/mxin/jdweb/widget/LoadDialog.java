package com.mxin.jdweb.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mxin.jdweb.R;
import com.mxin.jdweb.utils.ScreenUtils;

/**
 * Created by Administrator on 2019/2/18 0018.
 */

public class LoadDialog extends Dialog {

	public LoadDialog(@NonNull Context context) {
		super(context);
	}

	public LoadDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
	}

	protected LoadDialog(@NonNull Context context, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public void setText(String text){
		TextView textView = findViewById(R.id.tvLoad);
		if(textView!=null && text!=null){
			textView.setText(text);
		}
	}

	public static class Builder {

		Context mContext;

		public Builder(Context mContext) {
			this.mContext = mContext;
		}

		public LoadDialog create(){
			LoadDialog dialog = new LoadDialog(mContext, R.style.Dialog);
			dialog.setContentView(R.layout.dialog_login_view);
			dialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
			Window window = dialog.getWindow();
			WindowManager.LayoutParams lp = window.getAttributes();
			int screenW = ScreenUtils.getScreenWidth();
			lp.width = (int) (0.6 * screenW);

			TextView titleTxtv = dialog.findViewById(R.id.tvLoad);
			titleTxtv.setText("努力加载中...");

			return dialog;
		}

	}


}
