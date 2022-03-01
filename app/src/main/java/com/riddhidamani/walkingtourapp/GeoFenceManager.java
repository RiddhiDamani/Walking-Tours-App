package com.riddhidamani.walkingtourapp;

import android.app.PendingIntent;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GeoFenceManager {

    private static final String TAG = "GeoFenceManager";
    //private final MapsActivity mapsActivity;
    //private final GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private final ArrayList<Circle> circles = new ArrayList<>();
    private final List<PatternItem> pattern = Collections.singletonList(new Dot());
    private static final ArrayList<GeoFenceData> fenceList = new ArrayList<>();

    private ArrayList<LatLng> tourPath;

    // hash map
    private static HashMap<String, GeoFenceData> buildingHashmap = new HashMap<String, GeoFenceData>();

}
