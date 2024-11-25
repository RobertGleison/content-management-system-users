package com.example.netflixplus.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.netflixplus.R;
import com.example.netflixplus.entities.MediaResponse;

import java.util.ArrayList;
import java.util.List;

public class DownloadsFragment extends Fragment {
    private View rootView;
    private RecyclerView downloadsRecyclerView;
    private View emptyStateContainer;
    private DownloadAdapter downloadAdapter;
    private TextView storageInfoText;
    private Switch smartDownloadsSwitch;
    private Button findContentButton;

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
        storageInfoText = rootView.findViewById(R.id.storage_info);
        smartDownloadsSwitch = rootView.findViewById(R.id.smart_downloads_switch);
        findContentButton = rootView.findViewById(R.id.find_content_button);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        downloadsRecyclerView.setLayoutManager(layoutManager);
        downloadAdapter = new DownloadAdapter();
        downloadsRecyclerView.setAdapter(downloadAdapter);

        // Setup click listeners
        findContentButton.setOnClickListener(v -> {
            // Navigate to home or search fragment
            Toast.makeText(requireContext(), "Navigate to content", Toast.LENGTH_SHORT).show();
        });

        smartDownloadsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Smart Downloads enabled" : "Smart Downloads disabled";
            Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
        });

        rootView.findViewById(R.id.btn_settings).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Open download settings", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadDownloads() {
        // Simulate loading downloads - replace with actual local storage query
        List<MediaResponse> downloads = getDownloadedMovies();
        updateUI(downloads);
    }

    private List<MediaResponse> getDownloadedMovies() {
        // TODO: Replace with actual local storage query
        return new ArrayList<>(); // Return empty list for now
    }

    private void updateUI(List<MediaResponse> downloads) {
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

    private void showDownloads(List<MediaResponse> downloads) {
        emptyStateContainer.setVisibility(View.GONE);
        downloadsRecyclerView.setVisibility(View.VISIBLE);
        downloadAdapter.setDownloads(downloads);
    }

    private static class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {
        private List<MediaResponse> downloads = new ArrayList<>();

        @NonNull
        @Override
        public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_download, parent, false);
            return new DownloadViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
            MediaResponse media = downloads.get(position);
            holder.bind(media);
        }

        @Override
        public int getItemCount() {
            return downloads.size();
        }

        public void setDownloads(List<MediaResponse> downloads) {
            this.downloads = downloads;
            notifyDataSetChanged();
        }

        static class DownloadViewHolder extends RecyclerView.ViewHolder {
            private final ImageView thumbnail;
            private final TextView title;
            private final TextView info;
            private final ImageView moreOptions;
            private final ProgressBar downloadProgress;

            public DownloadViewHolder(@NonNull View itemView) {
                super(itemView);
                thumbnail = itemView.findViewById(R.id.movie_thumbnail);
                title = itemView.findViewById(R.id.movie_title);
                info = itemView.findViewById(R.id.movie_info);
                moreOptions = itemView.findViewById(R.id.more_options);
                downloadProgress = itemView.findViewById(R.id.download_progress);
            }

            public void bind(MediaResponse media) {
                title.setText(media.getTitle());
                info.setText(String.format("%s • %s", media.getGenre(), "1.2 GB"));

                Glide.with(itemView.getContext())
                        .load(media.getThumbnail())
                        .centerCrop()
                        .into(thumbnail);

                moreOptions.setOnClickListener(v -> {
                    // Show options popup (delete, share, etc.)
                    showOptionsPopup(v, media);
                });
            }

            private void showOptionsPopup(View anchor, MediaResponse media) {
                PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
                popup.getMenuInflater().inflate(R.menu.download_options_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_delete) {
                        // Handle delete
                        return true;
                    } else if (itemId == R.id.action_share) {
                        // Handle share
                        return true;
                    }
                    return false;
                });
                popup.show();
            }
        }
    }
}