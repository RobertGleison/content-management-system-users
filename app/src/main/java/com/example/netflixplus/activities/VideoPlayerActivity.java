
package com.example.netflixplus.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.netflixplus.R;

import java.util.Map;


/**
 * Activity for playing video content with position tracking and quality selection.
 * Handles video playback using ExoPlayer and maintains playback position persistence.
 */
public class VideoPlayerActivity extends AppCompatActivity {
    private ExoPlayer player;
    private static final String POSITION_PREFERENCE = "video_position";
    private static final String VIDEO_ID = "video_id";  // To identify different videos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_player);
        boolean isHighQuality = getIntent().getBooleanExtra("isHighQuality", true); // true as default
        String id = getIntent().getStringExtra("id"); // true as default
        String title = getIntent().getStringExtra("title");
        PlayerView playerView = findViewById(R.id.videoPlayer);

        // Get saved position
        long savedPosition = getSavedPosition(id);

        player = buildMoviePlayer(title, isHighQuality, savedPosition);
        playerView.setPlayer(player);
    }


    /**
     * Constructs and configures an ExoPlayer instance for video playback.
     */
    protected ExoPlayer buildMoviePlayer(String title, boolean isHighQuality, long startPosition) {
        String filename = title.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        player = new ExoPlayer.Builder(getApplicationContext()).build();
        String quality = isHighQuality ? "HD_HLS" : "LD_HLS";
        String selectedUrl = "http://netflixppup.duckdns.org/movies/" +
                              filename +
                              "/" +
                              quality +
                              "/output.m3u8";

        player.setMediaItem(MediaItem.fromUri(selectedUrl));

        player.prepare();
        player.seekTo(startPosition);  // Seek to saved position
        player.play();
        return player;
    }


    /**
     * Retrieves the saved playback position for a specific video.
     */
    private long getSavedPosition(String videoId) {
        return getSharedPreferences(POSITION_PREFERENCE, MODE_PRIVATE)
                .getLong(videoId, 0);
    }


    /**
     * Saves the current playback position for a specific video.
     */
    private void savePosition(String videoId, long position) {
        getSharedPreferences(POSITION_PREFERENCE, MODE_PRIVATE)
                .edit()
                .putLong(videoId, position)
                .apply();
    }


    /**
     * Handles activity pause by saving current playback position.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            savePosition("CharlieChaplin", player.getCurrentPosition());
        }
    }


    /**
     * Handles activity destruction by saving position and releasing player resources.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            savePosition("CharlieChaplin", player.getCurrentPosition());
            player.release();
            player = null;
        }
    }
}