
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
        Map<String,String> mediaUrls = (Map<String, String>) getIntent().getSerializableExtra("mediaUrls");

        PlayerView playerView = findViewById(R.id.videoPlayer);

        // Get saved position
        long savedPosition = getSavedPosition(id);

        player = buildMoviePlayer(mediaUrls, isHighQuality, savedPosition);
        playerView.setPlayer(player);
    }

    protected ExoPlayer buildMoviePlayer(Map<String, String> mediaUrls, boolean isHighQuality, long startPosition) {
        player = new ExoPlayer.Builder(getApplicationContext()).build();
        String selectedUrl = isHighQuality ? mediaUrls.get("HD_HLS") : mediaUrls.get("LD_HLS");

        player.setMediaItem(MediaItem.fromUri("selectedUrl"));

        player.prepare();
        player.seekTo(startPosition);  // Seek to saved position
        player.play();
        return player;
    }

    private long getSavedPosition(String videoId) {
        return getSharedPreferences(POSITION_PREFERENCE, MODE_PRIVATE)
                .getLong(videoId, 0);
    }

    private void savePosition(String videoId, long position) {
        getSharedPreferences(POSITION_PREFERENCE, MODE_PRIVATE)
                .edit()
                .putLong(videoId, position)
                .apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            savePosition("CharlieChaplin", player.getCurrentPosition());
        }
    }

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