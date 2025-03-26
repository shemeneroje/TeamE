package com.example.savourit.feature.settings;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Spinner;
import android.widget.AdapterView;

import com.example.savourit.R;
import com.example.savourit.LocaleHelper;

public class SettingsFragment extends Fragment {

    private SettingsViewModel mViewModel;
    private SwitchCompat notificationSwitch;
    private SwitchCompat darkModeSwitch;
    private SwitchCompat autoUpdateSwitch;

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

        requireActivity().setTitle(R.string.title_settings);

        notificationSwitch = view.findViewById(R.id.switch_notifications);
        darkModeSwitch = view.findViewById(R.id.switch_dark_mode);
        autoUpdateSwitch = view.findViewById(R.id.switch_auto_update);

        Spinner spinnerLanguage = view.findViewById(R.id.spinner_language);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String currentLang = prefs.getString("lang", "en");

        if (currentLang.equals("pl")) {
            spinnerLanguage.setSelection(1);
        } else if (currentLang.equals("es")) {
            spinnerLanguage.setSelection(2);
        } else {
            spinnerLanguage.setSelection(0);
        }

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {

                String selectedLang;
                switch (position) {
                    case 1:
                        selectedLang = "pl";
                        break;
                    case 2:
                        selectedLang = "es";
                        break;
                    default:
                        selectedLang = "en";
                }

                String savedLang = prefs.getString("lang", "en");
                if (!selectedLang.equals(savedLang)) {
                    prefs.edit().putString("lang", selectedLang).apply();
                    LocaleHelper.setLocale(requireContext(), selectedLang);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        requireActivity().recreate();
                    }, 100);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        setButtons();
    }

    private void setButtons() {
        if (notificationSwitch != null) {
            notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    Toast.makeText(requireContext(),
                            isChecked ? "Notifications Enabled" : "Notifications Disabled", Toast.LENGTH_SHORT).show());
        }

        if (darkModeSwitch != null) {
            darkModeSwitch.setChecked(
                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                            .getBoolean("dark_mode", false)
            );

            darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(requireContext()).edit();
                editor.putBoolean("dark_mode", isChecked);
                editor.apply();

                AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            });
        }

        if (autoUpdateSwitch != null) {
            autoUpdateSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    Toast.makeText(requireContext(),
                            isChecked ? "Auto-Update Enabled" : "Auto-Update Disabled", Toast.LENGTH_SHORT).show());
        }
    }
}
