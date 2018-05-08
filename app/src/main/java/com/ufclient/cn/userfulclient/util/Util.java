package com.ufclient.cn.userfulclient.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

/**
 * Created by channey on 2018/5/7.
 */

public class Util {
    public static boolean isNetworkAvailable(@NonNull Context context) {
        int type = -1;
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            type = mNetworkInfo.getType();
        }
        if(type >= 0){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 判断是否是debug模式
     * @param context
     * @return
     */
    public static boolean isDebug(@NonNull Context context){
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
