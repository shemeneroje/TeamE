package com.example.savourit.feature.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.PlacesStatusCodes;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.analytics.FirebaseAnalytics;

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
    private String selectedCuisine = "All Cuisines";
    private HomeViewModel viewModel;
    private ExecutorService executorService;

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
        String[] cuisines = getResources().getStringArray(R.array.cuisine_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, cuisines);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedCuisine = cuisines[position];
            Toast.makeText(requireContext(), "Selected: " + selectedCuisine, Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchorView, 0, 10);
    }

    private void fetchNearbyPlaces(LatLng userLocation) {
        if (placesClient == null) {
            return;
        }

        // Define the radius for searching nearby places
        final int radius = 1000; // 1 km radius

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
                // We will use the url for simplicity.
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(R.string.title_home);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown();
    }
}
