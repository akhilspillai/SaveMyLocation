package com.trace.save_my_location.adapters;

import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.trace.save_my_location.R;
import com.trace.save_my_location.fragments.SavedLocationsFragment;
import com.trace.save_my_location.fragments.ShowLocationFragment;
import com.trace.save_my_location.models.LocationModel;
import com.trace.save_my_location.utils.Constants;
import com.trace.save_my_location.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by akhil on 15/2/16.
 * This is an adapter for displaying saved locations
 */

public class SavedLocationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<LocationModel> savedLocationList;
    private SavedLocationsFragment fragment;

    public class LocationViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.iv_map)
        ImageView ivMap;
        @Bind(R.id.txt_address)
        TextView txtAddress;
        /*@Bind(R.id.btn_notify)
        Button btnNotify;*/

        public LocationViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @SuppressWarnings("unused")
        @OnClick(R.id.iv_map)
        void onMapClicked() {
            LocationModel locationModel = savedLocationList.get(getAdapterPosition());

            ArrayList<LocationModel> locationModelList = new ArrayList<>();
            locationModelList.add(locationModel);
            fragment.getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container,
                            ShowLocationFragment.newInstance(locationModelList))
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(Constants.BACK_STACK_NAME)
                                    .commit();
        }

        @SuppressWarnings("unused")
        @OnClick(R.id.btn_share)
        void onShareClicked() {
            LocationModel locationModel = savedLocationList.get(getAdapterPosition());
            Utils.shareLocation(fragment.getActivity(),
                    locationModel.getLatitude(), locationModel.getLongitude());
        }

        /*@SuppressWarnings("unused")
        @OnClick(R.id.btn_notify)
        void onNotifyClicked() {
            LocationModel locationModel = savedLocationList.get(getAdapterPosition());
            if (locationModel.getNotification().equals(Constants.DO_NOT_NOTIFY)) {
                fragment.sendGeoFenceActivateRequest(locationModel);
            } else {
                fragment.sendGeoFenceRemoveRequest(locationModel);
            }
        }*/

        @SuppressWarnings("unused")
        @OnClick(R.id.btn_delete)
        void onDeleteClicked() {
            fragment.deleteLocation(savedLocationList.get(getAdapterPosition()));
        }
    }

    public SavedLocationsAdapter(List<LocationModel> savedLocations,
                                 SavedLocationsFragment fragment) {
        this.savedLocationList = savedLocations;
        this.fragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.saved_locaion_view, parent, false);
        return new LocationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LocationViewHolder locationViewHolder = (LocationViewHolder)holder;
        LocationModel locationModel = savedLocationList.get(position);
        locationViewHolder.txtAddress.setText(locationModel.getAddress());
        File folder = new File(
                fragment.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                Constants.MAP_IMAGES_FOLDER);

        /*if (locationModel.getNotification().equals(Constants.NOTIFY)) {
            locationViewHolder.btnNotify.setText(R.string.do_not_notify);
        } else {
            locationViewHolder.btnNotify.setText(R.string.notify);
        }*/

        Picasso.with(fragment.getActivity())
                .load(new File(folder, locationModel.getId()+".jpg"))
                .placeholder(R.drawable.placeholder_map)
                .into(locationViewHolder.ivMap);

    }

    @Override
    public int getItemCount() {
        return savedLocationList.size();
    }
}
