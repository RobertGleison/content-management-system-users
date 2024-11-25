package com.example.netflixplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.netflixplus.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * AuthenticationActivity serves as the entry point for user authentication in the application.
 * It handles the initial authentication state check and provides navigation to login and registration screens.
 */
public class AuthenticationActivity extends AppCompatActivity {
    private Button buttonLogin;
    private Button buttonRegister;
    private FirebaseAuth firebaseAuth;


    /**
     * Checks the user's authentication state when the activity starts.
     * If a user is already signed in, redirects them to the LogoutActivity.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (firebaseAuth != null) {
            checkAuthenticationState();
        }
    }


    /**
     * Initializes the activity, sets up the UI components and click listeners.
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initializeFirebase()) {
            return;
        }
        initializeUI();
        setupClickListeners();
    }


    /**
     * Initializes Firebase and handles any initialization errors.
     * @return boolean indicating if Firebase initialization was successful
     */
    private boolean initializeFirebase() {
        try {
            // Ensure Firebase is initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
            firebaseAuth = FirebaseAuth.getInstance();
            return true;
        } catch (Exception e) {
            Toast.makeText(this,
                    "Failed to initialize Firebase. Please restart the app.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish(); // Close the activity if Firebase initialization fails
            return false;
        }
    }


    /**
     * Initializes the UI components and sets up the edge-to-edge display.
     */
    private void initializeUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authentication);
        setupWindowInsets();
        bindViews();
    }


    /**
     * Binds view references from the layout.
     */
    private void bindViews() {
        buttonLogin = findViewById(R.id.loginNow);
        buttonRegister = findViewById(R.id.registerNow);
    }


    /**
     * Sets up the window insets for proper edge-to-edge display.
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    /**
     * Sets up click listeners for the login and register buttons.
     */
    private void setupClickListeners() {
        if (buttonRegister != null) {
            buttonRegister.setOnClickListener(view -> navigateToRegister());
        }
        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(view -> navigateToLogin());
        }
    }


    /**
     * Checks if a user is currently authenticated and redirects to LogoutActivity if true.
     */
    private void checkAuthenticationState() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
        }
    }


    /**
     * Navigates to the registration screen.
     */
    private void navigateToRegister() {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }


    /**
     * Navigates to the login screen.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }


    /**
     * Navigates to the logout screen and finishes current activity.
     */
    private void navigateToMain() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }
}