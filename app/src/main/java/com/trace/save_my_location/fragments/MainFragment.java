package com.trace.save_my_location.fragments;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.trace.save_my_location.database.LocalDB;
import com.trace.save_my_location.services.GeoFenceTransitionsIntentService;
import com.trace.save_my_location.utils.Constants;
import com.trace.save_my_location.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class MainFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback {


    private List<Geofence> geoFenceList = new ArrayList<>();
    private GoogleApiClient googleApiClient;
    private PendingIntent geoFencePendingIntent;

    ProgressDialogFragment dialog;

    public MainFragment() {

    }

    public boolean checkForLocationPermission(int requestId) {
        if (Utils.isMarshMellowOrAbove()) {
            if (getActivity().checkSelfPermission(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        requestId);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onPermissionRequestReturn(requestCode, permissions, grantResults);
    }

    public abstract void onPermissionRequestReturn(int requestCode, @NonNull String[] permissions,
                                                   @NonNull int[] grantResults);
    public void formGeoFenceRequest(long columnId, double latitude, double longitude) {
        geoFenceList.add(new Geofence.Builder()
                .setRequestId(String.valueOf(columnId))

                .setCircularRegion(
                        latitude,
                        longitude,
                        Constants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(-1)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        sendGeoFencingRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        dialog.dismiss();
        Toast.makeText(getActivity(),
                "Unable to set notification. Connection failed. Please try again later",
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        dialog.dismiss();
        Toast.makeText(getActivity(),
                "Unable to set notification. Connection failed. Please try again later",
                Toast.LENGTH_SHORT)
                .show();
    }

    public void sendGeoFencingRequest() {
        if (checkForLocationPermission(Constants.GEOFENCING_PERMISSION_REQ_CODE)) {
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    getGeoFencingRequest(),
                    getGeoFencePendingIntent())
                    .setResultCallback(this);
        }
    }

    private PendingIntent getGeoFencePendingIntent() {
        if (geoFencePendingIntent != null) {
            return geoFencePendingIntent;
        }
        Intent intent = new Intent(getActivity(), GeoFenceTransitionsIntentService.class);
        return PendingIntent.getService(getActivity(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeoFencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geoFenceList);
        return builder.build();
    }

    @Override
    public void onResult(@NonNull Result result) {
        dialog.dismiss();
        if (!result.getStatus().isSuccess()) {
            Toast.makeText(getActivity(),
                    "Unable to set notification. Please try again later",
                    Toast.LENGTH_SHORT)
                    .show();

            long columnId = Long.parseLong(geoFenceList.get(0).getRequestId());
            new LocalDB(getActivity()).updateNotification(columnId, Constants.DO_NOT_NOTIFY);
        } else {
            Toast.makeText(getActivity(), "Location saved", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
