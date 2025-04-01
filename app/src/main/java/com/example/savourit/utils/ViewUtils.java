package com.example.savourit.utils;

import android.view.View;

public class ViewUtils {

    private static ViewUtils instance;

    private ViewUtils() {}

    public static synchronized ViewUtils getInstance() {
        if (instance == null) {
            instance = new ViewUtils();
        }
        return instance;
    }

    public void fadeView(View view, boolean show) {
        view.animate()
                .alpha(show ? 1f : 0f)
                .setDuration(300)
                .withStartAction(() -> { if (show) view.setVisibility(View.VISIBLE); })
                .withEndAction(() -> { if (!show) view.setVisibility(View.GONE); })
                .start();
    }
}

