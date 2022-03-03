package com.riddhidamani.walkingtourapp;

import android.location.Geocoder;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeoFenceDataVolley {

    private static final String TAG = "GeoFenceDataVolley";
    private static final String tour_path_url = "http://www.christopherhield.com/data/WalkingTourContent.json";
    // private final Geocoder geocoder;
    // private final GeoFenceManager geoFenceManager;


    public static void getFenceData(MapsActivity mapsActivity, GeoFenceManager geoFenceManager) {

        RequestQueue queue = Volley.newRequestQueue(mapsActivity);

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(TAG, "Listener Response: " + response);

                    // FENCES ARRAY PROCESSING
                    ArrayList<GeoFenceData> fences = new ArrayList<>();
                    JSONArray fencesArray = response.getJSONArray("fences");
                    for (int i = 0; i < fencesArray.length(); i++) {
                        JSONObject fence = fencesArray.getJSONObject(i);
                        String buildingName = fence.getString("id");
                        String address = fence.getString("address");
                        Double latitude = fence.getDouble("latitude");
                        Double longitude = fence.getDouble("longitude");
                        float radius = (float) fence.getDouble("radius");
                        String description = fence.getString("description");
                        String fenceColor = fence.getString("fenceColor");
                        String image = fence.getString("image");

                        GeoFenceData newFence = new GeoFenceData(buildingName, address, latitude, longitude, radius, description, fenceColor, image);
                        fences.add(newFence);
                    }
                    Log.d(TAG, "onResponse:FenceVolley: Total Fences: " + fences.size() + " fences");
                    geoFenceManager.addFences(fences);

                    // TOUR PATH ARRAY PROCESSING

                    JSONArray tourPathArray = response.getJSONArray("path");
                    ArrayList<LatLng> LatLngList = new ArrayList<>();
                    for (int i = 0; i < tourPathArray.length(); i++) {
                        String point = tourPathArray.getString(i);
                        String[] stringArr = point.split("\\s*,\\s*");
                        Double longitude = Double.parseDouble(stringArr[0]);
                        Double latitude = Double.parseDouble(stringArr[1]);
                        LatLng latLng = new LatLng(latitude, longitude);
                        LatLngList.add(latLng);
                    }

                    mapsActivity.runOnUiThread(() -> {
                        mapsActivity.getTourPath(LatLngList);
                    });

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        };

        Response.ErrorListener error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
                String errorMsg = error.networkResponse == null ?
                        error.getClass().getName() : new String(error.networkResponse.data);
                //activity.runOnUiThread(() -> activity.handleError(errorMsg));
            }
        };

        // Request a json response from the provided URL.
        JsonRequest<JSONObject> jsonRequest = new JsonRequest<JSONObject>(
                Request.Method.GET, tour_path_url, null, listener, error) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // This method is always the same!
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(jsonRequest);

    }
}
