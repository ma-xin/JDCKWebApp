package com.mxin.jdweb.widget.loading;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mxin.jdweb.R;
import com.mxin.jdweb.widget.loading.view.GlobalLoadingStatusView;
import com.wang.avi.AVLoadingIndicatorView;

import static com.mxin.jdweb.widget.loading.Gloading.STATUS_LOADING;

/**
 * demo:
 *      when status == STATUS_LOADING use another UI
 *      otherwise, use the same UI as the global status view
 * @author billy.qi
 * @since 19/3/19 23:20
 *
 *
 *
 * implementation 'com.wang.avi:library:2.1.3' 多种loading动画
 * https://github.com/81813780/AVLoadingIndicatorView
 */
public class IndicatorAdapter implements Gloading.Adapter {

    private Indicator indicator;
    private boolean changeFlag = false;
    private View cacheView;


    public IndicatorAdapter() {
    }

    public IndicatorAdapter(Indicator indicator) {
        if(this.indicator!=indicator){
            changeFlag = true;
            this.indicator = indicator;
        }
    }

    public void setIndicator(Indicator indicator){
        if(cacheView!=null && cacheView instanceof IndicatorLoadingStatusView){
            ((IndicatorLoadingStatusView)cacheView).setIndocator(indicator);
        }
    }

    @Override
    public View getView(Gloading.Holder holder, View convertView, int status) {
        if (status == STATUS_LOADING) {
            //only loading UI special
            IndicatorLoadingStatusView view;
            if (convertView == null || !(convertView instanceof IndicatorLoadingStatusView)) {
                view = new IndicatorLoadingStatusView(holder.getContext(),indicator);
                convertView = view;
            } else {
                view = (IndicatorLoadingStatusView) convertView;
            }
            cacheView = view;
            if(changeFlag){
                view.setIndocator(indicator);
            }
            view.start();
        } else {
            //other status use global UI
            GlobalLoadingStatusView view;
            if (convertView == null || !(convertView instanceof GlobalLoadingStatusView)) {
                view = new GlobalLoadingStatusView(holder.getContext(), holder.getRetryTask());
                convertView = view;
            } else {
                view = (GlobalLoadingStatusView) convertView;
            }
            cacheView = null;
            view.setStatus(status);
        }
        return convertView;
    }

    /**
     * special loading status view for only one activity usage
     * @author billy.qi
     * @since 19/3/19 23:12
     */
    class IndicatorLoadingStatusView extends RelativeLayout {

        private final AVLoadingIndicatorView indicatorView;

        public IndicatorLoadingStatusView(Context context, Indicator indicator) {
            super(context);
            setGravity(Gravity.CENTER);
//            setBackgroundColor(0xCCCCCCCC);
            indicatorView = new AVLoadingIndicatorView(context, null, R.style.AVLoadingIndicatorView_Large);
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addView(indicatorView, lp);
            indicatorView.setIndicatorColor(context.getResources().getColor(R.color.purple_500));
            setIndocator(indicator);
        }

        private void setIndocator(Indicator indicator){
            if(indicatorView!=null && indicator!=null){
                indicatorView.setIndicator(indicator.name());
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            indicatorView.hide();
        }

        public void start() {
            indicatorView.show();
        }
    }


    public enum Indicator{

        BallPulseIndicator,
        BallGridPulseIndicator,
        BallClipRotateIndicator,
        BallClipRotatePulseIndicator,
        SquareSpinIndicator,
        BallClipRotateMultipleIndicator,
        BallPulseRiseIndicator,
        BallRotateIndicator,
        CubeTransitionIndicator,
        BallZigZagIndicator,
        BallZigZagDeflectIndicator,
        BallTrianglePathIndicator,
        BallScaleIndicator,
        LineScaleIndicator,
        LineScalePartyIndicator,
        BallScaleMultipleIndicator,
        BallPulseSyncIndicator,
        BallBeatIndicator,
        LineScalePulseOutIndicator,
        LineScalePulseOutRapidIndicator,
        BallScaleRippleIndicator,
        BallScaleRippleMultipleIndicator,
        BallSpinFadeLoaderIndicator,
        LineSpinFadeLoaderIndicator,
        TriangleSkewSpinIndicator,
        PacmanIndicator,
        BallGridBeatIndicator,
        SemiCircleSpinIndicator

    }

//    BallPulseIndicator,
//    BallGridPulseIndicator,
//    BallClipRotateIndicator,
//    BallClipRotatePulseIndicator,
//    SquareSpinIndicator,
//    BallClipRotateMultipleIndicator,
//    BallPulseRiseIndicator,
//    BallRotateIndicator,
//    CubeTransitionIndicator,
//    BallZigZagIndicator,
//    BallZigZagDeflectIndicator,
//    BallTrianglePathIndicator,
//    BallScaleIndicator,
//    LineScaleIndicator,
//    LineScalePartyIndicator,
//    BallScaleMultipleIndicator,
//    BallPulseSyncIndicator,
//    BallBeatIndicator,
//    LineScalePulseOutIndicator,
//    LineScalePulseOutRapidIndicator,
//    BallScaleRippleIndicator,
//    BallScaleRippleMultipleIndicator,
//    BallSpinFadeLoaderIndicator,
//    LineSpinFadeLoaderIndicator,
//    TriangleSkewSpinIndicator,
//    PacmanIndicator,
//    BallGridBeatIndicator,
//    SemiCircleSpinIndicator,


}