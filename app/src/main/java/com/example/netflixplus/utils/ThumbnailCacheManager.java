package com.example.netflixplus.utils;

import android.content.Context;

import java.io.File;

public class ThumbnailCacheManager {
    private static final String CACHE_DIR = "movie_thumbnails";
    private final Context context;
    private final File cacheDir;

    public ThumbnailCacheManager(Context context) {
        this.context = context;
        // Use external cache if available, fall back to internal
        this.cacheDir = context.getExternalCacheDir() != null
                ? new File(context.getExternalCacheDir(), CACHE_DIR)
                : new File(context.getCacheDir(), CACHE_DIR);

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public File getThumbnailFile(String objectName) {
        return new File(cacheDir, objectName);
    }

    public boolean isThumbnailCached(String objectName) {
        File thumbnailFile = getThumbnailFile(objectName);
        return thumbnailFile.exists();
    }

    // Clear old thumbnails periodically or when storage is low
    public void clearOldThumbnails(long maxAge) {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            long now = System.currentTimeMillis();
            for (File file : files) {
                if (now - file.lastModified() > maxAge) {
                    file.delete();
                }
            }
        }
    }
}