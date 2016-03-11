package com.trace.save_my_location.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.trace.save_my_location.services.GeoFenceTransitionsIntentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by akhil on 25/2/16.
 * This class requests for a Geo Fence to be put up and returns the result to the calling class
 */
public class GeoFenceRequester implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback {

    private Context context;
    private long requestId;
    private GeoFenceResultCallback callback;
    private List<Geofence> geoFenceList = new ArrayList<>();
    private GoogleApiClient googleApiClient;
    private PendingIntent geoFencePendingIntent;

    private boolean isActivation;

    public GeoFenceRequester(Context context, GeoFenceResultCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void formGeoFenceActivateRequest(long columnId, double latitude, double longitude) {
        isActivation = true;
        geoFenceList.clear();
        requestId = columnId;
        geoFenceList.add(new Geofence.Builder()
                .setRequestId(String.valueOf(requestId))

                        .setCircularRegion(
                                latitude,
                                longitude,
                                Constants.GEOFENCE_RADIUS_IN_METERS
                        )
                        .setExpirationDuration(-1)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    public void formGeoFenceRemoveRequest(long columnId) {
        isActivation = false;
        geoFenceList.clear();
        requestId = columnId;

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (isActivation) {
            if (callback.checkForLocationPermission(
                    Constants.GEOFENCING_PERMISSION_REQ_CODE)) {
                sendGeoFencingActivateRequest();
            }
        } else {
            sendGeoFencingRemoveRequest();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        callback.onGeoFenceNotifyRequestReturn(requestId, false);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        callback.onGeoFenceNotifyRequestReturn(requestId, false);
    }

    @Override
    public void onResult(@NonNull Result result) {
        if (!result.getStatus().isSuccess()) {
            if (isActivation) {
                callback.onGeoFenceNotifyRequestReturn(requestId, false);
            } else {
                callback.onGeoFenceRemoveRequestReturn(requestId, false);
            }
        } else {
            if (isActivation) {
                callback.onGeoFenceNotifyRequestReturn(requestId, true);
            } else {
                callback.onGeoFenceRemoveRequestReturn(requestId, true);
            }
        }
    }

    public void sendGeoFencingActivateRequest() throws SecurityException {
        LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                getGeoFencingRequest(),
                getGeoFencePendingIntent())
                .setResultCallback(this);
    }

    public void sendGeoFencingRemoveRequest() {
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                Collections.singletonList(String.valueOf(requestId)))
                .setResultCallback(this);
    }

    private PendingIntent getGeoFencePendingIntent() {
        if (geoFencePendingIntent != null) {
            return geoFencePendingIntent;
        }
        Intent intent = new Intent(context, GeoFenceTransitionsIntentService.class);
        geoFencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geoFencePendingIntent;
    }

    private GeofencingRequest getGeoFencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geoFenceList);
        return builder.build();
    }

    public interface GeoFenceResultCallback {
        void onGeoFenceNotifyRequestReturn(long requestId, boolean success);
        void onGeoFenceRemoveRequestReturn(long requestId, boolean success);
        boolean checkForLocationPermission(int permissionRequestId);
    }
}
