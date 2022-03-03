package com.riddhidamani.walkingtourapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    private static final int LOCATION_REQUEST_CODE = 111;
    private static final int BACKGROUND_LOCATION_REQUEST = 333;
    private static final int SPLASH_TIME_OUT = 3000;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setTitle("Walking Tours");

        progressBar = findViewById(R.id.progress_bar);
        progressBar.getProgress();

        // Possibly check perm's here
        // Possibly load required resources here
        checkLocationAccuracy();
        determineLocation();

    }

    private void checkLocationAccuracy() {

        Log.d(TAG, "checkLocationAccuracy: ");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            Log.d(TAG, "onSuccess: High Accuracy Already Present");
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(SplashActivity.this, BACKGROUND_LOCATION_REQUEST);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });
    }

    private void determineLocation() {
        if (checkAppPermission()) {
            Log.d(TAG, "determineLocation: Let's Jump to MapsActivity");
            // Handler is used to execute something in the future
            new Handler().postDelayed(() -> {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out); // new act, old act
                // close this activity
                finish();
            }, SPLASH_TIME_OUT);
        }
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
                    exitWTApp();
                    Toast.makeText(this, "Location Permission not Granted", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == BACKGROUND_LOCATION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        //setupMap();
                        determineLocation();
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

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Allow Walking Tours to access the background location?");
            builder.setMessage("This app may want to access the your location all the time, even when you're not using the app.");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_REQUEST);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    exitWTApp();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(this, "ALREADY HAS BACKGROUND LOC PERMS", Toast.LENGTH_LONG).show();
        }
    }

    private void exitWTApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Walking Tour App Cannot Run Without Permissions");
        builder.setMessage("This app cannot be used without fine location access and background Location access. \n\n" +
                "Click ok to exit the app");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BACKGROUND_LOCATION_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: ");
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("High-Accuracy Location Services Required");
            builder.setMessage("High-Accuracy Location Services Required");
            builder.setPositiveButton("OK", (dialog, id) -> finish());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}