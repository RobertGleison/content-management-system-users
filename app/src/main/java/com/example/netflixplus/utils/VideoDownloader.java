package com.example.netflixplus.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

public class VideoDownloader {
    private static final String TAG = "GCPVideoDownloader";
    private static final String BUCKET_NAME = "netflixplus-library-cc2024 "; // Replace with your bucket name

    public interface DownloadProgressListener {
        void onProgressUpdate(int progress);
        void onDownloadComplete(File downloadedFile);
        void onError(String error);
    }

    /**
     * Downloads a video file from GCP Storage in a background thread
     * @param context Android context
     * @param objectName The name of the file in GCP bucket
     * @param filename Local filename to save as
     * @param listener Progress listener
     */
    public static void downloadVideo(String credentialsPath, Context context,String objectName, String filename,
                                     DownloadProgressListener listener) {
        new Thread(() -> {
            try {
                // Initialize GCP Storage client with credentials path
                Storage storage = initializeStorage(credentialsPath);

                // Get download directory
                File downloadDirectory = getDownloadDirectory(context);
                File outputFile = prepareOutputFile(downloadDirectory, filename);

                // Download the file
                downloadFromGCP(storage, objectName, outputFile, listener);
            } catch (IOException e) {
                Log.e(TAG, "Download failed", e);
                listener.onError("Download failed: " + e.getMessage());
            }
        }).start();
    }


    /**
     * Initializes the GCP Storage client
     */
    private static Storage initializeStorage(String credentialsPath) throws IOException {
        // Load credentials from file path
        File credentialsFile = new File(credentialsPath);
        GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(credentialsFile.toPath()));

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    /**
     * Determines and creates (if necessary) the download directory
     */
    private static File getDownloadDirectory(Context context) {
        File downloadDirectory;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        } else {
            downloadDirectory = new File(context.getFilesDir(), "videos");
        }

        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs();
        }
        return downloadDirectory;
    }

    /**
     * Creates output file in the specified directory
     */
    private static File prepareOutputFile(File directory, String filename) {
        return new File(directory, filename);
    }

    /**
     * Downloads file from GCP Storage with progress tracking
     */
    private static void downloadFromGCP(Storage storage, String objectName, File outputFile,
                                        DownloadProgressListener listener) throws IOException {
        // Create BlobId
        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        Blob blob = storage.get(blobId);

        if (blob == null) {
            throw new IOException("File not found in GCP bucket: " + objectName);
        }

        // Get file size for progress calculation
        long fileSize = blob.getSize();

        // Create a temporary file to track progress
        File tempFile = new File(outputFile.getParent(), outputFile.getName() + ".tmp");

        try {
            // Download to temporary file
            blob.downloadTo(tempFile.toPath());

            // Now read from temp file and write to final destination with progress
            try (FileInputStream fis = new FileInputStream(tempFile);
                 FileOutputStream fos = new FileOutputStream(outputFile);
                 FileChannel readChannel = fis.getChannel();
                 FileChannel writeChannel = fos.getChannel()) {

                transferWithProgress(readChannel, writeChannel, fileSize, listener);
                listener.onDownloadComplete(outputFile);
            }
        } finally {
            // Clean up temp file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }


    /**
     * Transfers data in chunks while updating progress
     */
    private static void transferWithProgress(ReadableByteChannel readChannel,
                                             FileChannel writeChannel,
                                             long fileSize,
                                             DownloadProgressListener listener) throws IOException {
        long transferred = 0;
        long chunk = 1024 * 1024; // 1MB chunks

        while (transferred < fileSize) {
            long count = writeChannel.transferFrom(readChannel, transferred, chunk);
            if (count <= 0) break;
            transferred += count;

            int progress = (int) ((transferred * 100) / fileSize);
            listener.onProgressUpdate(progress);
        }
    }
}