package com.example.zhonghe.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.zhonghe.pojo.power;


/**
 * 使用SharedPreferences进行数据存取的工具类
 */
public class SPDataUtils {
    private static final String mFileName = "mydata";//文件名称

    /**
     * @param context 上下文
     * @return
     */
    public static boolean saveInfo(Context context, int s1, int s2, int s3, int s4) {
        boolean flag = false;
        SharedPreferences sp = context.getSharedPreferences(mFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("s1", s1);
        editor.putInt("s2", s2);
        editor.putInt("s3", s3);
        editor.putInt("s4", s4);
        editor.commit();
        flag = true;
        return flag;
    }

    /**
     *
     * @param context 调用上下文
     * @return
     */
    public static power getInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(mFileName, Context.MODE_PRIVATE);
        power p = new power();
        p.setS1(sp.getInt("s1", 10));
        p.setS2(sp.getInt("s2", 33));
        p.setS3(sp.getInt("s3", 33));
        p.setS4(sp.getInt("s4", 10));
        return p;
    }


}
