package com.example.netflixplus.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.netflixplus.retrofitAPI.RetrofitNetworkConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TorrentDownloadService extends Service {

    private ExecutorService executorService;
    private Intent parentIntent;



    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Creating Service");
        executorService = Executors.newSingleThreadExecutor();
        // Initialize a background executor service

    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        // Extract data from the Intent
        parentIntent = intent;
        String title = intent.getStringExtra("title");
        String quality = intent.getStringExtra("quality");
        String accessToken = intent.getStringExtra("accessToken");
        System.out.println("Enter download service");

        // Run the download task in the background
        executorService.submit(() -> downloadTorrentFile(title, quality, accessToken));

        return START_NOT_STICKY; // Service will not restart if killed
    }

    //Title should be lowercase
    private void downloadTorrentFile(String title, String quality, String accessToken) {
        System.out.println("Inside Download File");
        byte[] torrent = null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        File dir = new File(this.getFilesDir(), "Downloads");
        TorrentManager torrentManager = TorrentManager.getInstance(dir);
        try {
            System.out.println("Entered Try Block");
            String fileUrl = String.format("%s/%s/%s/%s.torrent",
                    RetrofitNetworkConfig.BASE_URL, "movies", title, quality);
            System.out.println("torrentUrl: " + fileUrl);
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = new BufferedInputStream(connection.getInputStream());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    torrent = inputStream.readAllBytes();
                }
            } else {
                System.out.println("Failed to download file: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File outfile = null;
        // Handle the downloaded torrent
        if (torrent != null) {
            outfile = torrentManager.downloadTorrent(torrent, title, accessToken);
            System.out.println("exited download torrent");
        } else {
            System.out.println("Torrent file could not be obtained");
        }

        Intent intent = new Intent("com.example.TORRENT_DOWNLOAD_COMPLETE");
        if (outfile != null) {
            intent.putExtra("filePath", outfile.getAbsolutePath());
            intent.putExtra("id", parentIntent.getStringExtra("id"));
        } else {
            intent.putExtra("error", "Download failed");
        }
        System.out.println("Broadcasting Intent");
        sendBroadcast(intent);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This is not a bound Service
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
