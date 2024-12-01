// MovieDetailsActivity.java
package com.example.netflixplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;

import com.bumptech.glide.Glide;
import com.example.netflixplus.R;

import com.example.netflixplus.utils.ImageLoader;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.HashMap;
import java.util.Map;

public class MovieDetailsActivity extends AppCompatActivity {
    private ImageView thumbnailView;
    private TextView titleView, descriptionView, genreView, yearView, publisherView, durationView;
    private MaterialButtonToggleGroup qualityToggleGroup;
    private boolean isHighQuality = true; // Default to high quality

    // Store values as class fields
    private String title, description, genre, publisher, thumbnailUrl;
    private int year, duration;
    private String id;
    private Map<String, String> mediaUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Get data from intent
        id = getIntent().getStringExtra("id");
        title = getIntent().getStringExtra("title");
        description = getIntent().getStringExtra("description");
        genre = getIntent().getStringExtra("genre");
        year = getIntent().getIntExtra("year", 0); // 0 as default value
        publisher = getIntent().getStringExtra("publisher");
        duration = getIntent().getIntExtra("duration", 0); // 0 as default value
        mediaUrls = (Map<String, String>) getIntent().getSerializableExtra("mediaUrls");
        thumbnailUrl = mediaUrls.get("thumbnailUrl");

        // Validate required data
        if (title == null || description == null) {
            Toast.makeText(this, "Error loading movie details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupPlayer();
        populateUI();
        setupQualityToggle();
    }

    private void initializeViews() {
        thumbnailView = findViewById(R.id.movie_thumbnail);
        titleView = findViewById(R.id.movie_title);
        descriptionView = findViewById(R.id.movie_description);
        genreView = findViewById(R.id.movie_genre);
        yearView = findViewById(R.id.movie_year);
        publisherView = findViewById(R.id.movie_publisher);
        durationView = findViewById(R.id.movie_duration);
        qualityToggleGroup = findViewById(R.id.quality_toggle_group);

        findViewById(R.id.play_button).setOnClickListener(v -> {
            Intent intent = new Intent(MovieDetailsActivity.this, VideoPlayerActivity.class);
            // Pass the quality flag to VideoPlayerActivity
            intent.putExtra("isHighQuality", isHighQuality);
            intent.putExtra("id", id);
            intent.putExtra("mediaUrls", new HashMap<>(mediaUrls));
            startActivity(intent);
        });
    }

    private void setupQualityToggle() {
        // Set default selection to high quality
        qualityToggleGroup.check(R.id.high_quality_button);

        qualityToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.high_quality_button) {
                    isHighQuality = true;
                    Toast.makeText(this, "HD Quality Selected", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.low_quality_button) {
                    isHighQuality = false;
                    Toast.makeText(this, "SD Quality Selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupPlayer() {
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

        if (thumbnailUrl != null) {
            ImageLoader.loadMovieThumbnail(thumbnailUrl, thumbnailView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}