package com.example.savourit.feature.settings;

import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.example.savourit.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsFragment extends Fragment {

    private SettingsViewModel mViewModel;
    private SwitchCompat notificationSwitch;
    private SwitchCompat darkModeSwitch;
    private SwitchCompat autoUpdateSwitch;
    private Button accountButton;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationSwitch = view.findViewById(R.id.switch_notifications);
        darkModeSwitch = view.findViewById(R.id.switch_dark_mode);
        autoUpdateSwitch = view.findViewById(R.id.switch_auto_update);
        accountButton = view.findViewById(R.id.account_settings_button);

        setButtons();
    }

    private void setButtons(){
        if (notificationSwitch != null) {
            notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    Toast.makeText(requireContext(),
                            isChecked ? "Notifications Enabled" : "Notifications Disabled", Toast.LENGTH_SHORT).show());
        }

        if (darkModeSwitch != null) {
            darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    Toast.makeText(requireContext(),
                            isChecked ? "Dark Mode Enabled" : "Dark Mode Disabled", Toast.LENGTH_SHORT).show());
        }

        if (autoUpdateSwitch != null) {
            autoUpdateSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    Toast.makeText(requireContext(),
                            isChecked ? "Auto-Update Enabled" : "Auto-Update Disabled", Toast.LENGTH_SHORT).show());
        }

        if (accountButton != null) {
            accountButton.setOnClickListener(v -> {
//                Intent intent = new Intent(requireContext(), AccountSettingsActivity.class);
//                startActivity(intent);
            });
        }
    }
}