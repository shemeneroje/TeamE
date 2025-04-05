package com.example.savourit.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorUtils {
    private static ExecutorUtils instance;
    private final ExecutorService executor;

    private ExecutorUtils() {
        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized ExecutorUtils getInstance() {
        if (instance == null) {
            instance = new ExecutorUtils();
        }
        return instance;
    }

    public void runInBackground(Runnable task) {
        executor.execute(task);
    }
}
