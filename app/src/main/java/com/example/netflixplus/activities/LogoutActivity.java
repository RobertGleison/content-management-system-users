package com.example.netflixplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.netflixplus.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * LogoutActivity handles the user logout functionality and displays the current user's email.
 * It checks for authentication state and redirects to login if no user is authenticated.
 */
public class LogoutActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button buttonLogout;
    private TextView textViewEmail;
    private FirebaseUser user;


    /**
     * Initializes the activity, sets up UI components and handles authentication state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUI();
        checkAuthenticationState();
        setupClickListeners();
    }


    /**
     * Initializes UI components and sets up edge-to-edge display.
     */
    private void initializeUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logout);
        setupWindowInsets();

        auth = FirebaseAuth.getInstance();
        buttonLogout = findViewById(R.id.logout_button);
        textViewEmail = findViewById(R.id.email_user);
        user = auth.getCurrentUser();
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
     * Checks the user's authentication state and updates UI or redirects accordingly.
     */
    private void checkAuthenticationState() {
        if (user == null) {
            navigateToLogin();
        } else {
            updateUserInterface();
        }
    }


    /**
     * Updates the UI with the current user's email.
     */
    private void updateUserInterface() {
        textViewEmail.setText(user.getEmail());
    }


    /**
     * Sets up click listeners for interactive elements.
     */
    private void setupClickListeners() {
        buttonLogout.setOnClickListener(v -> handleLogout());
    }


    /**
     * Handles the logout process when the logout button is clicked.
     */
    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        navigateToLogin();
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