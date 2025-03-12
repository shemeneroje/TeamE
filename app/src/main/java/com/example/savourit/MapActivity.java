package com.example.savourit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private String apiKey;
    private LocationCallback locationCallback;

    private FirebaseAnalytics analytics;
    private PlacesClient placesClient;
    private Spinner cuisineSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Firebase Analytics
        analytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MapActivity");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MapActivity");
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        // Load API Key
        apiKey = BuildConfig.PLACES_API_KEY;
        if (TextUtils.isEmpty(apiKey)) {
            Log.e("PlacesAPI", "No API key found!");
            Toast.makeText(this, "Error: No API Key found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        placesClient = Places.createClient(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);

        // Set up the spinner with an array of cuisines
        cuisineSpinner = findViewById(R.id.cuisineSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.cuisine_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineSpinner.setAdapter(adapter);

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up the AutocompleteSupportFragment for location search
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(java.util.Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                }

                @Override
                public void onError(com.google.android.gms.common.api.Status status) {
                    Toast.makeText(MapActivity.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Autocomplete Error", status.getStatusMessage());
                }
            });
        } else {
            Log.e("Autocomplete", "Fragment is null. Check XML layout.");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableUserLocation();
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                updateMapLocation(location);
            } else {
                requestLocationUpdates();
            }
        });
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    updateMapLocation(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateMapLocation(Location location) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLatLng).title("You are here"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
    }
}
