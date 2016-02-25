package com.trace.save_my_location.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trace.save_my_location.R;
import com.trace.save_my_location.models.LocationModel;
import com.trace.save_my_location.screens.MainActivity;
import com.trace.save_my_location.utils.OnBackPressedListener;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by akhil on 15/2/16.
 * This class displays the map to show the location of the user
 */

public class ShowLocationFragment extends Fragment
        implements OnMapReadyCallback,
        OnBackPressedListener,
        GoogleMap.OnMarkerClickListener {


    private GoogleMap map;
    private ArrayList<LocationModel> locationModels;
    private LocationModel currentLocation;


    private static final String LOCATION_DATA_EXTRA = "location_data";


    public static ShowLocationFragment newInstance(ArrayList<LocationModel> locationModel) {
        ShowLocationFragment fragment = new ShowLocationFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(LOCATION_DATA_EXTRA, locationModel);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            locationModels = savedInstanceState.getParcelableArrayList(LOCATION_DATA_EXTRA);
        } else {
            locationModels = getArguments().getParcelableArrayList(LOCATION_DATA_EXTRA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_location, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpMap();
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
        moveToLocation();
    }

    private void moveToLocation() {
        if (map != null) {
            currentLocation = locationModels.get(0);
            LatLng currentLatLng =
                    new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.clear();
            map.setOnMarkerClickListener(this);
            for (LocationModel locationModel : locationModels) {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(locationModel.getLatitude(),
                                locationModel.getLongitude()))
                        .title(locationModel.getAddress()));
            }
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng latLng = marker.getPosition();
        for (LocationModel locationModel : locationModels) {
            if (locationModel.getLatitude() == latLng.latitude
                    && locationModel.getLongitude() == latLng.longitude){
                currentLocation = locationModel;
                break;
            }
            currentLocation = null;
        }
        return false;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fab_navigate)
    void onNavigateClicked() {
        if (currentLocation != null) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" +
                    currentLocation.getLatitude() + "," + currentLocation.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } else {
            Toast.makeText(getActivity(), "Select a marker first.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity)getActivity()).setOnBackPressedListener(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setOnBackPressedListener(this);
    }

    @Override
    public boolean isBackPressHandled() {
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(LOCATION_DATA_EXTRA, locationModels);
    }
}
