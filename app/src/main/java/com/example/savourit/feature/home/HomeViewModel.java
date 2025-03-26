package com.example.savourit.feature.home;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();
    private final FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService executorService;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    // Dummy restaurant data
    private final Map<String, LatLng> allRestaurants = new HashMap<String, LatLng>() {{
        put("Pizza Place", new LatLng(53.345, -6.260));
        put("Sushi Spot", new LatLng(53.346, -6.261));
        put("Indian Delight", new LatLng(53.347, -6.262));
        put("Mexican Fiesta", new LatLng(53.348, -6.263));
        put("French Bistro", new LatLng(53.349, -6.264));
    }};

    public void loadRestaurants(GoogleMap map, String filter) {
        if (map == null) return;

        map.clear();
        for (Map.Entry<String, LatLng> entry : allRestaurants.entrySet()) {
            if (filter.equals("All") || entry.getKey().toLowerCase().contains(filter.toLowerCase())) {
                map.addMarker(new MarkerOptions()
                        .position(entry.getValue())
                        .title(entry.getKey()));
            }
        }
    }
    public HomeViewModel(@NonNull Application application) {
        super(application);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Location> getUserLocation() {
        return userLocation;
    }

    public void requestLocationPermission(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        executorService.execute(() -> fetchLocation(context));
    }

    private void fetchLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLocation.postValue(location); // Update LiveData in Background
                    }
                });
    }

    public void enableUserLocation(Context context, GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Run UI-related updates on the Main Thread
            mainThreadHandler.post(() -> googleMap.setMyLocationEnabled(true));
            requestLocationPermission(context);
        }
    }

    public void updateMapLocation(GoogleMap googleMap, Location location) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Run UI-related updates on the Main Thread
        mainThreadHandler.post(() -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(userLatLng).title("You are here"));
            googleMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown(); // Shut down executor service when ViewModel is cleared
    }
}
