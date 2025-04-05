package com.example.savourit.data.models;

import com.google.android.gms.maps.model.LatLng;

public class MyLatLng {
    private double lat;
    private double lng;

    public MyLatLng() {} // Required no-arg constructor for Firebase

    public MyLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public LatLng toLatLng() {
        return new LatLng(lat, lng);
    }

    public static MyLatLng fromLatLng(LatLng latLng) {
        return new MyLatLng(latLng.latitude, latLng.longitude);
    }
}

