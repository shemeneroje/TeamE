package com.example.savourit.feature.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.savourit.data.models.RestaurantModel;
import com.example.savourit.data.remote.FireStoreHelper;
import com.example.savourit.databinding.FragmentProfileBinding;
import com.example.savourit.feature.auth.forget.ForgotPasswordActivity;
import com.example.savourit.feature.restaurant.Restaurant;
import com.example.savourit.feature.restaurant.RestaurantAdapter;
import com.example.savourit.feature.update.UpgradePlanActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    private RestaurantAdapter restaurantAdapter;
    private final List<RestaurantModel> likedRestaurants = new ArrayList<>();

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Observe user info
        viewModel.getFirstName().observe(getViewLifecycleOwner(), name -> binding.firstNameTitle.setText("First Name: " + name));
        viewModel.getLastName().observe(getViewLifecycleOwner(), name -> binding.lastNameTitle.setText("Last Name: " + name));
        viewModel.getEmail().observe(getViewLifecycleOwner(), email -> binding.emailAddressTitle.setText(email));
        viewModel.getUsername().observe(getViewLifecycleOwner(), username -> binding.usernameTitle.setText("Username: " + username));
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Setup RecyclerView for liked restaurants
        binding.recyclerLiked.setLayoutManager(new LinearLayoutManager(requireContext()));
        restaurantAdapter = new RestaurantAdapter(requireContext(), likedRestaurants);
        binding.recyclerLiked.setAdapter(restaurantAdapter);

        // Load liked restaurants
        if (mAuth.getCurrentUser() != null) {
            loadLikedRestaurants();
        }

        // Reset password
        binding.resetButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ForgotPasswordActivity.class)));

        // Upgrade plan
        binding.upgradeButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), UpgradePlanActivity.class)));

        return binding.getRoot();
    }

    private void loadLikedRestaurants() {
        FireStoreHelper.getInstance().getUserLikedRestaurants(new FireStoreHelper.FireStoreCallback<List<RestaurantModel>>() {
            @Override
            public void onSuccess(List<RestaurantModel> result) {
                requireActivity().runOnUiThread(() -> {
                    likedRestaurants.clear();
                    likedRestaurants.addAll(result);
                    restaurantAdapter.notifyDataSetChanged();

                    if (result.isEmpty()) {
                        binding.recyclerLiked.setVisibility(View.GONE);
                        binding.emptyLikedText.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerLiked.setVisibility(View.VISIBLE);
                        binding.emptyLikedText.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    binding.recyclerLiked.setVisibility(View.GONE);
                    binding.emptyLikedText.setVisibility(View.VISIBLE);
                });
            }
        });
    }

}
