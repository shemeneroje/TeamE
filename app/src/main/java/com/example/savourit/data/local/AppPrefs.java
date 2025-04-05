package com.example.savourit.data.local;

import android.content.Context;
import android.content.SharedPreferences;

// AppPrefs is a utility class to manage SharedPreferences for saving and retrieving app-related preferences.
public class AppPrefs {
    // Define a constant for the SharedPreferences file name.
    private static final String PREFS_NAME = "savourit_prefs";

    // Define the key used for storing the app launch count.
    private static final String KEY_LAUNCH_COUNT = "launch_count";

    // This method increments the launch count stored in SharedPreferences and returns the new count.
    public static int incrementLaunchCount(Context context) {
        // Retrieve the SharedPreferences object using the private file name.
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Get the current launch count increment it by 1.
        int current = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1;

        // Save the updated launch count back to SharedPreferences.
        prefs.edit().putInt(KEY_LAUNCH_COUNT, current).apply();

        // Return the updated launch count.
        return current;
    }

    // This method resets the launch count to 0.
    public static void resetLaunchCount(Context context) {
        // Retrieve the SharedPreferences object and reset the launch count to 0.
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_LAUNCH_COUNT, 0).apply();
    }
}
