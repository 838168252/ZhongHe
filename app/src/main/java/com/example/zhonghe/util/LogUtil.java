package com.example.zhonghe.util;

import android.util.Log;

public class LogUtil {
    private static boolean isDebug = true ;

    public static void e(String msg) {
        if (isDebug) {
            Log.e("pang", msg);
        }
    }

    public static void e(String tag , String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }
}
