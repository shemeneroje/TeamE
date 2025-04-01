package com.example.savourit.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPrefs {
    private static final String PREFS_NAME = "savourit_prefs";
    private static final String KEY_LAUNCH_COUNT = "launch_count";

    public static int incrementLaunchCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_LAUNCH_COUNT, current).apply();
        return current;
    }

    public static void resetLaunchCount(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_LAUNCH_COUNT, 0).apply();
    }
}
