package com.riddhidamani.walkingtourapp;

import androidx.annotation.NonNull;
import java.io.Serializable;

public class GeoFenceData implements Serializable {

    private final String id;
    private final String address;
    private final double latitude;
    private final double longitude;
    private final float radius;
    private final String description;
    private final String fenceColor;
    private final String imageURL;

    public GeoFenceData(String id, String address, double latitude, double longitude, float radius, String description, String fenceColor, String imageURL) {
        this.id = id;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.description = description;
        this.fenceColor = fenceColor;
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getRadius() {
        return radius;
    }

    public String getDescription() {
        return description;
    }

    public String getFenceColor() {
        return fenceColor;
    }

    public String getImageURL() {
        return imageURL;
    }

    @NonNull
    @Override
    public String toString() {
        return "FenceData{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                ", lat=" + latitude +
                ", lon=" + longitude +
                ", radius=" + radius +
                ", description=" + description +
                ", fenceColor='" + fenceColor + '\'' +
                '}';
    }
}
