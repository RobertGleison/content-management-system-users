package com.example.netflixplus.utils;

import android.content.Context;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class ThumbnailLoader {

    public void downloadObject(String projectId, String bucketName, String objectName, String destFilePath, Context context) {
        ThumbnailCacheManager thumbnailManager = new ThumbnailCacheManager(context);

        // Check if thumbnail is already cached
        if (!thumbnailManager.isThumbnailCached(objectName)) {
            // Download from Google Cloud Storage
            Storage storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .build()
                    .getService();

            Blob blob = storage.get(BlobId.of(bucketName, "thumbnail_testarconversao5.heic" ));
            blob.downloadTo(Paths.get(destFilePath));

        // Clear old thumbnails (e.g., older than 1 day)
        thumbnailManager.clearOldThumbnails(TimeUnit.DAYS.toMillis(1));
    }
}}