package com.trace.save_my_location.utils;

import android.os.Build;
import android.util.Log;

/**
 * Created by akhil on 5/2/16.
 * Class that provides some utility methods
 */
public class Utils {

    private static final String LOCATION_LOG = "Save location";

    public static boolean isLollipopOrAbove () {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isMarshMellowOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isJellyBeanMr2OrAbove () {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static void log(String log) {
        Log.d(LOCATION_LOG, log);
    }
}
