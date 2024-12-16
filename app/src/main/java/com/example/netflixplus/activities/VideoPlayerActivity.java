package com.example.netflixplus.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;

import com.example.netflixplus.R;
import com.example.netflixplus.retrofitAPI.RetrofitClient;
import com.example.netflixplus.retrofitAPI.RetrofitNetworkConfig;
import com.example.netflixplus.utils.MovieProgressManager;

public class VideoPlayerActivity extends AppCompatActivity {
    private ExoPlayer player;
    private MovieProgressManager progressManager;
    private final String authToken = RetrofitClient.getIdToken();
    private String id;
    private PlayerView playerView;
    private static final int PROGRESS_UPDATE_INTERVAL = 1000; // 1 second in milliseconds
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                int currentMiliseconds = (int) (player.getCurrentPosition());
                progressManager.saveProgress(id, currentMiliseconds);
            }
            progressHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_player);

        progressManager = new MovieProgressManager(this);


        // Store PlayerView as class member
        playerView = findViewById(R.id.videoPlayer);
        boolean isHighQuality = getIntent().getBooleanExtra("isHighQuality", true);
        id = getIntent().getStringExtra("id");
        String title = getIntent().getStringExtra("title");
        if (id != null) {
            Log.d("Player", "id not null");
            int savedMinutes = progressManager.getProgress(id);
            playVideo(savedMinutes);
        } else {
            Log.d("Player", "No ID found");
            initializePlayer(getMediaItem(), 0);
        }
    }

    private MediaItem getMediaItem(){
        boolean isHighQuality = getIntent().getBooleanExtra("isHighQuality", true);
        String title = getIntent().getStringExtra("title");
        String path = getIntent().getStringExtra("videoPath");
        if (path != null){
            System.out.println("Getting movie from file");
            return getMediaItemFromFile(path);
        }else{
            System.out.println("Streaming movie");
            return getMediaItemFromWeb(title, isHighQuality);
        }
    }

    private MediaItem getMediaItemFromFile(String path) {
        String uri = "file://" + path;
        Log.d("Player Url", uri);
        MediaItem item = MediaItem.fromUri(uri);
        player = new ExoPlayer.Builder(this).build();
        return item;
    }
//        boolean isHighQuality = getIntent().getBooleanExtra("isHighQuality", true);
//        id = getIntent().getStringExtra("id");
//        String title = getIntent().getStringExtra("title");
//       if (id != null){
//           int savedMinutes = progressManager.getProgress(id);
//            } if (savedMinutes > 0) {
//                showResumeDialog(savedMinutes);
//       } else {
//           mediaItemFromweb()
//       }

    private void playVideo(int savedMinutes) {
        long milliseconds = savedMinutes;
        // Release existing player if any
        releasePlayer();
        initializePlayer(getMediaItem(),
                milliseconds);

        startProgressTracking();
    }

    private void showResumeDialog(int savedMinutes) {
        new AlertDialog.Builder(this)
                .setTitle("Resume Playback")
                .setMessage("Would you like to resume from " + savedMinutes / 1000 + " seconds?")
                .setPositiveButton("Resume", (dialog, which) -> {
                    playVideo(savedMinutes);
                })
                .setNegativeButton("Start Over", (dialog, which) -> {
                    // Release existing player if any
                    playVideo(0);
                })
                .show();
    }

    private void releasePlayer() {
        if (player != null) {
            stopProgressTracking();
            player.release();
            player = null;
        }
    }

    private void startProgressTracking() {
        progressHandler.postDelayed(progressUpdateRunnable, PROGRESS_UPDATE_INTERVAL);
    }

    private void stopProgressTracking() {
        progressHandler.removeCallbacks(progressUpdateRunnable);
    }

    @OptIn(markerClass = UnstableApi.class)
    private MediaItem getMediaItemFromWeb(String title, boolean isHighQuality){
        String filename = title.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();

        HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true);

        DataSource.Factory dataSourceFactory = () -> {
            HttpDataSource dataSource = httpDataSourceFactory.createDataSource();
            dataSource.setRequestProperty("Authorization", "Bearer " + authToken);
            return dataSource;
        };

        Context context = getApplicationContext();
        player = new ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                        new DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory))
                .build();

        String quality = isHighQuality ? "HD_HLS" : "LD_HLS";
        String selectedUrl = RetrofitNetworkConfig.BASE_URL + "movies/" +
                filename +
                "/" +
                quality +
                "/output.m3u8";
        System.out.println("Url " + selectedUrl);
        MediaItem item = MediaItem.fromUri(selectedUrl);
        System.out.println(item);
        return item;
    }

    private void initializePlayer(MediaItem item, long startPosition) {
        System.out.println(item);
        player.addMediaItem(item);
        player.prepare();
        player.seekTo(startPosition);
        playerView.setPlayer(player);
        player.play();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    progressManager.clearProgress(id);
                    stopProgressTracking();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && id != null) {
            int currentMinutes = (int) (player.getCurrentPosition());
            progressManager.saveProgress(id, currentMinutes);
        }
        player.pause();
        stopProgressTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            startProgressTracking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            if (id != null) {
                int currentMinutes = (int) (player.getCurrentPosition());
                progressManager.saveProgress(id, currentMinutes);
            }
            releasePlayer();
        }
    }
}