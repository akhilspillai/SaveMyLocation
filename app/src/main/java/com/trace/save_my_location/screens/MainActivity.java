package com.trace.save_my_location.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.trace.save_my_location.R;
import com.trace.save_my_location.fragments.MapDisplayFragment;
import com.trace.save_my_location.fragments.SavedLocationsFragment;
import com.trace.save_my_location.fragments.ShowLocationFragment;
import com.trace.save_my_location.models.LocationModel;
import com.trace.save_my_location.utils.Constants;
import com.trace.save_my_location.utils.OnBackPressedListener;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.nav_view)
    NavigationView navigationView;

    private OnBackPressedListener onBackPressedListener;

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

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frag_container, MapDisplayFragment.newInstance(), "map_fragment")
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ArrayList<LocationModel> locationModels =
                intent.getParcelableArrayListExtra(Constants.LOCATION_DATA_EXTRA);
        if (locationModels != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container,
                            ShowLocationFragment.newInstance(locationModels),
                            "map_fragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            getSupportFragmentManager()
                    .popBackStack(
                            Constants.BACK_STACK_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (id == R.id.nav_saved) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frag_container, SavedLocationsFragment.newInstance())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(Constants.BACK_STACK_NAME)
                    .commit();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setOnBackPressedListener(OnBackPressedListener listener) {
        this.onBackPressedListener = listener;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (onBackPressedListener == null || !onBackPressedListener.isBackPressHandled()){
            super.onBackPressed();
        }
    }

}
