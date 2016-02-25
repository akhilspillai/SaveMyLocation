package com.trace.save_my_location.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

    public static void shareLocation(Context context, double latitude, double longitude) {
        String url = Uri.parse("http://maps.google.com")
                .buildUpon()
                .appendQueryParameter("q", latitude + "," + longitude)
                .build().toString();
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Sharing Location");
        i.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(i, "Share URL"));
    }
}
