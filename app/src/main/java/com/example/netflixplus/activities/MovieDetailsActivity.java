// MovieDetailsActivity.java
package com.example.netflixplus.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.example.netflixplus.R;
import com.example.netflixplus.entities.MediaResponse;

public class MovieDetailsActivity extends AppCompatActivity {
    private PlayerView playerView;
    private ExoPlayer player;
    private ImageView thumbnailView;
    private TextView titleView, descriptionView, genreView, yearView, publisherView, durationView;

    // Store values as class fields
    private String title, description, genre, publisher, thumbnail;
    private int year, duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Get data from intent
        title = getIntent().getStringExtra("title");
        description = getIntent().getStringExtra("description");
        genre = getIntent().getStringExtra("genre");
        year = getIntent().getIntExtra("year", 0); // 0 as default value
        publisher = getIntent().getStringExtra("publisher");
        duration = getIntent().getIntExtra("duration", 0); // 0 as default value
        thumbnail = getIntent().getStringExtra("thumbnail");

        // Validate required data
        if (title == null || description == null) {
            Toast.makeText(this, "Error loading movie details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupPlayer();
        populateUI();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.view);
        thumbnailView = findViewById(R.id.movie_thumbnail);
        titleView = findViewById(R.id.movie_title);
        descriptionView = findViewById(R.id.movie_description);
        genreView = findViewById(R.id.movie_genre);
        yearView = findViewById(R.id.movie_year);
        publisherView = findViewById(R.id.movie_publisher);
        durationView = findViewById(R.id.movie_duration);

        findViewById(R.id.play_button).setOnClickListener(v -> startPlayback());
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupPlayer() {
        // Create a data source factory
        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();

        // Create HLS media source factory
        HlsMediaSource.Factory hlsMediaSourceFactory = new HlsMediaSource.Factory(httpDataSourceFactory);

        // Create ExoPlayer with the factories
        ExoPlayer.Builder builder = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(
                        new DefaultMediaSourceFactory(this)
                                .setDataSourceFactory(httpDataSourceFactory)
                );

        player = builder.build();
        playerView.setPlayer(player);

        // Create media source
        MediaSource mediaSource = hlsMediaSourceFactory
                .createMediaSource(MediaItem.fromUri("http://34.141.197.84/live/output.m3u8"));

        // Set the media source and prepare
        player.setMediaSource(mediaSource);
        player.prepare();
    }

    private void populateUI() {
        titleView.setText(title);
        descriptionView.setText(description);
        genreView.setText(genre);

        // Format year if it's not 0
        if (year != 0) {
            yearView.setText(String.valueOf(year));
        }

        if (publisher != null) {
            publisherView.setText(publisher);
        }

        // Format duration to show as "X min" if it's not 0
        if (duration != 0) {
            durationView.setText(String.format("%d min", duration));
        }

        if (thumbnail != null) {
            Glide.with(this)
                    .load(thumbnail)
                    .centerCrop()
                    .into(thumbnailView);
        }
    }

    private void startPlayback() {
        thumbnailView.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);
        player.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}