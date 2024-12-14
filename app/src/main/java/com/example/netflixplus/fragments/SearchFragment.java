package com.example.netflixplus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.netflixplus.R;
import com.example.netflixplus.activities.MovieDetailsActivity;
import com.example.netflixplus.entities.MediaResponseDTO;
import com.example.netflixplus.retrofitAPI.RetrofitClient;
import com.example.netflixplus.utils.MovieAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment implements MovieAdapter.OnMovieClickListener {
    private View rootView;
    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private View emptyStateContainer;
    private MovieAdapter searchAdapter;
    private List<MediaResponseDTO> allMovies = new ArrayList<>();
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final PublishSubject<String> searchSubject = PublishSubject.create();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        setupViews();
        setupSearch();
        loadAllMovies();
        return rootView;
    }


    private void setupViews() {
        searchEditText = rootView.findViewById(R.id.search_edit_text);
        searchResultsRecyclerView = rootView.findViewById(R.id.search_results_recycler_view);
        emptyStateContainer = rootView.findViewById(R.id.empty_state_container);

        // Setup RecyclerView with Grid Layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        searchResultsRecyclerView.setLayoutManager(gridLayoutManager);
        searchAdapter = new MovieAdapter(this);
        searchResultsRecyclerView.setAdapter(searchAdapter);

        // Add this line to setup the spinner
        setupGenreSpinner();
    }


    /**
     * Setuo search bar view
     */
    private void setupSearch() {
        // Setup RxJava search with debounce
        disposables.add(searchSubject
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(query -> performSearch(query),
                        throwable -> showError("Search error: " + throwable.getMessage())));

        // Setup EditText listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchSubject.onNext(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupGenreSpinner() {
        Spinner spinner = rootView.findViewById(R.id.movies_genre_list);

        // Create list of genres
        List<String> genres = Arrays.asList("All", "Drama", "Animation", "Fiction", "Action", "Comedy", "Fantasy", "Horror");

        // Create and set the adapter
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                genres) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                // Make the selected item view empty/invisible
                TextView textView = (TextView) view;
                textView.setText("");
                return view;
            }
        };

        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the spinner
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGenre = parent.getItemAtPosition(position).toString();
                if (!selectedGenre.equals("All")) {
                    loadMoviesByGenre(selectedGenre);
                } else {
                    loadAllMovies();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadAllMovies();
            }
        });
    }

    private void loadMoviesByGenre(String genre) {
        RetrofitClient.getInstance()
                .getApi()
                .getMediaByGenre(genre)
                .enqueue(new Callback<List<MediaResponseDTO>>() {
                    @Override
                    public void onResponse(Call<List<MediaResponseDTO>> call, Response<List<MediaResponseDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateSearchResults(response.body());
                        } else {
                            showError("Failed to load movies for genre: " + genre);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MediaResponseDTO>> call, Throwable t) {
                        showError("Network Error: " + t.getMessage());
                    }
                });
    }


    /**
     * Trigger get all movies HTTP request to load movies on searching
     */
    private void loadAllMovies() {
        RetrofitClient.getInstance()
                .getApi()
                .getAllMedia()
                .enqueue(new Callback<List<MediaResponseDTO>>() {
                    @Override
                    public void onResponse(Call<List<MediaResponseDTO>> call, Response<List<MediaResponseDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            allMovies = response.body();
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
     * Make search of movie that contains the query input in the title name.
     */
    private void performSearch(String query) {
        if (query.isEmpty()) {
            showEmptyState();
            return;
        }

        List<MediaResponseDTO> searchResults = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase();

        // Search in title and genre
        for (MediaResponseDTO movie : allMovies) {
            if (movie.getTitle().toLowerCase().contains(lowercaseQuery) ||
                    (movie.getGenre() != null && movie.getGenre().toLowerCase().contains(lowercaseQuery))) {
                searchResults.add(movie);
            }
        }

        updateSearchResults(searchResults);
    }


    /**
     * Update search when the user is typing the query on search bar
     */
    private void updateSearchResults(List<MediaResponseDTO> results) {
        if (results.isEmpty()) {
            showEmptyState();
        } else {
            showResults();
            searchAdapter.setMovies(results);
        }
    }


    /**
     * Open movie details card on movie card click
     */
    private void showResults() {
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
    }


    /**
     * Open movie details card on movie card click
     */
    private void showEmptyState() {
        searchResultsRecyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
    }


    /**
     * Open movie details card on movie card click
     */
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Open movie details card on movie card click
     */
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
    }
}