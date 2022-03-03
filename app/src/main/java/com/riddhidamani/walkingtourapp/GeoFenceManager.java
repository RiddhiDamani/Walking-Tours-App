package com.riddhidamani.walkingtourapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GeoFenceManager {

    private static final String TAG = "GeoFenceManager";
    private final MapsActivity mapsActivity;
    private final GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private final ArrayList<Circle> circles = new ArrayList<>();
    private final List<PatternItem> pattern = Collections.singletonList(new Dot());
    private static final ArrayList<GeoFenceData> fenceList = new ArrayList<>();

    private ArrayList<LatLng> tourPath;

    // hash map
    private static HashMap<String, GeoFenceData> buildingHashmap = new HashMap<String, GeoFenceData>();

    public GeoFenceManager(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
        geofencingClient = LocationServices.getGeofencingClient(mapsActivity);

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(mapsActivity, aVoid -> Log.d(TAG, "onSuccess: removeGeofences"))
                .addOnFailureListener(mapsActivity, e -> {
                    Log.d(TAG, "onFailure: removeGeofences");
                    Toast.makeText(mapsActivity, "Trouble removing existing fences: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        // Invoke GeoFenceData Volley
        GeoFenceDataVolley.getFenceData(mapsActivity, this);
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(mapsActivity, GeofenceReceiver.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(mapsActivity, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    static GeoFenceData getFenceData(String id) {
        if(buildingHashmap.containsKey(id)) {
            return buildingHashmap.get(id);
        }
        return null;
    }

    void addFences(ArrayList<GeoFenceData> fences) {
        fenceList.clear();
        fenceList.addAll(fences);

        for (GeoFenceData fd : fenceList) {

            buildingHashmap.put(fd.getId(), fd);

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(fd.getId())
                    .setCircularRegion(
                            fd.getLatitude(),
                            fd.getLongitude(),
                            fd.getRadius())
                    .setNotificationResponsiveness(1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build();

            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .addGeofence(geofence)
                    .build();

            geofencePendingIntent = getGeofencePendingIntent();

            if (ActivityCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Log.d(TAG, "addFences: total " + fences.size() + " fences");

            geofencingClient
                    .addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: addGeofences" + fd.getId()))
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Log.d(TAG, "onFailure: addGeofences"+ fd.getId());
                        Toast.makeText(mapsActivity, "Trouble adding new fence: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        }
        mapsActivity.runOnUiThread(this::drawFences);
    }

    void drawFences() {
        for (GeoFenceData fd : fenceList) {
            drawFence(fd);
        }
    }
    private void drawFence(GeoFenceData fd) {

        int line = Color.parseColor(fd.getFenceColor());
        int fill = ColorUtils.setAlphaComponent(line, 80);

        LatLng latLng = new LatLng(fd.getLatitude(), fd.getLongitude());
        GoogleMap map = mapsActivity.getMap();

        Circle c = mapsActivity.getMap().addCircle(new CircleOptions()
                .center(latLng)
                .radius(fd.getRadius())
                .strokePattern(pattern)
                .strokeColor(line)
                .fillColor(fill));

        circles.add(c);
    }

    void eraseFences() {
        for (Circle c : circles)
            c.remove();
        circles.clear();
    }


}
