package com.example.savourit.feature.home;

import android.app.Application;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.savourit.BuildConfig;
import com.example.savourit.data.models.RestaurantModel;
import com.example.savourit.data.remote.FireStoreHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();
    private final Map<String, Marker> markerMap = new HashMap<>();
    private final MutableLiveData<List<RestaurantModel>> nearbyRestaurants = new MutableLiveData<>();
    private final FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, RestaurantModel> markerModelMap = new HashMap<>();

    private final Map<String, Boolean> likedStatusMap = new HashMap<>();


    public HomeViewModel(@NonNull Application application) {
        super(application);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Location> getUserLocation() {
        return userLocation;
    }

    public LiveData<List<RestaurantModel>> getNearbyRestaurants() {
        return nearbyRestaurants;
    }
    public Map<String, Boolean> getLikedStatusMap() {
        return likedStatusMap;
    }

    public void fetchLikedStatusForUser(List<RestaurantModel> restaurants, Runnable onComplete) {
        likedStatusMap.clear();
        FireStoreHelper.getInstance().getUserLikedRestaurantIds(new FireStoreHelper.FireStoreCallback<>() {
            @Override
            public void onSuccess(List<String> placeIdList) {
                for (RestaurantModel model : restaurants) {
                    boolean isLiked = placeIdList.contains(model.getPlaceId());
                    model.setLiked(isLiked);
                    likedStatusMap.put(model.getPlaceId(), isLiked);
                }
                onComplete.run();
            }

            @Override
            public void onFailure(String errorMsg) {
                onComplete.run();
            }
        });
    }

    public void likeOrUnlikeRestaurant(RestaurantModel model, Runnable onComplete) {
        String placeId = model.getPlaceId();
        boolean currentlyLiked = Boolean.TRUE.equals(likedStatusMap.get(placeId));

        if (currentlyLiked) {
            FireStoreHelper.getInstance().unlikeRestaurant(model,
                    () -> {
                        likedStatusMap.put(placeId, false);
                        model.setLiked(false);
                        onComplete.run();
                    },
                    onComplete
            );
        } else {
            FireStoreHelper.getInstance().likeRestaurant(model,
                    () -> {
                        likedStatusMap.put(placeId, true);
                        model.setLiked(true);
                        onComplete.run();
                    },
                    onComplete
            );
        }
    }


    public boolean isRestaurantLiked(String placeId) {
        Boolean liked = likedStatusMap.get(placeId);
        return liked != null && liked;
    }
    public void requestUserLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    userLocation.setValue(location);
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void fetchRestaurants(Location location, String selectedBudget) {
        executorService.execute(() -> {
            List<RestaurantModel> resultList = new ArrayList<>();
            try {
                JSONArray results = getJsonArray(location, BuildConfig.PLACES_API_KEY);

                for (int i = 0; i < results.length(); i++) {
                    JSONObject obj = results.getJSONObject(i);
                    String name = obj.getString("name");
                    String address = obj.getString("vicinity");
                    String icon = obj.optString("icon", "");
                    double lat = obj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng = obj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    String placeId = obj.getString("place_id");

                    int priceLevel = obj.has("price_level") ? obj.getInt("price_level") : 0;
                    if (isWithinSelectedBudget(priceLevel, selectedBudget)) {
                        resultList.add(new RestaurantModel(placeId,name, address, icon, new LatLng(lat, lng), placeId));
                    }
                }

                mainHandler.post(() -> nearbyRestaurants.setValue(resultList));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @NonNull
    private static JSONArray getJsonArray(Location location, String apiKey) throws Exception {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                + "location=" + location.getLatitude() + "," + location.getLongitude()
                + "&radius=1000&type=restaurant&key=" + apiKey;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) json.append(line);

        JSONObject jsonObject = new JSONObject(json.toString());
        return jsonObject.getJSONArray("results");
    }

    private boolean isWithinSelectedBudget(int priceLevel, String selectedBudget) {
        switch (selectedBudget) {
            case "No Budget": return true;
            case "€1-€10": return priceLevel == 1;
            case "€10-€20": return priceLevel == 2;
            case "€20-€30": return priceLevel == 3;
            default: return false;
        }
    }


    public void plotMarkersOnMap(GoogleMap map, List<RestaurantModel> restaurants) {
        if (map == null || restaurants == null) return;
        map.clear();
        markerMap.clear();
        markerModelMap.clear();

        for (RestaurantModel model : restaurants) {
            LatLng latLng = model.getLatLng();

            if (latLng != null) {
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(model.getName())
                        .snippet(model.getAddress());

                Marker marker = map.addMarker(options);
                if (marker != null) {
                    marker.setTag(model.getPlaceId());
                    markerModelMap.put(model.getPlaceId(), model);
                    markerMap.put(model.getPlaceId(), marker);
                }
            }
        }

    }

    public Marker getMarkerByPlaceId(String placeId) {
        return markerMap.get(placeId);
    }

    public void clearAllMarkersExcept(String selectedPlaceId) {
        for (Map.Entry<String, Marker> entry : markerMap.entrySet()) {
            Marker marker = entry.getValue();
            marker.setVisible(entry.getKey().equals(selectedPlaceId));
        }
    }

    public void showAllMarkers() {
        for (Marker marker : markerMap.values()) {
            marker.setVisible(true);
        }
    }

    public RestaurantModel getRestaurantByPlaceId(String placeId) {
        return markerModelMap.get(placeId);
    }

    public interface PolylineCallback {
        void onPolylineReady(List<LatLng> polylinePoints);
    }

    public void getRoutePolyline(LatLng origin, LatLng destination, PolylineCallback callback) {
        executorService.execute(() -> {
            try {
                String url = "https://maps.googleapis.com/maps/api/directions/json?"
                        + "origin=" + origin.latitude + "," + origin.longitude
                        + "&destination=" + destination.latitude + "," + destination.longitude
                        + "&mode=walking"
                        + "&key=" + BuildConfig.PLACES_API_KEY;

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONObject json = new JSONObject(response.toString());
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() > 0) {
                    String encoded = routes.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points");

                    List<LatLng> points = PolyUtil.decode(encoded);

                    mainHandler.post(() -> callback.onPolylineReady(points));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}