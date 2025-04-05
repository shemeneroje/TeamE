package com.example.savourit.feature.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.savourit.BuildConfig;
import com.example.savourit.R;
import com.example.savourit.adapters.RestaurantAdapter;
import com.example.savourit.data.models.RestaurantModel;
import com.example.savourit.data.remote.FireStoreHelper;
import com.example.savourit.utils.UserUtils;
import com.example.savourit.utils.ViewUtils;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

// Fragment to handle the home screen with map and restaurant listings
public class HomeFragment extends Fragment implements OnMapReadyCallback {

    // Google Map object
    private GoogleMap mMap;
    private HomeViewModel viewModel;
    private Polyline currentPolyline;
    private String selectedBudget = "No Budget";
    private LatLng currentDestination;

    // UI elements
    private Button stopRouteBtn;
    private ImageView filterIcon;
    private ProgressBar mapLoadingIndicator;
    private RecyclerView restaurantRecyclerView;

    // Adapter for displaying nearby restaurants
    private RestaurantAdapter restaurantAdapter;

    // Location services
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;

    // Permission request for location
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) viewModel.requestUserLocation();
                else Toast.makeText(requireContext(), "Location permission is required.", Toast.LENGTH_SHORT).show();
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize ViewModel and location client
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Initialize Google Places API
        initPlaces();
        // Set up UI views and listeners
        initUI(root);
        // Set up autocomplete search
        setUpAutocomplete();
        // Observe LiveData for location and restaurant data
        observeLiveData();

        // Load the Google Map asynchronously
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        return root;
    }

    // Initialize Google Places if not already done
    private void initPlaces() {
        if (!Places.isInitialized() && !TextUtils.isEmpty(BuildConfig.PLACES_API_KEY)) {
            Places.initialize(requireContext(), BuildConfig.PLACES_API_KEY);
        }
    }

    // Set up UI components and listeners
    private void initUI(View root) {
        stopRouteBtn = root.findViewById(R.id.stopRoute);
        filterIcon = root.findViewById(R.id.filterIcon);
        restaurantRecyclerView = root.findViewById(R.id.floatingRestaurantList);
        restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mapLoadingIndicator = root.findViewById(R.id.mapLoadingIndicator);

        // Stop navigation button click
        stopRouteBtn.setOnClickListener(v -> stopRouteNavigation());

        // Filter icon click
        filterIcon.setOnClickListener(this::showFilterPopup);

        // Recenter to user's location
        ImageView myLocationBtn = root.findViewById(R.id.btnMyLocation);
        myLocationBtn.setOnClickListener(v -> {
            Location loc = viewModel.getUserLocation().getValue();
            if (loc != null && mMap != null) {
                LatLng userLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
            } else {
                Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Observe location and restaurant data from ViewModel
    private void observeLiveData() {
        viewModel.getUserLocation().observe(getViewLifecycleOwner(), location -> {
            if (mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 15));
            }
            viewModel.fetchRestaurants(location, selectedBudget);
        });

        viewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            if (restaurants == null || restaurants.isEmpty()) return;

            mapLoadingIndicator.setVisibility(View.GONE);
            viewModel.plotMarkersOnMap(mMap, restaurants);

            // Check premium status and show data
            viewModel.fetchLikedStatusForUser(restaurants, () -> {
                UserUtils.isUserPremium(isPremium -> requireActivity().runOnUiThread(() -> {
                    Log.d("HomeFragment", "User is Premium: " + isPremium);

                    // Initialize adapter with interaction listeners
                    restaurantAdapter = new RestaurantAdapter(
                            restaurants,
                            isPremium,
                            viewModel.getLikedStatusMap(),
                            new RestaurantAdapter.OnItemInteractionListener() {

                                @Override
                                public void onLike(RestaurantModel model) {
                                    boolean isLiked = viewModel.isRestaurantLiked(model.getPlaceId());
                                    String toastText = isLiked ? "Removed from favorites: " : "Liked: ";
                                    Toast.makeText(requireContext(), toastText + model.getName(), Toast.LENGTH_SHORT).show();

                                    viewModel.likeOrUnlikeRestaurant(model, () -> {
                                        restaurantAdapter.updateLikedStatus(viewModel.getLikedStatusMap());
                                    });
                                }

                                @Override
                                public void onRate(RestaurantModel model) {
                                    showRateDialog(model);
                                }

                                @Override
                                public void onSelect(RestaurantModel model) {
                                    Toast.makeText(requireContext(), "Selected " + model.getName(), Toast.LENGTH_SHORT).show();

                                    if (mMap != null) {
                                        Marker selectedMarker = viewModel.getMarkerByPlaceId(model.getPlaceId());
                                        LatLng latLng = model.getLatLng();

                                        if (selectedMarker != null && latLng != null) {
                                            viewModel.clearAllMarkersExcept(model.getPlaceId());
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                                            selectedMarker.showInfoWindow();
                                        }

                                        Location userLoc = viewModel.getUserLocation().getValue();
                                        if (userLoc != null && latLng != null) {
                                            currentDestination = latLng;
                                            drawRoute(new LatLng(userLoc.getLatitude(), userLoc.getLongitude()), currentDestination);
                                            startLocationUpdates();
                                            stopRouteBtn.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }

                            });

                    restaurantRecyclerView.setAdapter(restaurantAdapter);
                }));
            });
        });
    }

    // Shows a custom rating dialog for the selected restaurant
    private void showRateDialog(RestaurantModel model) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.rate_dialog, null);
        TextView name = dialogView.findViewById(R.id.rateRestaurantTitle);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText reviewInput = dialogView.findViewById(R.id.reviewInput);

        // Custom tint for the rating bar
        ratingBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange)));
        ratingBar.setSecondaryProgressTintList(ColorStateList.valueOf(Color.LTGRAY));

        name.setText(model.getName());

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                Toast.makeText(requireContext(), "Selected Rating: " + rating, Toast.LENGTH_SHORT).show();
            }
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Rate Restaurant")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String reviewText = reviewInput.getText().toString().trim();

                    if (rating == 0f) {
                        Toast.makeText(requireContext(), "Please select a rating.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showLoading();
                    FireStoreHelper.getInstance().submitRestaurantReview(
                            model.getPlaceId(),
                            reviewText,
                            rating,
                            this::hideLoading,
                            this::hideLoading
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLoading() {
        mapLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mapLoadingIndicator.setVisibility(View.GONE);
    }

    // Setup autocomplete search bar for places
    private void setUpAutocomplete() {
        AutocompleteSupportFragment fragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (fragment != null) {
            fragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            fragment.setCountry("IE");

            fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    if (place.getLatLng() != null && mMap != null) {
                        mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                    }
                }

                @Override
                public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                    Toast.makeText(requireContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Callback once the Google Map is ready
    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mapLoadingIndicator.setVisibility(View.VISIBLE);
            viewModel.requestUserLocation();
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Hide/show restaurant list when moving map
        mMap.setOnCameraMoveListener(() -> restaurantRecyclerView.setVisibility(View.GONE));
        mMap.setOnCameraIdleListener(() -> restaurantRecyclerView.setVisibility(View.VISIBLE));

        // Click on marker to start route
        mMap.setOnMarkerClickListener(marker -> {
            currentDestination = marker.getPosition();
            Location userLoc = viewModel.getUserLocation().getValue();
            if (userLoc != null) {
                drawRoute(new LatLng(userLoc.getLatitude(), userLoc.getLongitude()), currentDestination);
                startLocationUpdates();
                stopRouteBtn.setVisibility(View.VISIBLE);
            }
            return true;
        });

        // Custom info window for markers
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(@NonNull Marker marker) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_info_window, null);
                TextView nameText = view.findViewById(R.id.info_Name);
                ImageView imageView = view.findViewById(R.id.info_Image);

                String placeId = (String) marker.getTag();
                RestaurantModel model = viewModel.getRestaurantByPlaceId(placeId);
                if (model != null) {
                    nameText.setText(model.getName());
                    if (model.getImageUrl().isEmpty()) {
                        imageView.setVisibility(View.GONE);
                    }
                    Glide.with(view.getContext()).load(model.getImageUrl()).into(imageView);
                }
                return view;
            }
        });
    }

    // Draws a route on the map between user and destination
    private void drawRoute(LatLng origin, LatLng destination) {
        ViewUtils.getInstance().fadeView(restaurantRecyclerView, false);
        mapLoadingIndicator.setVisibility(View.VISIBLE);

        viewModel.getRoutePolyline(origin, destination, polyline -> {
            mapLoadingIndicator.setVisibility(View.GONE);

            if (mMap != null) {
                if (currentPolyline != null) currentPolyline.remove();
                currentPolyline = mMap.addPolyline(new PolylineOptions()
                        .addAll(polyline)
                        .width(20)
                        .color(Color.BLUE)
                        .geodesic(true));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng point : polyline) builder.include(point);
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 110));
            }
        });
    }

    // Starts continuous location updates to update the route
    private void startLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (result.getLastLocation() != null && currentDestination != null) {
                    drawRoute(new LatLng(
                            result.getLastLocation().getLatitude(),
                            result.getLastLocation().getLongitude()), currentDestination);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        }
    }

    // Stops navigation and route updates
    private void stopRouteNavigation() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        stopRouteBtn.setVisibility(View.GONE);
        viewModel.showAllMarkers();
        if (currentPolyline != null) {
            currentPolyline.remove();
            currentPolyline = null;
        }

        if (currentDestination != null && mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentDestination, 17));
        }

        currentDestination = null;
        ViewUtils.getInstance().fadeView(restaurantRecyclerView, true);
    }

    // Shows budget filter popup
    private void showFilterPopup(View anchor) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_filter_menu, null);
        ListView listView = popupView.findViewById(R.id.listView);
        String[] budgetOptions = {"No Budget", "€1-€10", "€10-€20", "€20-€30"};

        listView.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, budgetOptions));
        PopupWindow popup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.showAsDropDown(anchor);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedBudget = budgetOptions[position];
            popup.dismiss();

            Location location = viewModel.getUserLocation().getValue();
            if (location != null) {
                mapLoadingIndicator.setVisibility(View.VISIBLE);
                viewModel.fetchRestaurants(location, selectedBudget);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopRouteNavigation();
    }
}
