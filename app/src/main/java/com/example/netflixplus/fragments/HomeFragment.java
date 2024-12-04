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

    private View trendingSection;
    private View popularSection;
    private View dramaSection;
    private View animationSection;
    private View fictionSection;
    private View fantasySection;
    private View horrorSection;
    private View actionSection;
    private View comedySection;

    private RecyclerView trendingRecyclerView;
    private RecyclerView popularRecyclerView;
    private RecyclerView dramaRecyclerView;
    private RecyclerView animationRecyclerView;
    private RecyclerView fictionRecyclerView;
    private RecyclerView fantasyRecyclerView;
    private RecyclerView horrorRecyclerView;
    private RecyclerView actionRecyclerView;
    private RecyclerView comedyRecyclerView;

    private ImageView featuredMovieImage;
    private TextView featuredMovieTitle;
    private MediaResponseDTO featuredMovie;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        System.out.println("Estou no Home Fragment");
        setupViews();
        System.out.println("Sai do setup views home fragment");
        loadMovies();
        System.out.println("loadei os filmes");
        return rootView;
    }

    private void setupViews() {
        // Initialize featured movie views
        featuredMovieImage = rootView.findViewById(R.id.featured_movie_image);
        featuredMovieTitle = rootView.findViewById(R.id.featured_movie_title);
        rootView.findViewById(R.id.play_button).setOnClickListener(v -> {
            if (featuredMovie != null) {
                onMovieClick(featuredMovie);
            }
        });

        // Initialize sections
        trendingSection = rootView.findViewById(R.id.trending_section);
        popularSection = rootView.findViewById(R.id.popular_section);
        dramaSection = rootView.findViewById(R.id.drama_section);
        animationSection = rootView.findViewById(R.id.animation_section);
        fictionSection = rootView.findViewById(R.id.fiction_section);
        fantasySection = rootView.findViewById(R.id.fantasy_section);
        horrorSection = rootView.findViewById(R.id.horror_section);
        actionSection = rootView.findViewById(R.id.action_section);
        comedySection = rootView.findViewById(R.id.comedy_section);

        initializeRecyclerView(R.id.trending_recycler_view, trendingAdapter = new MovieAdapter(this));
        initializeRecyclerView(R.id.popular_recycler_view, popularAdapter = new MovieAdapter(this));
        initializeRecyclerView(R.id.drama_recycler_view, dramaAdapter = new MovieAdapter(this));
        initializeRecyclerView(R.id.animation_recycler_view, animationAdapter = new MovieAdapter(this));
        initializeRecyclerView(R.id.fiction_recycler_view, fictionAdapter = new MovieAdapter(this));
        initializeRecyclerView(R.id.fantasy_recycler_view, fantasyAdapter = new MovieAdapter(this));
        initializeRecyclerView(R.id.horror_recycler_view, horrorAdapter = new MovieAdapter(this));
        initializeRecyclerView(R.id.action_recycler_view, actionAdapter = new MovieAdapter(this));
        initializeRecyclerView(R.id.comedy_recycler_view, comedyAdapter = new MovieAdapter(this));
    }

    private void initializeRecyclerView(int viewId, MovieAdapter adapter) {
        RecyclerView recyclerView = rootView.findViewById(viewId);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
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
        featuredMovie = movies.get(0);
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
                switch (genre.toLowerCase()) {
                    case "drama":
                        dramaMovies.add(movie);
                        break;
                    case "animation":
                        animationMovies.add(movie);
                        break;
                    case "fiction":
                        fictionMovies.add(movie);
                        break;
                    case "action":
                        actionMovies.add(movie);
                        break;
                    case "comedy":
                        comedyMovies.add(movie);
                        break;
                    case "fantasy":
                        fantasyMovies.add(movie);
                        break;
                    case "horror":
                        horrorMovies.add(movie);
                        break;
                }
            }

            if (trendingMovies.size() < movies.size() / 2) {
                trendingMovies.add(movie);
            } else {
                popularMovies.add(movie);
            }
        }

        // Update sections visibility and content
        updateSection(dramaSection, dramaAdapter, dramaMovies);
        updateSection(animationSection, animationAdapter, animationMovies);
        updateSection(fictionSection, fictionAdapter, fictionMovies);
        updateSection(actionSection, actionAdapter, actionMovies);
        updateSection(comedySection, comedyAdapter, comedyMovies);
        updateSection(fantasySection, fantasyAdapter, fantasyMovies);
        updateSection(horrorSection, horrorAdapter, horrorMovies);
        updateSection(trendingSection, trendingAdapter, trendingMovies);
        updateSection(popularSection, popularAdapter, popularMovies);
    }

    private void updateSection(View sectionView, MovieAdapter adapter, List<MediaResponseDTO> movies) {
        if (movies == null || movies.isEmpty()) {
            if (sectionView != null) {
                ViewGroup.LayoutParams params = sectionView.getLayoutParams();
                params.height = 0;  // Set height to 0
                sectionView.setLayoutParams(params);
                sectionView.setVisibility(View.GONE);  // Also set visibility to GONE
                sectionView.setPadding(0, 0, 0, 0);  // Remove any padding
            }
            adapter.setMovies(new ArrayList<>()); // Clear the adapter
        } else {
            if (sectionView != null) {
                ViewGroup.LayoutParams params = sectionView.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                sectionView.setLayoutParams(params);
                sectionView.setVisibility(View.VISIBLE);
                sectionView.setPadding(16, 16, 16, 16);  // Direct padding values in dp
                adapter.setMovies(movies);
            }
        }
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
        System.out.println("TESTE1: " + media.getBucketPaths().get("thumbnail"));
        System.out.println("TESTE2: " + media.getBucketPaths().get("HD_HLS"));
        System.out.println("TESTE3: " + media.getBucketPaths().get("LD_HLS"));
        System.out.println("TESTE4: " + media.getBucketPaths().get("HD_default"));
        System.out.println("TESTE5: " + media.getBucketPaths().get("HD_default"));
        System.out.println("description: " + media.getDescription());
        System.out.println("genre: " + media.getGenre());
        System.out.println("title: " + media.getTitle());
        System.out.println("year: " + media.getYear());
        System.out.println("publisher: " + media.getPublisher());
        System.out.println("duration: " + media.getDuration());
        startActivity(intent);
    }
}