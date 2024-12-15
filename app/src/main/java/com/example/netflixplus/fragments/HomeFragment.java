package com.example.netflixplus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.netflixplus.R;
import com.example.netflixplus.activities.MovieDetailsActivity;
import com.example.netflixplus.entities.MediaResponseDTO;
import com.example.netflixplus.retrofitAPI.RetrofitClient;
import com.example.netflixplus.utils.MovieAdapter;
import com.example.netflixplus.utils.ThumbnailLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements MovieAdapter.OnMovieClickListener {
    private View rootView;
    private MovieAdapter dramaAdapter;
    private MovieAdapter animationAdapter;
    private MovieAdapter fictionAdapter;
    private MovieAdapter horrorAdapter;
    private MovieAdapter comedyAdapter;
    private MovieAdapter actionAdapter;
    private MovieAdapter fantasyAdapter;

    private View dramaSection;
    private View animationSection;
    private View fictionSection;
    private View fantasySection;
    private View horrorSection;
    private View actionSection;
    private View comedySection;

    private ImageView featuredMovieImage;
    private TextView featuredMovieTitle;
    private MediaResponseDTO featuredMovie;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setupViews();
        loadMovies();
        return rootView;
    }


    private void setupViews() {
        featuredMovieImage = rootView.findViewById(R.id.featured_movie_image);
        featuredMovieTitle = rootView.findViewById(R.id.featured_movie_title);
        rootView.findViewById(R.id.play_button).setOnClickListener(v -> {
            if (featuredMovie != null) {
                onMovieClick(featuredMovie);
            }
        });

        // Initialize sections
        dramaSection = rootView.findViewById(R.id.drama_section);
        animationSection = rootView.findViewById(R.id.animation_section);
        fictionSection = rootView.findViewById(R.id.fiction_section);
        fantasySection = rootView.findViewById(R.id.fantasy_section);
        horrorSection = rootView.findViewById(R.id.horror_section);
        actionSection = rootView.findViewById(R.id.action_section);
        comedySection = rootView.findViewById(R.id.comedy_section);

        // Initialize adapters
        dramaAdapter = new MovieAdapter(this);
        animationAdapter = new MovieAdapter(this);
        fictionAdapter = new MovieAdapter(this);
        fantasyAdapter = new MovieAdapter(this);
        horrorAdapter = new MovieAdapter(this);
        actionAdapter = new MovieAdapter(this);
        comedyAdapter = new MovieAdapter(this);

        initializeRecyclerView(R.id.drama_recycler_view, dramaAdapter);
        initializeRecyclerView(R.id.animation_recycler_view, animationAdapter);
        initializeRecyclerView(R.id.fiction_recycler_view, fictionAdapter);
        initializeRecyclerView(R.id.fantasy_recycler_view, fantasyAdapter);
        initializeRecyclerView(R.id.horror_recycler_view, horrorAdapter);
        initializeRecyclerView(R.id.action_recycler_view, actionAdapter);
        initializeRecyclerView(R.id.comedy_recycler_view, comedyAdapter);
    }


    /**
     * Replicate the movie card layout to each item on get all movies response. Basically set the grid
     */
    private void initializeRecyclerView(int viewId, MovieAdapter adapter) {
        RecyclerView recyclerView = rootView.findViewById(viewId);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }


    /**
     * Load all movies in the home page
     */
    private void loadMovies() {
        RetrofitClient.getInstance()
                .getApi()
                .getAllMedia()
                .enqueue(new Callback<List<MediaResponseDTO>>() {
                    @Override
                    public void onResponse(Call<List<MediaResponseDTO>> call, Response<List<MediaResponseDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateUI(response.body());
                        } else {
                            showError("Failed to load movies");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MediaResponseDTO>> call, Throwable t) {
                        showError("Network Error: " + t.getMessage());
                    }
                });
    }


    /**
     * Setup the UI design with thumbnails on each genre section
     */
    private void updateUI(List<MediaResponseDTO> movies) {
        if (movies.isEmpty()) return;

        // Update featured movie
        featuredMovie = movies.get(0);
        featuredMovieTitle.setText(featuredMovie.getTitle());
        ThumbnailLoader.loadThumbnailWithGCS(featuredMovie.getBucketPaths().get("thumbnail"), featuredMovieImage);

        // Filter movies by genre
        HashMap<String, List<MediaResponseDTO>> moviesByGenre = new HashMap<>();

        for (MediaResponseDTO movie : movies) {
            String genre = movie.getGenre();
            if (genre != null) {
                moviesByGenre.computeIfAbsent(genre.toLowerCase(), k -> new ArrayList<>()).add(movie);
            }
        }

        // Update sections
        updateSection(dramaSection, dramaAdapter, moviesByGenre.get("drama"));
        updateSection(animationSection, animationAdapter, moviesByGenre.get("animation"));
        updateSection(fictionSection, fictionAdapter, moviesByGenre.get("fiction"));
        updateSection(actionSection, actionAdapter, moviesByGenre.get("action"));
        updateSection(comedySection, comedyAdapter, moviesByGenre.get("comedy"));
        updateSection(fantasySection, fantasyAdapter, moviesByGenre.get("fantasy"));
        updateSection(horrorSection, horrorAdapter, moviesByGenre.get("horror"));
    }


    /**
     * Setup the UI design with thumbnails that have the same genre
     */
    private void updateSection(View sectionView, MovieAdapter adapter, List<MediaResponseDTO> movies) {
        if (movies == null || movies.isEmpty()) {
            sectionView.setVisibility(View.GONE);
            adapter.setMovies(new ArrayList<>());
        } else {
            sectionView.setVisibility(View.VISIBLE);
            adapter.setMovies(movies);
        }
    }


    /**
     * Shows error messages if present
     */
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Handle movie click to access movie details card
     */
    @Override
    public void onMovieClick(MediaResponseDTO media) {
        Intent intent = new Intent(requireContext(), MovieDetailsActivity.class);
        intent.putExtra("title", media.getTitle());
        intent.putExtra("id", media.getId());
        intent.putExtra("description", media.getDescription());
        intent.putExtra("genre", media.getGenre());
        intent.putExtra("year", media.getYear());
        intent.putExtra("publisher", media.getPublisher());
        intent.putExtra("duration", media.getDuration());
        intent.putExtra("mediaUrls", new HashMap<>(media.getBucketPaths()));
        startActivity(intent);
    }
}