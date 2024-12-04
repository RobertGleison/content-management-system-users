package com.example.netflixplus.activities;


import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.netflixplus.R;
import com.example.netflixplus.utils.ImageLoader;
import com.example.netflixplus.utils.VideoDownloader;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MovieDetailsActivity extends AppCompatActivity {
    private ImageView thumbnailView;
    private TextView titleView, descriptionView, genreView, yearView, publisherView, durationView;
    private MaterialButtonToggleGroup qualityToggleGroup;
    private boolean isHighQuality = true; // Default to high quality
    private ProgressBar downloadProgress;
    private Button downloadButton;
    private TextView downloadProgressText;

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
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        extractIntentData(intent);

        // Validate required data
        if (title == null || description == null || mediaUrls == null) {
            Toast.makeText(this, "Error loading movie details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        setupQualityToggle();
        populateUI();
    }

    private void extractIntentData(Intent intent) {
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        genre = intent.getStringExtra("genre");
        year = intent.getIntExtra("year", 0);
        publisher = intent.getStringExtra("publisher");
        duration = intent.getIntExtra("duration", 0);
        @SuppressWarnings("unchecked")
        Map<String, String> urls = (Map<String, String>) intent.getSerializableExtra("mediaUrls");
        mediaUrls = urls;
        if (mediaUrls != null) {
            thumbnailUrl = mediaUrls.get("thumbnail");
        }
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
        downloadProgress = findViewById(R.id.download_progress);
        downloadButton = findViewById(R.id.download_button);
        downloadProgressText = findViewById(R.id.download_progress_text);

        // Initially hide progress indicators
//        downloadProgress.setVisibility(View.GONE);
//        downloadProgressText.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        findViewById(R.id.play_button).setOnClickListener(v -> startVideoPlayer());
        downloadButton.setOnClickListener(v -> startDownload());
    }

    private void startVideoPlayer() {
        Intent intent = new Intent(MovieDetailsActivity.this, VideoPlayerActivity.class);
        intent.putExtra("isHighQuality", isHighQuality);
        intent.putExtra("mediaUrls", new HashMap<>(mediaUrls));
        System.out.println("TESTE: " + mediaUrls.get("thumbnail"));
        System.out.println("TESTE: " + mediaUrls.get("HD_HLS"));
        System.out.println("TESTE: " + mediaUrls.get("LD_HLS"));
        System.out.println("TESTE: " + mediaUrls.get("HD_default"));
        System.out.println("TESTE: " + mediaUrls.get("HD_default"));
        startActivity(intent);
    }

    private void startDownload() {
        if (mediaUrls == null) {
            Toast.makeText(this, "Error: No media URLs available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable download button while downloading
        downloadButton.setEnabled(false);
        showProgressIndicators();

        String videoUrl = isHighQuality ? mediaUrls.get("HD_default") : mediaUrls.get("SD_default");
        if (videoUrl == null) {
            handleDownloadError("Invalid video URL");
            return;
        }

        String filename = createFilename();
        startVideoDownload(videoUrl, filename);
    }

    private void showProgressIndicators() {
        downloadProgress.setVisibility(View.VISIBLE);
        downloadProgressText.setVisibility(View.VISIBLE);
        downloadProgress.setProgress(0);
        downloadProgressText.setText(R.string.download_starting);
    }

    private String createFilename() {
        String sanitizedTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
        String quality = isHighQuality ? "HD" : "SD";
        return sanitizedTitle + "_" + quality + ".mp4";
    }

    private void startVideoDownload(String videoUrl, String filename) {
        VideoDownloader.downloadVideo(
                this,
                videoUrl,
                filename,
                new VideoDownloader.DownloadProgressListener() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        runOnUiThread(() -> updateDownloadProgress(progress));
                    }

                    @Override
                    public void onDownloadComplete(File downloadedFile) {
                        runOnUiThread(() -> handleDownloadSuccess(downloadedFile));
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> handleDownloadError(error));
                    }
                }
        );
    }

    private void updateDownloadProgress(int progress) {
        downloadProgress.setProgress(progress);
        downloadProgressText.setText(getString(R.string.download_progress, progress));
    }

    private void handleDownloadSuccess(File downloadedFile) {
        resetDownloadUI();
        Toast.makeText(this,
                getString(R.string.download_complete, downloadedFile.getName()),
                Toast.LENGTH_LONG).show();

        // Scan the file so it appears in gallery
        MediaScannerConnection.scanFile(
                this,
                new String[]{downloadedFile.getAbsolutePath()},
                null,
                null
        );
    }

    private void handleDownloadError(String error) {
        resetDownloadUI();
        Toast.makeText(this,
                getString(R.string.download_failed, error),
                Toast.LENGTH_LONG).show();
    }

    private void resetDownloadUI() {
        downloadButton.setEnabled(true);
        downloadProgress.setVisibility(View.GONE);
        downloadProgressText.setVisibility(View.GONE);
    }

    private void setupQualityToggle() {
        qualityToggleGroup.check(R.id.high_quality_button);

        qualityToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isHighQuality = checkedId == R.id.high_quality_button;
                int messageResId = isHighQuality ?
                        R.string.hd_quality_selected :
                        R.string.sd_quality_selected;
                Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI() {
        titleView.setText(title);
        descriptionView.setText(description);
        genreView.setText(genre);
        yearView.setText(String.valueOf(year));
        publisherView.setText(publisher);
        durationView.setText(getString(R.string.duration_format, duration));

        if (thumbnailUrl != null) {
            ImageLoader.loadMovieThumbnail(thumbnailUrl, thumbnailView);
        }
    }
}