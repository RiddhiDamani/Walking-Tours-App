package com.riddhidamani.walkingtourapp;

import android.location.Location;
import android.location.LocationListener;

import androidx.annotation.NonNull;

import java.util.List;

public class AppLocationListener implements LocationListener  {
    private final MapsActivity mapsActivity;

    AppLocationListener(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mapsActivity.updateLocation(location);
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        // Nothing to do here
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // Nothing to do here
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // Nothing to do here
    }
}
