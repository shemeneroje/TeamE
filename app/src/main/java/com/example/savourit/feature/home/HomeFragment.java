package com.example.savourit.feature.home;

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
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

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

        executorService.execute(() -> viewModel.requestLocationPermission(requireContext()));

        viewModel.getUserLocation().observe(getViewLifecycleOwner(), location -> {
            if (mMap != null) {
                requireActivity().runOnUiThread(() -> viewModel.updateMapLocation(mMap, location));
            }
        });

        return rootView;
    }

    private void setUpAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setCountry("IE");
            autocompleteFragment.setPlaceFields(java.util.Arrays.asList(
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
        executorService.execute(() -> viewModel.enableUserLocation(requireContext(), mMap));
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
        String[] cuisines = {"All Cuisines", "Italian", "Chinese", "Indian", "Mexican", "French"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, cuisines);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedCuisine = cuisines[position];
            Toast.makeText(requireContext(), "Selected: " + selectedCuisine, Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchorView, 0, 10);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown();
    }
}
