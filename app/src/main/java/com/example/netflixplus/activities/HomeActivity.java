package com.example.netflixplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.netflixplus.R;
import com.example.netflixplus.fragments.HomeFragment;
import com.example.netflixplus.fragments.SearchFragment;
import com.example.netflixplus.fragments.DownloadsFragment;
import com.example.netflixplus.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * HomeActivity serves as the main container for app navigation.
 * It manages multiple fragments through a bottom navigation bar.
 */
public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        initializeUI();
        checkAuthenticationState();
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment()); // Load default fragment
        }
    }


    /**
     * Initializes the UI components and sets up navigation.
     */
    private void initializeUI() {
        System.out.println("\n\n\n\n\n\n\n\n Entrei no HomeActivity \n\n\n\n\n\n\n\n\n\n");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        setupWindowInsets();

        auth = FirebaseAuth.getInstance();
        setupBottomNavigation();
    }


    /**
     * Sets up the bottom navigation with its icons and click listeners.
     */
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_search) {
                loadFragment(new SearchFragment());
                return true;
            } else if (itemId == R.id.nav_downloads) {
                loadFragment(new DownloadsFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }


    /**
     * Loads a fragment into the container.
     * @param fragment The fragment to load
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }


    /**
     * Sets up window insets for edge-to-edge display.
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    /**
     * Checks if user is authenticated, redirects to login if not.
     */
    private void checkAuthenticationState() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        }
    }


    /**
     * Navigates to the login screen and finishes current activity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
}