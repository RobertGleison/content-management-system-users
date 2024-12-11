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


    /**
     * Downloads a video file in a background thread
     */
    public static void downloadVideo(Context context, String urlString, String filename,
                                     DownloadProgressListener listener) {
        new Thread(() -> {
            try {
                File downloadDirectory = getDownloadDirectory(context);
                File outputFile = prepareOutputFile(downloadDirectory, filename);
                downloadFileWithProgress(urlString, outputFile, listener);
            } catch (IOException e) {
                Log.e(TAG, "Download failed", e);
                listener.onError("Download failed: " + e.getMessage());
            }
        }).start();
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
     * Performs the actual file download with progress tracking
     */
    private static void downloadFileWithProgress(String urlString, File outputFile,
                                                 DownloadProgressListener listener) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        long fileSize = httpConnection.getContentLengthLong();

        try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
             FileChannel fileChannel = fileOutputStream.getChannel()) {

            transferWithProgress(readableByteChannel, fileChannel, fileSize, listener);
            listener.onDownloadComplete(outputFile);
        }
    }


    /**
     * Transfers data in chunks while updating progress
     */
    private static void transferWithProgress(ReadableByteChannel readableByteChannel,
                                             FileChannel fileChannel, long fileSize,
                                             DownloadProgressListener listener) throws IOException {
        long transferred = 0;
        long chunk = 1024 * 1024; // 1MB chunks

        while (transferred < fileSize) {
            long count = fileChannel.transferFrom(readableByteChannel, transferred, chunk);
            if (count <= 0) break;
            transferred += count;

            int progress = (int) ((transferred * 100) / fileSize);
            listener.onProgressUpdate(progress);
        }
    }
}