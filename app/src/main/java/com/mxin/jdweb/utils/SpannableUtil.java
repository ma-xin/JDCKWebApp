package com.mxin.jdweb.utils;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

public class SpannableUtil {

    public static CharSequence formatForeground(String str, int foregroundColor){
        SpannableString sp = new SpannableString(str);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(foregroundColor);
        sp.setSpan(foregroundColorSpan, 0, str.length(), SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
        return sp;
    }

    public static CharSequence formatForeground(String str, int start, int end, int foregroundColor){
        SpannableString sp = new SpannableString(str);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(foregroundColor);
        sp.setSpan(foregroundColorSpan, start, end, SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
        return sp;
    }

    public static CharSequence formatForegroundToRed(String str){
        return formatForeground(str, Color.RED);
    }

    public static CharSequence formatForegroundToGray(String str){
        return formatForeground(str, Color.GRAY);
    }

    public static CharSequence formatForegroundToGreen(String str){
        return formatForeground(str, Color.GREEN);
    }

    public static CharSequence formatForegroundToBlueGray(String str){
        return formatForeground(str, Color.parseColor("#4E6985"));
    }
}
