package com.example.savourit.feature.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.savourit.BuildConfig;
import com.example.savourit.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.PlacesStatusCodes;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private PlacesClient placesClient;
    private FirebaseAnalytics analytics;
    private String apiKey;
    private ImageView filterIcon;
    private String selectedBudget = "No Budget";
    private HomeViewModel viewModel;
    private ExecutorService executorService;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Button stopNavigationButton;
    private LatLng currentDestination;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        executorService = Executors.newSingleThreadExecutor();
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        filterIcon = rootView.findViewById(R.id.filterIcon);
        filterIcon.setOnClickListener(this::showFilterPopup);

        setUpAutocomplete();
        setupMap();

        // Observe the user's location
        viewModel.getUserLocation().observe(getViewLifecycleOwner(), location -> {
            if (mMap != null && location != null) {
                // Get the user's LatLng and move the camera to the user's location
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));

                // Call the method to fetch nearby places
                fetchNearbyPlaces(userLatLng);  // Fetch nearby places after getting user location
            }
        });

        stopNavigationButton = rootView.findViewById(R.id.btnStopNavigation);
        stopNavigationButton.setOnClickListener(v -> {
            stopLocationUpdates();
            stopNavigationButton.setVisibility(View.GONE); // Hide button when stopping
        });

        return rootView;
    }

    private void setUpAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setCountry("IE");
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null && mMap != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                }

                @Override
                public void onError(com.google.android.gms.common.api.Status status) {
                    String errorMessage = status.getStatusMessage() != null ? status.getStatusMessage() : "Unknown error";
                    Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("Autocomplete Error", errorMessage);
                }
            });
        } else {
            Log.e("Autocomplete", "Fragment is null. Check XML layout.");
        }
    }

    private void setupMap() {
        analytics = FirebaseAnalytics.getInstance(requireContext());
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "HomeFragment");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "HomeFragment");
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        apiKey = BuildConfig.PLACES_API_KEY;
        if (TextUtils.isEmpty(apiKey)) {
            Log.e("PlacesAPI", "No API key found!");
            Toast.makeText(requireContext(), "Error: No API Key found!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey);
        }
        placesClient = Places.createClient(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        requestLocationPermission();

        mMap.setOnMarkerClickListener(marker -> {
            LatLng destination = marker.getPosition();
            currentDestination = destination; // Save destination for real-time tracking

            if (viewModel.getUserLocation().getValue() == null) {
                Toast.makeText(requireContext(), "User location not available", Toast.LENGTH_SHORT).show();
                return true;
            }

            LatLng userLocation = new LatLng(
                    viewModel.getUserLocation().getValue().getLatitude(),
                    viewModel.getUserLocation().getValue().getLongitude()
            );

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(destination).title(marker.getTitle()));

            // Start real-time tracking and fetch initial route
            startLocationUpdates();
            getDirections(userLocation, destination);

            // Fetch place details for the selected restaurant
            fetchPlaceDetails(marker);

            return true;
        });
    }


    private void fetchPlaceDetails(Marker marker) {
        String placeName = marker.getTitle();
        String placeAddress = marker.getSnippet();

        new AlertDialog.Builder(requireContext())
                .setTitle(placeName)
                .setMessage("Address: " + placeAddress + "\nDo you want directions?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // If Yes is clicked, get directions to the destination
                    LatLng userLocation = new LatLng(
                            viewModel.getUserLocation().getValue().getLatitude(),
                            viewModel.getUserLocation().getValue().getLongitude()
                    );
                    getDirections(userLocation, marker.getPosition());
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // If No is clicked, dismiss the dialog and leave all markers visible
                    dialog.dismiss();
                })
                .show();
    }




    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            viewModel.enableUserLocation(requireContext(), mMap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.enableUserLocation(requireContext(), mMap);
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showFilterPopup(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_filter_menu, null);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        ListView listView = popupView.findViewById(R.id.listView);
        String[] budget = {"No Budget", "€1-€10", "€10-€20", "€20-€30"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, budget);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedBudget = budget[position];
            Toast.makeText(requireContext(), "Selected: " + selectedBudget, Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchorView, 0, 10);
    }

    private void fetchNearbyPlaces(LatLng userLocation) {
        if (placesClient == null) {
            return;
        }

        // Define the radius for searching nearby places
        final int radius = 5000; // 5 km radius

        // Create a PlaceSearchRequest to find places around the user’s location
        String type = "restaurant|bar|cafe"; // Type filter to get restaurant, bar, and cafe

        // Build the request to search for nearby places
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                + "location=" + userLocation.latitude + "," + userLocation.longitude
                + "&radius=" + radius
                + "&type=" + type
                + "&key=" + BuildConfig.PLACES_API_KEY;

        // Use an HTTP request (e.g., OkHttp or Retrofit) to send this request to Google Places API.
        // For simplicity, here we demonstrate the network call to Places API.

        // Make a network request to fetch the nearby places asynchronously.
        executorService.execute(() -> {
            try {
                // Send HTTP request (use your networking library of choice like Retrofit/OkHttp)
                // Example with Retrofit or any HTTP client to get places
                // url for simplicity.
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    // Handle the response to parse the JSON and extract the places
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }

                    // Parse the response JSON to extract places
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray results = jsonResponse.getJSONArray("results");

                    // Add markers for each restaurant/bar/cafe
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject placeObject = results.getJSONObject(i);
                        String name = placeObject.getString("name");
                        double lat = placeObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        double lng = placeObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        String address = placeObject.getString("vicinity");

                        // Add markers to the map for each place
                        LatLng placeLatLng = new LatLng(lat, lng);
                        requireActivity().runOnUiThread(() -> {
                            mMap.addMarker(new MarkerOptions()
                                    .position(placeLatLng)
                                    .title(name)
                                    .snippet(address));
                        });
                    }
                } else {
                    Log.e("PlacesAPI", "Error fetching nearby places: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("PlacesAPI", "Error fetching nearby places", e);
            }
        });
    }

    private void getDirections(LatLng origin, LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "&mode=walking"
                + "&key=" + BuildConfig.PLACES_API_KEY;

        executorService.execute(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray routes = jsonResponse.getJSONArray("routes");

                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                        String encodedPolyline = overviewPolyline.getString("points");

                        requireActivity().runOnUiThread(() -> drawRoute(encodedPolyline));

                        // Check if user has arrived
                        float[] results = new float[1];
                        Location.distanceBetween(
                                origin.latitude, origin.longitude,
                                destination.latitude, destination.longitude,
                                results
                        );

                        if (results[0] < 50) { // If user is within 50 meters
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "You have arrived!", Toast.LENGTH_LONG).show();
                                stopLocationUpdates(); // Stop tracking
                            });
                        }
                    }
                } else {
                    Log.e("DirectionsAPI", "Error fetching directions: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("DirectionsAPI", "Error fetching directions", e);
            }
        });
    }


    private void drawRoute(String encodedPolyline) {
        List<LatLng> routePoints = PolyUtil.decode(encodedPolyline);
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(20)
                .color(Color.BLUE)
                .geodesic(true);

        mMap.addPolyline(polylineOptions);
    }


    private void startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)  // Update every 5 seconds
                .setFastestInterval(3000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) return;

                Location newLocation = locationResult.getLastLocation();
                LatLng newLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());

                // Update map camera
                mMap.animateCamera(CameraUpdateFactory.newLatLng(newLatLng));

                // Fetch updated directions to the destination
                if (currentDestination != null) {
                    getDirections(newLatLng, currentDestination);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown();
    }
}