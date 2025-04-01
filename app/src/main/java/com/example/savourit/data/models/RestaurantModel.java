package com.example.savourit.data.models;

import com.google.android.gms.maps.model.LatLng;

public class RestaurantModel {
    private String name;
    private String address;
    private String imageUrl;
    private MyLatLng location;
    private String placeId;
    private boolean isLiked;
    private String id;

    public RestaurantModel() {} // Required for Firestore

    public RestaurantModel(String id, String name, String address, String imageUrl, LatLng latLng, String placeId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.location = MyLatLng.fromLatLng(latLng); // Convert to custom serializable
        this.placeId = placeId;
        this.isLiked = false;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public MyLatLng getLocation() {
        return location;
    }

    public LatLng getLatLng() {
        return location != null ? location.toLatLng() : null;
    }

    public void setLocation(MyLatLng location) {
        this.location = location;
    }

    public String getPlaceId() {
        return placeId;
    }
}
