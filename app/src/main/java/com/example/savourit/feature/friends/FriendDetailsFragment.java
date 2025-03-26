package com.example.savourit.feature.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.savourit.R;
import com.example.savourit.feature.friends.viewmodel.FriendDetailsViewModel;

public class FriendDetailsFragment extends Fragment {

    private TextView txtFriendUsername, txtFriendAccountType, txtFriendEmail;
    private Button btnBack;
    private FriendDetailsViewModel viewModel;

    public FriendDetailsFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtFriendUsername = view.findViewById(R.id.txtFriendUsername);
        txtFriendAccountType = view.findViewById(R.id.txtFriendAccountType);
        txtFriendEmail = view.findViewById(R.id.txtFriendEmail);
        btnBack = view.findViewById(R.id.btnBack);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(FriendDetailsViewModel.class);

        // Get friendId from arguments
        if (getArguments() != null) {
            String friendId = getArguments().getString("friendId");
            viewModel.loadFriendDetails(friendId);
        }

        // Observe friend details from ViewModel
        viewModel.getFriendDetails().observe(getViewLifecycleOwner(), friend -> {
            if (friend != null) {
                txtFriendUsername.setText(friend.getUsername());
                txtFriendAccountType.setText(friend.getAccountType());
                txtFriendEmail.setText(friend.getEmail());
            }
        });

        // Handle Back Button Click
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
    }
}
