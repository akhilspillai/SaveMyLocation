package com.trace.save_my_location.screens;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.os.PersistableBundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
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
import com.trace.save_my_location.fragments.ProgressDialogFragment;
import com.trace.save_my_location.services.FetchAddressIntentService;
import com.trace.save_my_location.utils.Constants;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,
        OnMapReadyCallback,
        GoogleMap.OnCameraChangeListener {

    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.rl_fabs)
    RelativeLayout rlFabs;
    @Bind(R.id.nav_view)
    NavigationView navigationView;
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

    private ProgressDialogFragment dialog;

    private static final int LOCATION_REFRESH_TIME = 0,
            LOCATION_REFRESH_DISTANCE = 0,
            LOCATION_PERMISSION_REQ_CODE = 1000;
    private static final String LOCATION_DATA_EXTRA = "location_data",
            MAP_TAG = "map_tag",
            PANEL_STATE = "panel_state",
            ADDRESS = "address";

    private final LocationListener fineLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            MainActivity.this.location = location;
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
            MainActivity.this.location = location;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

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

        setUpMap(savedInstanceState);
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

    private void setUpMap(Bundle savedInstaneState) {
        SupportMapFragment supportMapFragment;
        if (savedInstaneState == null) {
            supportMapFragment = new SupportMapFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frag_container, supportMapFragment, MAP_TAG)
                    .commit();
        } else {
            location = savedInstaneState.getParcelable(LOCATION_DATA_EXTRA);
            isPanelOpen = savedInstaneState.getBoolean(PANEL_STATE);
            addressOutput = savedInstaneState.getString(ADDRESS);
            txtAddressDisplay.setText(addressOutput);
            txtAddress.setText(addressOutput);
            supportMapFragment = (SupportMapFragment)getSupportFragmentManager()
                    .findFragmentByTag(MAP_TAG);
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
        if (checkForLocationPermission()) {
            if (locationManager != null) {
                locationManager.removeUpdates(fineLocationListener);
                locationManager.removeUpdates(courseLocationListener);
            } else {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME,
                        LOCATION_REFRESH_DISTANCE, courseLocationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                        LOCATION_REFRESH_DISTANCE, fineLocationListener);
            }
        }
    }

    public boolean checkForLocationPermission() {
        if (Utils.isMarshMellowOrAbove()) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQ_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findUserLocation();
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
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
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
    protected void onStop() {
        super.onStop();
        stopLocating();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (location == null) {
            findUserLocation();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isPanelOpen) {
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.rl_save)
    void onSaveClicked(View view) {
        dialog = ProgressDialogFragment.getInstance("Saving location. Please wait...");
        dialog.show(getSupportFragmentManager(), "progress");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                captureScreen(new LocalDB(MainActivity.this).insert(addressOutput,
                        location.getLongitude(),
                        location.getLongitude(),
                        Constants.DO_NOT_NOTIFY));
                return null;
            }
        }.execute();
    }

    public void captureScreen(final long columnId) {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                OutputStream fout = null;
                boolean success = false;
                File folder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (folder != null) {
                    try {
                        fout = new FileOutputStream(new File(folder,String.valueOf(columnId)));
                        snapshot.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                        success = true;
                    } catch (FileNotFoundException e) {
                        Utils.log("Exception writing to file");
                    } finally {
                        if (fout != null) {
                            try {
                                fout.flush();
                                fout.close();
                            } catch (IOException e) {
                                Utils.log("Unable to close file output");
                            }
                        }
                    }
                }
                dialog.dismiss();
                if (success) {
                    Toast.makeText(MainActivity.this, "Location saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Unable to save location", Toast.LENGTH_SHORT).show();
                }
            }
        };

        map.snapshot(callback);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.rl_share)
    void onShareClicked(View view) {

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.rl_notify)
    void onNotifyClicked(View view) {

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
