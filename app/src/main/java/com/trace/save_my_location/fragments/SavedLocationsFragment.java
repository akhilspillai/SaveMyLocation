package com.trace.save_my_location.fragments;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.trace.save_my_location.R;
import com.trace.save_my_location.adapters.SavedLocationsAdapter;
import com.trace.save_my_location.database.LocalDB;
import com.trace.save_my_location.models.LocationModel;
import com.trace.save_my_location.utils.Constants;
import com.trace.save_my_location.utils.GeoFenceRequester;
import com.trace.save_my_location.views.EmptyCheckRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * Created by akhil on 15/2/16.
 * This fragment shows the already saved user locations
 */
public class SavedLocationsFragment extends MainFragment
        implements GeoFenceRequester.GeoFenceResultCallback {

    @Bind(R.id.rv_saved_locations)
    EmptyCheckRecyclerView rvSavedLocations;
    @Bind(R.id.txt_empty)
    TextView txtEmpty;

    private ArrayList<LocationModel> locationModels;

    private SavedLocationsAdapter adapter;
    private GeoFenceRequester requester;

    private static final String LOCATION_MODELS_KEY = "location_models_key";

    public static SavedLocationsFragment newInstance() {
        return new SavedLocationsFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            locationModels = new ArrayList<>();
        } else {
            locationModels = savedInstanceState.getParcelableArrayList(LOCATION_MODELS_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_loc, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_PORTRAIT) {
                    return 2;
                } else {
                    return 1;
                }
            }
        });
        rvSavedLocations.setLayoutManager(layoutManager);
        adapter = new SavedLocationsAdapter(locationModels, this);
        SlideInRightAnimationAdapter slideInRightAnimationAdapter =
                new SlideInRightAnimationAdapter(adapter);
        slideInRightAnimationAdapter.setInterpolator(new AccelerateDecelerateInterpolator());
        slideInRightAnimationAdapter.setFirstOnly(false);
        rvSavedLocations.setAdapter(adapter);
        rvSavedLocations.setEmptyView(txtEmpty);
        rvSavedLocations.setItemAnimator(new LandingAnimator(new OvershootInterpolator(1f)));

        if (savedInstanceState == null) {
            fetchLocations();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void fetchLocations() {
        new AsyncTask<Void, Void, List<LocationModel>>() {
            @Override
            protected List<LocationModel> doInBackground(Void... params) {
                return new LocalDB(getActivity()).retrieveAllSavedLocations();
            }

            @Override
            protected void onPostExecute(List<LocationModel> locationModelList) {
                super.onPostExecute(locationModelList);
                locationModels.clear();
                locationModels.addAll(locationModelList);
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

    public void sendGeoFenceActivateRequest(LocationModel locationModel) {
        requester = new GeoFenceRequester(getActivity(), this);
        requester.formGeoFenceActivateRequest(locationModel.getId(),
                locationModel.getLatitude(), locationModel.getLongitude());
    }

    public void sendGeoFenceRemoveRequest(LocationModel locationModel) {
        requester = new GeoFenceRequester(getActivity(), this);
        requester.formGeoFenceRemoveRequest(locationModel.getId());
    }

    public void deleteLocation(final LocationModel locationModel) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int i = 0;
                for (LocationModel locationModelTemp : locationModels) {
                    if (locationModelTemp.getId() == locationModel.getId()) {
                        new LocalDB(getActivity()).deleteLocation(locationModel.getId());
                        locationModels.remove(i);
                        if (locationModel.getNotification().equals(Constants.NOTIFY)) {
                            sendGeoFenceRemoveRequest(locationModel);
                        }
                        return i;
                    }
                    i++;
                }
                return -1;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                adapter.notifyItemRemoved(integer);
            }
        }.execute();
    }

    @Override
      public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == Constants.GEOFENCING_PERMISSION_REQ_CODE) {
                requester.sendGeoFencingActivateRequest();
            }
        } else {
            Snackbar.make(rvSavedLocations,
                    "The permission is required to find your location.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onGeoFenceNotifyRequestReturn(final long id, boolean success) {

        if (success) {
            Toast.makeText(getActivity(), "Location notification enabled", Toast.LENGTH_SHORT)
                    .show();
        }
        updateIfNeededAndFetch(id, success, true);
    }

    @Override
    public void onGeoFenceRemoveRequestReturn(long id, boolean success) {
        updateIfNeededAndFetch(id, success, false);
    }

    private void updateIfNeededAndFetch(final long id,
                                        final boolean shouldUpdate, final boolean shouldNotify) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                LocalDB localDB = new LocalDB(getActivity());
                if (shouldUpdate) {
                    localDB.updateNotification(id, shouldNotify ?
                            Constants.NOTIFY : Constants.DO_NOT_NOTIFY);
                }
                int i = 0;
                for (LocationModel locationModel : locationModels) {
                    if (locationModel.getId() == id) {
                        locationModel.setNotification(shouldNotify ?
                                Constants.NOTIFY : Constants.DO_NOT_NOTIFY);
                        return i;
                    }
                    i++;
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer locationModelPosition) {
                super.onPostExecute(locationModelPosition);
                adapter.notifyItemChanged(locationModelPosition);
            }
        }.execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(LOCATION_MODELS_KEY, locationModels);
    }
}
