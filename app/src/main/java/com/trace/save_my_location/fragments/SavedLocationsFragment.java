package com.trace.save_my_location.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trace.save_my_location.R;
import com.trace.save_my_location.adapters.SavedLocationsAdapter;
import com.trace.save_my_location.database.LocalDB;
import com.trace.save_my_location.views.EmptyCheckRecyclerView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by akhil on 15/2/16.
 * This fragment shows the already saved user locations
 */
public class SavedLocationsFragment extends Fragment {

    @Bind(R.id.rv_saved_locations)
    EmptyCheckRecyclerView rvSavedLocations;
    @Bind(R.id.txt_empty)
    TextView txtEmpty;

    public static SavedLocationsFragment newInstance() {
        return new SavedLocationsFragment();
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
        rvSavedLocations.setAdapter(
                new SavedLocationsAdapter(
                new LocalDB(getActivity()).retrieveAllSavedLocations(), this));
        rvSavedLocations.setEmptyView(txtEmpty);
    }
}
