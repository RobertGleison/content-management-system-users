package com.example.netflixplus.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.netflixplus.R;
import com.example.netflixplus.entities.MediaResponseDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying movie thumbnails in a grid layout.
 * This adapter handles the creation and binding of movie view holders,
 * loading movie thumbnails using Glide, and managing click events.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<MediaResponseDTO> movies;
    private final OnMovieClickListener listener;
    private static final String PROJECT_ID = "netflixplus-438015";
    private static final String BUCKET_NAME = "netflixplus-library-cc2024";

    /**
     * Constructs a new MovieAdapter with a click listener.
     */
    public MovieAdapter(OnMovieClickListener listener) {
        this.listener = listener;
        this.movies = new ArrayList<>();
    }


    /**
     * Updates the adapter's movie list and refreshes the view.
     */
    public void setMovies(@NonNull List<MediaResponseDTO> movies) {
        this.movies = new ArrayList<>(movies); // Create defensive copy
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = createMovieCardView(parent);
        return new MovieViewHolder(view);
    }


    /**
     * Creates the movie card view by inflating the layout.
     */
    private View createMovieCardView(@NonNull ViewGroup parent) {
        return LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_card, parent, false);
    }


    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MediaResponseDTO movie = movies.get(position);
        loadMovieThumbnail(holder, movie);
        setupClickListener(holder, movie);
    }


    /**
     * Loads the movie thumbnail using Glide library.
     */
    private void loadMovieThumbnail(@NonNull MovieViewHolder holder, @NonNull MediaResponseDTO movie) {
        // Use the ImageLoader utility with the ImageView from the ViewHolder
        ThumbnailLoader.loadThumbnailWithGCS(movie.getBucketPaths().get("thumbnail"), holder.posterImage);
    }


    /**
     * Sets up the click listener for the movie item.
     */
    private void setupClickListener(@NonNull MovieViewHolder holder, @NonNull MediaResponseDTO movie) {
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });
    }


    @Override
    public int getItemCount() {
        return movies.size();
    }


    /**
     * Interface for handling movie click events.
     */
    public interface OnMovieClickListener {
        void onMovieClick(MediaResponseDTO movie);
    }


    /**
     * ViewHolder class for movie items.
     */
    static class MovieViewHolder extends RecyclerView.ViewHolder {
        private final ImageView posterImage;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.movie_poster);
        }
    }
}