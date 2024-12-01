package com.example.netflixplus.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.netflixplus.R;
import com.example.netflixplus.activities.MovieDetailsActivity;
import com.example.netflixplus.entities.MediaResponseDTO;
import com.example.netflixplus.retrofitAPI.RetrofitClient;
import com.example.netflixplus.utils.ImageLoader;
import com.example.netflixplus.utils.MovieAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements MovieAdapter.OnMovieClickListener {
    private View rootView;
    private MovieAdapter trendingAdapter;
    private MovieAdapter popularAdapter;
    private MovieAdapter dramaAdapter;
    private MovieAdapter animationAdapter;
    private MovieAdapter fictionAdapter;
    private MovieAdapter horrorAdapter;
    private MovieAdapter comedyAdapter;
    private MovieAdapter actionAdapter;
    private MovieAdapter fantasyAdapter;

    private ImageView featuredMovieImage;
    private TextView featuredMovieTitle;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setupViews();
        loadMovies();
        return rootView;
    }


    private void setupViews() {
        // Initialize featured movie views
        featuredMovieImage = rootView.findViewById(R.id.featured_movie_image);
        featuredMovieTitle = rootView.findViewById(R.id.featured_movie_title);
        rootView.findViewById(R.id.play_button).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Play clicked", Toast.LENGTH_SHORT).show();
        });

        // Initialize Trending RecyclerView
        RecyclerView trendingRecyclerView = rootView.findViewById(R.id.trending_recycler_view);
        LinearLayoutManager trendingLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        trendingRecyclerView.setLayoutManager(trendingLayoutManager);
        trendingAdapter = new MovieAdapter(this);
        trendingRecyclerView.setAdapter(trendingAdapter);

        // Initialize Popular RecyclerView
        RecyclerView popularRecyclerView = rootView.findViewById(R.id.popular_recycler_view);
        LinearLayoutManager popularLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        popularRecyclerView.setLayoutManager(popularLayoutManager);
        popularAdapter = new MovieAdapter(this);
        popularRecyclerView.setAdapter(popularAdapter);

        // Initialize Drama RecyclerView
        RecyclerView dramaRecyclerView = rootView.findViewById(R.id.drama_recycler_view);
        LinearLayoutManager dramaLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        dramaRecyclerView.setLayoutManager(dramaLayoutManager);
        dramaAdapter = new MovieAdapter(this);
        dramaRecyclerView.setAdapter(dramaAdapter);

        // Initialize Animation RecyclerView
        RecyclerView animationRecyclerView = rootView.findViewById(R.id.animation_recycler_view);
        LinearLayoutManager animationLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        animationRecyclerView.setLayoutManager(animationLayoutManager);
        animationAdapter = new MovieAdapter(this);
        animationRecyclerView.setAdapter(animationAdapter);

        // Initialize Fiction RecyclerView
        RecyclerView fictionRecyclerView = rootView.findViewById(R.id.fiction_recycler_view);
        LinearLayoutManager fictionLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        fictionRecyclerView.setLayoutManager(fictionLayoutManager);
        fictionAdapter = new MovieAdapter(this);
        fictionRecyclerView.setAdapter(fictionAdapter);

        // Initialize Fantasy RecyclerView
        RecyclerView fantasyRecyclerView = rootView.findViewById(R.id.fantasy_recycler_view);
        LinearLayoutManager fantasyLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        fantasyRecyclerView.setLayoutManager(fantasyLayoutManager);
        fantasyAdapter = new MovieAdapter(this);
        fantasyRecyclerView.setAdapter(fantasyAdapter);

        // Initialize Horror RecyclerView
        RecyclerView horrorRecyclerView = rootView.findViewById(R.id.horror_recycler_view);
        LinearLayoutManager horrorLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        horrorRecyclerView.setLayoutManager(horrorLayoutManager);
        horrorAdapter = new MovieAdapter(this);
        horrorRecyclerView.setAdapter(horrorAdapter);

        // Initialize Action RecyclerView
        RecyclerView actionRecyclerView = rootView.findViewById(R.id.action_recycler_view);
        LinearLayoutManager actionLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        actionRecyclerView.setLayoutManager(actionLayoutManager);
        actionAdapter = new MovieAdapter(this);
        actionRecyclerView.setAdapter(actionAdapter);

        // Initialize Comedy RecyclerView
        RecyclerView comedyRecyclerView = rootView.findViewById(R.id.comedy_recycler_view);
        LinearLayoutManager comedyLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        comedyRecyclerView.setLayoutManager(comedyLayoutManager);
        comedyAdapter = new MovieAdapter(this);
        comedyRecyclerView.setAdapter(comedyAdapter);

    }


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
                            try {
                                String errorBody = response.errorBody() != null ?
                                        response.errorBody().string() : "Unknown error";
                                showError("Failed to load movies: " + errorBody);
                            } catch (Exception e) {
                                showError("Failed to load movies: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MediaResponseDTO>> call, Throwable t) {
                        showError("Network Error: " + t.getMessage());
                        t.printStackTrace();
                    }
                });
    }


    private void updateUI(List<MediaResponseDTO> movies) {
        if (movies.isEmpty()) return;

        // Update featured movie
        MediaResponseDTO featuredMovie = movies.get(0);
        featuredMovieTitle.setText(featuredMovie.getTitle());
        ImageLoader.loadMovieThumbnail(requireContext(), featuredMovie.getBucketPaths().get("thumbnail"), featuredMovieImage);

        // Filter movies by genre
        List<MediaResponseDTO> dramaMovies = new ArrayList<>();
        List<MediaResponseDTO> animationMovies = new ArrayList<>();
        List<MediaResponseDTO> fictionMovies = new ArrayList<>();
        List<MediaResponseDTO> actionMovies = new ArrayList<>();
        List<MediaResponseDTO> comedyMovies = new ArrayList<>();
        List<MediaResponseDTO> fantasyMovies = new ArrayList<>();
        List<MediaResponseDTO> horrorMovies = new ArrayList<>();
        List<MediaResponseDTO> trendingMovies = new ArrayList<>();
        List<MediaResponseDTO> popularMovies = new ArrayList<>();

        for (MediaResponseDTO movie : movies) {
            String genre = movie.getGenre();
            if (genre != null) {
                if (genre.equalsIgnoreCase("Drama")) {
                    dramaMovies.add(movie);
                } else if (genre.equalsIgnoreCase("Animation")) {
                    animationMovies.add(movie);
                } else if (genre.equalsIgnoreCase("Fiction")) {
                    fictionMovies.add(movie);
                } else if (genre.equalsIgnoreCase("Action")) {
                    actionMovies.add(movie);
                } else if (genre.equalsIgnoreCase("Comedy")) {
                    comedyMovies.add(movie);
                } else if (genre.equalsIgnoreCase("Fantasy")) {
                    fantasyMovies.add(movie);
                } else if (genre.equalsIgnoreCase("Horror")) {
                    horrorMovies.add(movie);
                }
            }

            if (trendingMovies.size() < movies.size() / 2) {
                trendingMovies.add(movie);
            } else {
                popularMovies.add(movie);
            }
        }


        // Update all adapters
        if (!dramaMovies.isEmpty()) dramaAdapter.setMovies(dramaMovies);
        if (!animationMovies.isEmpty()) animationAdapter.setMovies(animationMovies);
        if (!fictionMovies.isEmpty()) fictionAdapter.setMovies(fictionMovies);
        if (!actionMovies.isEmpty()) actionAdapter.setMovies(actionMovies);
        if (!comedyMovies.isEmpty()) comedyAdapter.setMovies(comedyMovies);
        if (!fantasyMovies.isEmpty()) fantasyAdapter.setMovies(fantasyMovies);
        if (!horrorMovies.isEmpty()) horrorAdapter.setMovies(horrorMovies);
        if (!trendingMovies.isEmpty()) trendingAdapter.setMovies(trendingMovies);
        if (!popularMovies.isEmpty()) popularAdapter.setMovies(popularMovies);
    }


    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onMovieClick(MediaResponseDTO media) {
        Intent intent = new Intent(requireContext(), MovieDetailsActivity.class);
        intent.putExtra("title", media.getTitle());
        intent.putExtra("description", media.getDescription());
        intent.putExtra("genre", media.getGenre());
        intent.putExtra("year", media.getYear());
        intent.putExtra("publisher", media.getPublisher());
        intent.putExtra("duration", media.getDuration());
        intent.putExtra("mediaUrls", new HashMap<>(media.getBucketPaths()));
        startActivity(intent);
    }
}