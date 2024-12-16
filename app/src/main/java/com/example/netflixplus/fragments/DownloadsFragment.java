package com.example.netflixplus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.netflixplus.R;
import com.example.netflixplus.activities.VideoPlayerActivity;
import com.example.netflixplus.entities.MediaResponseDTO;
import com.example.netflixplus.utils.TorrentManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.text.WordUtils;

public class DownloadsFragment extends Fragment {
    private View rootView;
    private RecyclerView downloadsRecyclerView;
    private View emptyStateContainer;
    private DownloadAdapter downloadAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_download, container, false);
        setupViews();
        loadDownloads();
        return rootView;
    }


    private void setupViews() {
        downloadsRecyclerView = rootView.findViewById(R.id.downloads_recycler_view);
        emptyStateContainer = rootView.findViewById(R.id.empty_state_container);

        // Setup RecyclerView
        downloadsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        downloadAdapter = new DownloadAdapter(movie -> playMovie(movie));
        downloadsRecyclerView.setAdapter(downloadAdapter);
    }


    /**
     * Load all downloaded moview
     */
    private void loadDownloads() {
        // Get downloaded movies from local storage
        List<MediaResponseDTO> downloads = getDownloadedMovies();
        updateUI(downloads);
    }


    private List<MediaResponseDTO> getDownloadedMovies() {
        List<MediaResponseDTO> downloadedMovies = new ArrayList<>();

        // Get the downloads directory
        File downloadDir = TorrentManager.getInstance(new File(getContext().getFilesDir(), "Downloads")).getOutputDirectory();
        System.out.println("dir" + downloadDir);
        if (downloadDir.exists() && downloadDir.isDirectory()) {
            // List all MP4 files
            System.out.println("Checking download Folder");
            File[] files = downloadDir.listFiles();

            if (files != null) {
                for (File folder : files) {
                    // Create a MediaResponseDTO for each file
                    System.out.println(folder);
                    File[] movies = folder.listFiles();
                    if (movies != null){
                        for (File movie : movies) {
                            System.out.println(movie.toString());
                            MediaResponseDTO media = new MediaResponseDTO();
                            media.setTitle(formatName(folder.getName(),movie.getName()));
                            media.setFilePath(movie.getAbsolutePath());
                            downloadedMovies.add(media);
                        }
                    }
                }
            }
        }

        return downloadedMovies;
    }

    private String formatName(String name, String name1) {
        String input = name.replace('_', ' ');
        return WordUtils.capitalizeFully(input)  +" "+ name1.substring(0,2);
    }


    private void updateUI(List<MediaResponseDTO> downloads) {
        if (downloads.isEmpty()) {
            showEmptyState();
        } else {
            showDownloads(downloads);
        }
    }


    private void showEmptyState() {
        downloadsRecyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
    }


    private void showDownloads(List<MediaResponseDTO> downloads) {
        emptyStateContainer.setVisibility(View.GONE);
        downloadsRecyclerView.setVisibility(View.VISIBLE);
        downloadAdapter.setDownloads(downloads);
    }


    private void playMovie(MediaResponseDTO movie) {
        File movieFile = new File(movie.getFilePath());
        if (!movieFile.exists()) {
            Toast.makeText(requireContext(), "Error: Movie file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), VideoPlayerActivity.class);
        intent.putExtra("videoPath", movie.getFilePath());
        intent.putExtra("title", movie.getTitle());
        startActivity(intent);
    }


    private static class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {
        private List<MediaResponseDTO> downloads = new ArrayList<>();
        private final OnMovieClickListener listener;

        interface OnMovieClickListener {
            void onMovieClick(MediaResponseDTO movie);
        }

        DownloadAdapter(OnMovieClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Using a simple TextView for the list item
            TextView textView = new TextView(parent.getContext());
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setPadding(32, 24, 32, 24); // Add some padding
            textView.setTextSize(16); // Set text size
            return new DownloadViewHolder(textView);
        }


        @Override
        public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
            MediaResponseDTO media = downloads.get(position);
            holder.bind(media, listener);
        }


        @Override
        public int getItemCount() {
            return downloads.size();
        }


        public void setDownloads(List<MediaResponseDTO> downloads) {
            this.downloads = downloads;
            notifyDataSetChanged();
        }


        static class DownloadViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleView;

            public DownloadViewHolder(@NonNull View itemView) {
                super(itemView);
                titleView = (TextView) itemView;
            }

            public void bind(MediaResponseDTO media, OnMovieClickListener listener) {
                titleView.setText(media.getTitle());
                titleView.setOnClickListener(v -> listener.onMovieClick(media));
            }
        }
    }
}