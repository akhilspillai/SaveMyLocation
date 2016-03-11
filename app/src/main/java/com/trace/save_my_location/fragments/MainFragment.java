package com.trace.save_my_location.fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.trace.save_my_location.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


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
}
