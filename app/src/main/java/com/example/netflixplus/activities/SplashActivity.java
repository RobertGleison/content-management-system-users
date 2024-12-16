package com.example.netflixplus.activities;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.netflixplus.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3500;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // If no user logged in, proceed with normal splash screen
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Configure window for full-screen display
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setupSplashImage();
        scheduleLoginTransition();
    }


    /**
     * Checks if there is a current Firebase user
     * @return true if user is logged in, false otherwise
     */
    private boolean isUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }


    /**
     * Navigates directly to the main screen, bypassing splash and login
     */
    private void navigateToMainScreen() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    /**
     * Sets up the splash image view and starts animation if the drawable is animatable.
     */
    private void setupSplashImage() {
        ImageView splash = findViewById(R.id.splash_image);
        splash.setImageResource(R.drawable.splash_art);
        getWindow().getDecorView().setBackgroundColor(
                ContextCompat.getColor(this, R.color.background_splash)
        );

        Drawable drawable = splash.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }


    /**
     * Schedules the transition to the login screen after SPLASH_TIME milliseconds.
     * Uses Handler to post a delayed runnable that starts the LoginActivity
     * and finishes the current activity.
     */
    private void scheduleLoginTransition() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME);
    }
}