package com.example.netflixplus.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MovieProgressManager {
    private static final String PREF_NAME = "MovieProgress";
    private static final String KEY_PREFIX = "movie_progress_";

    private final SharedPreferences preferences;

    public MovieProgressManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveProgress(String movieId, int minutes) {
        preferences.edit()
                .putInt(KEY_PREFIX + movieId, minutes)
                .apply();
    }

    public int getProgress(String movieId) {
        return preferences.getInt(KEY_PREFIX + movieId, 0);
    }

    public void clearProgress(String movieId) {
        preferences.edit()
                .remove(KEY_PREFIX + movieId)
                .apply();
    }
}
