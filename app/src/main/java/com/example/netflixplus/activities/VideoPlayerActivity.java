package com.example.netflixplus.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.netflixplus.utils.MovieProgressManager;

public class VideoPlayerActivity extends AppCompatActivity {
    private ExoPlayer player;
    private MovieProgressManager progressManager;
    private final String authToken = RetrofitClient.getIdToken();
    private String id;
    private PlayerView playerView;
    private static final int PROGRESS_UPDATE_INTERVAL = 60000; // 1 minute in milliseconds
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                int currentMinutes = (int) (player.getCurrentPosition() / 1000 / 60);
                progressManager.saveProgress(id, currentMinutes);
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
        boolean isHighQuality = getIntent().getBooleanExtra("isHighQuality", true);
        id = getIntent().getStringExtra("id");
        String title = getIntent().getStringExtra("title");

        // Store PlayerView as class member
        playerView = findViewById(R.id.videoPlayer);

        if (id != null) {
            int savedMinutes = progressManager.getProgress(id);
            if (savedMinutes > 0) {
                showResumeDialog(savedMinutes);
            } else {
                initializePlayer(title, isHighQuality, 0);
                playerView.setPlayer(player);
                startProgressTracking();
            }
        }
    }

    private void showResumeDialog(int savedMinutes) {
        new AlertDialog.Builder(this)
                .setTitle("Resume Playback")
                .setMessage("Would you like to resume from " + savedMinutes + " minutes?")
                .setPositiveButton("Resume", (dialog, which) -> {
                    long milliseconds = savedMinutes * 60L * 1000L;
                    // Release existing player if any
                    releasePlayer();
                    initializePlayer(getIntent().getStringExtra("title"),
                            getIntent().getBooleanExtra("isHighQuality", true),
                            milliseconds);
                    playerView.setPlayer(player);
                    startProgressTracking();
                })
                .setNegativeButton("Start Over", (dialog, which) -> {
                    // Release existing player if any
                    releasePlayer();
                    initializePlayer(getIntent().getStringExtra("title"),
                            getIntent().getBooleanExtra("isHighQuality", true),
                            0);
                    playerView.setPlayer(player);
                    startProgressTracking();
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
    private void initializePlayer(String title, boolean isHighQuality, long startPosition) {
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
        String selectedUrl = "http://34.91.139.20:80/movies/" +
                filename +
                "/" +
                quality +
                "/output.m3u8";

        player.setMediaItem(MediaItem.fromUri(selectedUrl));
        player.prepare();
        player.seekTo(startPosition);
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
            int currentMinutes = (int) (player.getCurrentPosition() / 1000 / 60);
            progressManager.saveProgress(id, currentMinutes);
        }
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
                int currentMinutes = (int) (player.getCurrentPosition() / 1000 / 60);
                progressManager.saveProgress(id, currentMinutes);
            }
            releasePlayer();
        }
    }
}