package com.example.netflixplus.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import com.example.netflixplus.retrofitAPI.RetrofitClient;
import com.example.netflixplus.utils.TokenRefreshHandler;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * HomeActivity serves as the main container for app navigation.
 * It manages multiple fragments through a bottom navigation bar.
 */
public class HomeActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private TokenRefreshHandler tokenRefreshHandler;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // Setup token refresh
        tokenRefreshHandler = new TokenRefreshHandler(this);
        tokenRefreshHandler.startTokenRefresh();

        initializeUI();
        checkAuthenticationState();

        // Add this line to set up the token before loading the fragment
        setupAuthToken(() -> {
            if (savedInstanceState == null) {
                loadFragment(new HomeFragment()); // Load default fragment
            }
        });

        checkAndRequestPermissions();
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkTokenExpiration();
    }


    /**
     * Create an authentication token
     */
    public void setupAuthToken(Runnable onComplete) {
        if (user == null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
        }

        if (user != null) {
            user.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String token = task.getResult().getToken();
                            Log.d("MovieDebug", "Got new token from Firebase");
                            String settedtoken = RetrofitClient.setIdToken(token);
                            Log.d("MovieDebug", "Token is:" + settedtoken);

                            onComplete.run(); // Run the callback after token is set
                        } else {
                            Log.e("MovieDebug", "Failed to get token", task.getException());
                            handleInvalidToken();
                        }
                    });
        } else {
            Log.e("MovieDebug", "No current user found");
            navigateToLogin();
        }
    }


    /**
     * Check if the token expired
     */
    private void checkTokenExpiration() {
        if (user == null) return;

        user.getIdToken(false)
                .addOnSuccessListener(result -> {
                    if (result == null || result.getToken() == null) {
                        handleInvalidToken();
                        return;
                    }

                    long expirationTime = result.getExpirationTimestamp() * 1000;
                    if (System.currentTimeMillis() >= expirationTime) {
                        handleInvalidToken();
                    }
                })
                .addOnFailureListener(e -> handleInvalidToken());
    }


    /**
     * Handle invalid tokens. If the tokens is invalid, go to Login Page.
     */
    private void handleInvalidToken() {
        auth.signOut();
        navigateToLogin();
    }


    /**
     * Handles media permissions based on Android version.
     * Requests appropriate permissions for accessing media files.
     */
    private void checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            handleAndroid13Permissions();
        } else {
            handleLegacyPermissions();
        }
    }


    /**
     * Handles permissions for Android 13 (API 33) and above.
     */
    private void handleAndroid13Permissions() {
        if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_VIDEO
                    },
                    PERMISSION_REQUEST_CODE
            );
        }
    }


    /**
     * Handles permissions for Android versions below 13.
     */
    private void handleLegacyPermissions() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
            );
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
        user = auth.getCurrentUser();
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