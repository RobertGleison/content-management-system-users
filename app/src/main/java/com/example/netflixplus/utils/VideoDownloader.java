package com.example.netflixplus.utils;


import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class VideoDownloader {
    private static final String TAG = "VideoDownloader";

    public interface DownloadProgressListener {
        void onProgressUpdate(int progress);
        void onDownloadComplete(File downloadedFile);
        void onError(String error);
    }

    public static void downloadVideo(Context context, String urlString, String filename,
                                     DownloadProgressListener listener) {
        new Thread(() -> {
            try {
                // Choose the appropriate directory
                File downloadDirectory;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // Store in Movies directory
                    downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                    // For Android 10+ you might want to use this instead:
                    // downloadDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                } else {
                    // Fallback to internal storage
                    downloadDirectory = new File(context.getFilesDir(), "videos");
                }

                // Create directories if they don't exist
                if (!downloadDirectory.exists()) {
                    downloadDirectory.mkdirs();
                }

                // Create the output file
                File outputFile = new File(downloadDirectory, filename);

                // Download the file
                URL url = new URL(urlString);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                long fileSize = httpConnection.getContentLengthLong();

                try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                     FileChannel fileChannel = fileOutputStream.getChannel()) {

                    long transferred = 0;
                    long chunk = 1024 * 1024; // 1MB chunks

                    while (transferred < fileSize) {
                        long count = fileChannel.transferFrom(readableByteChannel, transferred, chunk);
                        if (count <= 0) break;
                        transferred += count;

                        // Update progress
                        int progress = (int) ((transferred * 100) / fileSize);
                        listener.onProgressUpdate(progress);
                    }

                    // Notify completion
                    listener.onDownloadComplete(outputFile);
                }

            } catch (IOException e) {
                Log.e(TAG, "Download failed", e);
                listener.onError("Download failed: " + e.getMessage());
            }
        }).start();
    }
}