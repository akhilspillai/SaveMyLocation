package com.trace.save_my_location.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Environment;
import android.widget.RemoteViews;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.squareup.picasso.Picasso;
import com.trace.save_my_location.R;
import com.trace.save_my_location.database.LocalDB;
import com.trace.save_my_location.models.LocationModel;
import com.trace.save_my_location.screens.MainActivity;
import com.trace.save_my_location.utils.Constants;
import com.trace.save_my_location.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by akhil on 19/2/16.
 * This service receives geo fence enter and exit calls
 */
public class GeoFenceTransitionsIntentService extends IntentService {

    private static final int NOTIFICATION_ID = 1001;

    public GeoFenceTransitionsIntentService() {
        super("GeoFenceTransitionsIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geoFencingEvent = GeofencingEvent.fromIntent(intent);
        if (geoFencingEvent.hasError()) {
            Utils.log("Error while geo fencing:" + geoFencingEvent.getErrorCode());
            return;
        }

        int geoFenceTransition = geoFencingEvent.getGeofenceTransition();

        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggeringGeoFences = geoFencingEvent.getTriggeringGeofences();

            sendNotification(triggeringGeoFences);
        }
    }

    private void sendNotification(List<Geofence> triggeringGeoFences) {
        Notification notification;

        ArrayList<LocationModel> locationModels = new ArrayList<>();
        LocalDB localDB = new LocalDB(this);
        for (Geofence geofence : triggeringGeoFences) {
            try {
                locationModels.add(localDB.retrieveSavedLocation(Long.parseLong(geofence.getRequestId())));
            } catch (Exception e) {
                Utils.log("Error while retrieving saved location "+e.getMessage());
            }
        }

        if (locationModels.isEmpty()) {
            return;
        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putParcelableArrayListExtra(Constants.LOCATION_DATA_EXTRA, locationModels);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder.setSmallIcon(R.drawable.ic_location_notification);

        notification = notificationBuilder.build();
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.notification_nearby_location);

        remoteViews.setTextViewText(R.id.txt_location_subtitle, "You are near " +
                triggeringGeoFences.size()+" of your saved locations.");

        notification.contentView = remoteViews;
        notification.contentIntent = pendingIntent;

        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                Constants.MAP_IMAGES_FOLDER);

        Picasso.with(this)
                .load(new File(folder, triggeringGeoFences.get(0).getRequestId()+".jpg"))
                .placeholder(R.drawable.placeholder_map)
                .into(remoteViews, R.id.iv_map_image, NOTIFICATION_ID, notification);

        final NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
