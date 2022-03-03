package com.riddhidamani.walkingtourapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.riddhidamani.walkingtourapp.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = getClass().getSimpleName();
    private static final int LOCATION_REQUEST_CODE = 111;
    private static final int BACKGROUND_LOCATION_REQUEST = 333;
    private static final float INIT_ZOOM = 17.0f;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    // Location Stuff
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private final ArrayList<LatLng> latLonHistory = new ArrayList<>();
    private Polyline llHistoryPolyline;
    private boolean travelPathVisibility = true;
    private boolean tourPathVisibility = true;
    private Polyline tourPathPolyline;

    // Zoom Stuff
    private boolean zooming = false;
    private float oldZoom ;
    private final float zoomDefault = 16.0f;
    // Geofencing Stuff
    private GeoFenceManager geoFenceManager;
    private Geocoder geocoder;
    public static int screen_height;
    public static int screen_width;

    private Marker manMarker;

    private final List<PatternItem> pattern = Collections.singletonList(new Dot());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getScreenDimensions();
        geocoder = new Geocoder(this);

        setupMap();
    }

    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_height = displayMetrics.heightPixels;
        screen_width = displayMetrics.widthPixels;
    }

    // Map stuff
    private void setupMap() {

        geoFenceManager = new GeoFenceManager(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomDefault));
        zooming = true;

        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);

        if (checkAppPermission()) {
            //getInitialLocation();
            setupLocationListener();
            setupZoomListener();
        }
        setupZoomListener();
    }


    /////////////////////////////
    // Perm stuff!
    /////////////////////////////

    private boolean checkAppPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getBackgroundLocPerm();
                } else {
                    Toast.makeText(this, "Location Permission not Granted", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == BACKGROUND_LOCATION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        setupLocationListener();
                        //setupMap();
                    } else {
                        Toast.makeText(this, "Location Permission not Granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void getBackgroundLocPerm() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            return;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "NEED BASIC PERMS FIRST!", Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_REQUEST);
        } else {
            Toast.makeText(this, "ALREADY HAS BACKGROUND LOC PERMS", Toast.LENGTH_LONG).show();
        }
    }

    public void setupLocationListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new AppLocationListener(this);

        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
        }
    }

    private void setupZoomListener() {
        mMap.setOnCameraIdleListener(() -> {
            if (zooming) {
                Log.d(TAG, "onCameraIdle: DONE ZOOMING: " + mMap.getCameraPosition().zoom);
                zooming = false;
                oldZoom = mMap.getCameraPosition().zoom;
            }
        });

        mMap.setOnCameraMoveListener(() -> {
            if (mMap.getCameraPosition().zoom != oldZoom) {
                Log.d(TAG, "onCameraMove: ZOOMING: " + mMap.getCameraPosition().zoom);
                zooming = true;
            }
        });
    }


    private void getInitialLocation() {
        FusedLocationProviderClient mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location.
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    }
                });

        mFusedLocationClient.getLastLocation().addOnFailureListener(this,
                e -> Log.d(TAG, "onFailure: "));
    }

    public void updateLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLonHistory.add(latLng); // Add the LL to our location history

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addresses.get(0);
            binding.mapCurrentAddress.setText(address.getAddressLine(0));

        } catch (IOException e) {
            e.printStackTrace();
            binding.mapCurrentAddress.setText("");
        }

        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomDefault));
            zooming = true;
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }

            llHistoryPolyline = mMap.addPolyline(polylineOptions);
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(20);
            llHistoryPolyline.setColor(Color.parseColor("#006948"));

            // set visibility base on check box
            if(travelPathVisibility) {
                llHistoryPolyline.setVisible(true);
            }else {
                llHistoryPolyline.setVisible(false);
            }

            float r = getRadius();
            // Try for Extra Credit
            if (r > 0) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);
                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);
                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(iconBitmap);
                options.rotation(location.getBearing());

                if (manMarker != null) {
                    manMarker.remove();
                }

                manMarker = mMap.addMarker(options);
            }
        }
        if (!zooming)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        float factor = (float) ((35.0 / 2.0 * z) - (355.0 / 2.0));
        float multiplier = ((7.0f / 7200.0f) * screen_width) - (1.0f / 20.0f);
        float radius = factor * multiplier;
        return radius;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public void getTourPath(ArrayList<LatLng> list) {

        // draw path
        PolylineOptions polylineOptions = new PolylineOptions();

        for (LatLng ll : list) {
            polylineOptions.add(ll);
        }

        tourPathPolyline = mMap.addPolyline(polylineOptions);
        tourPathPolyline.setEndCap(new RoundCap());
        tourPathPolyline.setWidth(12);
        tourPathPolyline.setColor(ContextCompat.getColor(this, R.color.path_orange));

        // set visibility base on check box
        if (travelPathVisibility) {
            tourPathPolyline.setVisible(true);
        } else {
            tourPathPolyline.setVisible(false);
        }
    }

    // ------ VISUALIZATION CONTROLS START ----------------

    // Maps Checkbox1 - show address details
    public void showAddressDetails(View view) {
        CheckBox checkBox = (CheckBox) view;
        if (checkBox.isChecked()) {
            binding.mapCurrentAddress.setVisibility(TextView.VISIBLE);
        } else {
            binding.mapCurrentAddress.setVisibility(TextView.INVISIBLE);
        }
    }

    // Maps Checkbox2 - show geo fence details
    public void showGeoFenceDetails(View view) {
        CheckBox checkBox = (CheckBox) view;
        if (checkBox.isChecked()) {
            geoFenceManager.drawFences();
        } else {
            geoFenceManager.eraseFences();
        }
    }

    // Maps Checkbox3 - show travel path details
    public void showTravelPathDetails(View view) {
        CheckBox checkBox = (CheckBox) view;
        if (checkBox.isChecked()) {
            travelPathVisibility = true;
            llHistoryPolyline.setVisible(true);
        } else {
            travelPathVisibility = false;
            llHistoryPolyline.setVisible(false);
        }
    }

    // Maps Checkbox4 - show tour path details
    public void showTourPathDetails(View view) {
        CheckBox checkBox = (CheckBox) view;
        if (checkBox.isChecked()) {
            tourPathVisibility = true;
            tourPathPolyline.setVisible(true);
        } else {
            tourPathVisibility = false;
            tourPathPolyline.setVisible(false);
        }
    }

    // ------ VISUALIZATION CONTROLS END ----------------

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkAppPermission() && locationManager != null && locationListener != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }
}