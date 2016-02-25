package com.trace.save_my_location.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.trace.save_my_location.R;
import com.trace.save_my_location.database.LocalDB;
import com.trace.save_my_location.screens.MainActivity;
import com.trace.save_my_location.services.FetchAddressIntentService;
import com.trace.save_my_location.utils.Constants;
import com.trace.save_my_location.utils.OnBackPressedListener;
import com.trace.save_my_location.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by akhil on 15/2/16.
 * This class displays the map to show the location of the user
 */

public class MapDisplayFragment extends MainFragment
        implements OnMapReadyCallback,
        GoogleMap.OnCameraChangeListener,
        OnBackPressedListener {

    @Bind(R.id.rl_fabs)
    RelativeLayout rlFabs;
    @Bind(R.id.txt_address_display)
    TextView txtAddressDisplay;
    @Bind(R.id.sliding_panel)
    SlidingUpPanelLayout slidingPanel;
    @Bind(R.id.txt_address)
    TextView txtAddress;


    private GoogleMap map;
    private LocationManager locationManager;
    private Location location;
    private String addressOutput;
    private AddressResultReceiver resultReceiver = new AddressResultReceiver(new Handler());
    private boolean isPanelOpen = false;

    private static final String LOCATION_DATA_EXTRA = "location_data",
            PANEL_STATE = "panel_state",
            ADDRESS = "address";

    private final LocationListener fineLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            MapDisplayFragment.this.location = location;
            moveToCurrentLocation(true);
            try {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(courseLocationListener);
            } catch (SecurityException e) {
                Utils.log("No permission");
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private final LocationListener courseLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            MapDisplayFragment.this.location = location;
            moveToCurrentLocation(true);
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException e) {
                Utils.log("No permission");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public static MapDisplayFragment newInstance() {
        return new MapDisplayFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            location = savedInstanceState.getParcelable(LOCATION_DATA_EXTRA);
            isPanelOpen = savedInstanceState.getBoolean(PANEL_STATE);
            addressOutput = savedInstanceState.getString(ADDRESS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        slidingPanel.setPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {

            @Override
            public void onPanelCollapsed(View panel) {
                isPanelOpen = false;
            }

            @Override
            public void onPanelExpanded(View panel) {
                isPanelOpen = true;
            }
        });
        if (isPanelOpen) {
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
        if(addressOutput != null) {
            txtAddressDisplay.setText(addressOutput);
            txtAddress.setText(addressOutput);
        }
        setUpMap();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fab_locate)
    public void onLocateClick(View view) {
        findUserLocation();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fab_save)
    public void onSaveClick(View view) {
        bringSaveLayoutUp();
    }

    private void bringSaveLayoutUp() {
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    private void setUpMap() {
        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentByTag("map_fragment_tag");
        if (supportMapFragment == null) {
            supportMapFragment = new SupportMapFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.map_container, supportMapFragment, "map_fragment_tag")
                    .commit();
        }
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (location == null) {
            LatLng center = new LatLng(0, 0);
            map.moveCamera(CameraUpdateFactory.newLatLng(center));
        } else {
            moveToCurrentLocation(false);
        }

        map.setOnCameraChangeListener(this);
    }

    private void moveToCurrentLocation(boolean animate) {
        if (map != null && location != null) {
            LatLng currentLocation =
                    new LatLng(location.getLatitude(), location.getLongitude());
            map.clear();
            if (animate) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            }
        }
    }

    private void findUserLocation() {
        if (checkForLocationPermission(Constants.LOCATION_PERMISSION_REQ_CODE)) {
            if (locationManager != null) {
                locationManager.removeUpdates(fineLocationListener);
                locationManager.removeUpdates(courseLocationListener);
            } else {
                locationManager = (LocationManager) getActivity()
                        .getSystemService(Activity.LOCATION_SERVICE);
            }
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null &&
                    location.getTime() > Calendar.getInstance().getTimeInMillis() - 60 * 1000) {
                moveToCurrentLocation(true);
            } else {
                Location courseLocation =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (courseLocation != null && location != null) {
                    if (location.getTime() < courseLocation.getTime()) {
                        location = courseLocation;
                    }
                } else if (courseLocation != null) {
                    location = courseLocation;
                }

                moveToCurrentLocation(true);

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        Constants.LOCATION_REFRESH_TIME,
                        Constants.LOCATION_REFRESH_DISTANCE,
                        courseLocationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        Constants.LOCATION_REFRESH_TIME,
                        Constants.LOCATION_REFRESH_DISTANCE,
                        fineLocationListener);
            }
        }
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
    public void onPermissionRequestReturn(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == Constants.LOCATION_PERMISSION_REQ_CODE) {
                findUserLocation();
            } else if (requestCode == Constants.GEOFENCING_PERMISSION_REQ_CODE) {
                sendGeoFencingRequest();
            }
        } else {
            Snackbar.make(rlFabs, "The permission is required to find your location.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (map != null) {
            location = new Location("");
            location.setLatitude(cameraPosition.target.latitude);
            location.setLongitude(cameraPosition.target.longitude);
            startIntentService();
        }
    }

    private void startIntentService() {
        txtAddressDisplay.setText(R.string.fetching_address);
        txtAddress.setText(R.string.fetching_address);
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        getActivity().startService(intent);
    }

    private void stopLocating() {
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(fineLocationListener);
                locationManager.removeUpdates(courseLocationListener);
            }
        } catch (SecurityException e) {
            Utils.log("No permission");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocating();
        ((MainActivity)getActivity()).setOnBackPressedListener(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setOnBackPressedListener(this);
        if (location == null) {
            findUserLocation();
        }

    }

    @Override
    public boolean isBackPressHandled() {
        if (isPanelOpen) {
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.rl_save)
    void onSaveClicked(View view) {
        saveLocation(false);
    }

    private void saveLocation(final boolean shouldNotify) {

        if (location != null && !addressOutput.equalsIgnoreCase("No address found")) {
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            dialog = ProgressDialogFragment.newInstance("Saving location. Please wait...");
            dialog.show(getFragmentManager(), "progress");
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {

                    saveAndDeployFence(new LocalDB(getActivity())
                                    .insert(addressOutput,
                                            location.getLongitude(),
                                            location.getLatitude(),
                                            shouldNotify ? Constants.NOTIFY : Constants.DO_NOT_NOTIFY),
                            shouldNotify);
                    return null;
                }
            }.execute();
        } else {
            InfoFragment.newInstance(
                    "Address Unavailable", "Unable to fetch the address for the selected location.")
                    .show(getFragmentManager(), "info_fragment");
        }
    }

    public void saveAndDeployFence(final long columnId, final boolean shouldNotify) {

        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                OutputStream out = null;
                boolean success = false;
                File folder = new File(
                        getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        Constants.MAP_IMAGES_FOLDER);
                if (folder.exists() || folder.mkdir()) {
                    try {
                        out = new FileOutputStream(new File(folder, columnId+".jpg"));
                        snapshot.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        success = true;
                    } catch (FileNotFoundException e) {
                        Utils.log("Exception writing to file");
                    } finally {
                        if (out != null) {
                            try {
                                out.flush();
                                out.close();
                            } catch (IOException e) {
                                Utils.log("Unable to close file output");
                            }
                        }
                    }
                }
                if (!shouldNotify) {
                    dialog.dismiss();
                    if (success) {
                        Toast.makeText(getActivity(), "Location saved", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(getActivity(), "Unable to save location", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        };

        map.snapshot(callback);
        formGeoFenceRequest(columnId, location.getLatitude(), location.getLongitude());
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.rl_share)
    void onShareClicked(View view) {
        if (location != null) {
            Utils.shareLocation(getActivity(), location.getLatitude(), location.getLongitude());
        } else {
            InfoFragment.newInstance(
                    "Location Unavailable", "Please wait while we fetch your location.")
                    .show(getFragmentManager(), "info_fragment");
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.rl_notify)
    void onNotifyClicked(View view) {
        saveLocation(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LOCATION_DATA_EXTRA, location);
        outState.putBoolean(PANEL_STATE, isPanelOpen);
        outState.putString(ADDRESS, addressOutput);
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            txtAddressDisplay.setText(addressOutput);
            txtAddress.setText(addressOutput);
        }

    }
}
