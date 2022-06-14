package com.mxin.jdweb.widget.loading;

import android.view.View;

import com.mxin.jdweb.widget.loading.util.LoadConstants;
import com.mxin.jdweb.widget.loading.view.GlobalLoadingStatusView;


/**
 * demo to show how to create a {@link Gloading.Adapter}
 * @see GlobalLoadingStatusView
 * @author billy.qi
 * @since 19/3/18 18:37
 */
public class GlobalAdapter implements Gloading.Adapter {

    @Override
    public View getView(Gloading.Holder holder, View convertView, int status) {
        GlobalLoadingStatusView loadingStatusView = null;
        //reuse the old view, if possible
        if (convertView != null && convertView instanceof GlobalLoadingStatusView) {
            loadingStatusView = (GlobalLoadingStatusView) convertView;
        }
        if (loadingStatusView == null) {
            loadingStatusView = new GlobalLoadingStatusView(holder.getContext(), holder.getRetryTask());
        }
        loadingStatusView.setStatus(status);
        Object data = holder.getData();
        //show or not show msg view
        boolean hideMsgView = LoadConstants.HIDE_LOADING_STATUS_MSG.equals(data);
        loadingStatusView.setMsgViewVisibility(!hideMsgView);
        return loadingStatusView;
    }

}