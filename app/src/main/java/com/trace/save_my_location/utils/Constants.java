package com.trace.save_my_location.utils;

/**
 * Created by akhil on 5/2/16.
 * Hosts all the constant values.
 */

public class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final int GEOFENCE_RADIUS_IN_METERS = 200;
    public static final String PACKAGE_NAME =
            "com.trace.save_my_location";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
    public static final String DATABASE = PACKAGE_NAME +
            ".location.db";
    public static final String DO_NOT_NOTIFY = "do_not_notify";
    public static final String NOTIFY = "notify";
    public static final String MAP_IMAGES_FOLDER = "map_images";
    public static final String BACK_STACK_NAME = "back_stack";

    public static final int LOCATION_REFRESH_TIME = 0,
            LOCATION_REFRESH_DISTANCE = 0,
            LOCATION_PERMISSION_REQ_CODE = 1000,
            GEOFENCING_PERMISSION_REQ_CODE = 1001;
}
