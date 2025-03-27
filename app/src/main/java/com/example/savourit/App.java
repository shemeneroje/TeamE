package com.example.savourit;

import android.app.Application;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Install the emoji provider
        EmojiManager.install(new GoogleEmojiProvider());
    }
}