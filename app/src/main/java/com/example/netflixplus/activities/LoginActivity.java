/**
 * Activity handling user login functionality for the Netflix copy application.
 * Manages user authentication, password reset, and navigation between screens.
 */
package com.example.netflixplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.netflixplus.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText editTextEmail, editTextPassword;
    private TextInputLayout emailLayout, passwordLayout;
    private Button buttonLogin;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private TextView textViewRegister, textViewForgotPassword;

    @Override
    public void onStart() {
        super.onStart();
        checkCurrentUser();
    }


    /**
     * Checks if a user is currently signed in and redirects to HomeActivity if true.
     */
    private void checkCurrentUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToHome();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();
        initializeFirebase();
        bindViews();
        setupClickListeners();
    }


    /**
     * Sets up the initial UI configuration including EdgeToEdge support.
     */
    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setupWindowInsets();
    }


    /**
     * Configures window insets for proper UI layout.
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    /**
     * Initializes Firebase Authentication instance.
     */
    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }


    /**
     * Binds all view references from the layout.
     */
    private void bindViews() {
        emailLayout = findViewById(R.id.email_layout_login);
        passwordLayout = findViewById(R.id.password_layout_login);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progressBar);
        textViewRegister = findViewById(R.id.registerNow);
        textViewForgotPassword = findViewById(R.id.forgotPassword);
    }


    /**
     * Sets up click listeners for interactive elements.
     */
    private void setupClickListeners() {
        textViewRegister.setOnClickListener(v -> navigateToRegister());
        textViewForgotPassword.setOnClickListener(v -> handleForgotPassword());
        buttonLogin.setOnClickListener(v -> handleLogin());
    }


    /**
     * Handles the login process including input validation and Firebase authentication.
     */
    private void handleLogin() {
        toggleLoadingState(true);
        clearErrors();

        String email = String.valueOf(editTextEmail.getText());
        String password = String.valueOf(editTextPassword.getText());

        if (!validateLoginInputs(email, password)) {
            toggleLoadingState(false);
            return;
        }

        authenticateUser(email, password);
    }


    /**
     * Validates login input fields.
     * @param email User's email address
     * @param password User's password
     * @return boolean indicating if all inputs are valid
     */
    private boolean validateLoginInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            showError(emailLayout, editTextEmail, "Enter email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            showError(passwordLayout, editTextPassword, "Enter password");
            return false;
        }

        return true;
    }


    /**
     * Handles the password reset process.
     */
    private void handleForgotPassword() {
        String email = String.valueOf(editTextEmail.getText());

        if (TextUtils.isEmpty(email)) {
            showError(emailLayout, editTextEmail, "Enter email");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        sendPasswordResetEmail(email);
    }


    /**
     * Sends password reset email using Firebase Authentication.
     * @param email User's email address
     */
    private void sendPasswordResetEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Authenticates user with Firebase using email and password.
     * @param email User's email address
     * @param password User's password
     */
    private void authenticateUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this::handleAuthenticationResult);
    }


    /**
     * Handles the result of the Firebase authentication attempt.
     * @param task Firebase authentication task
     */
    private void handleAuthenticationResult(@NonNull Task<AuthResult> task) {
        toggleLoadingState(false);

        if (task.isSuccessful()) {
            Toast.makeText(LoginActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
            navigateToHome();
            return;
        }

        handleAuthenticationError(task.getException());
    }


    /**
     * Handles various types of authentication errors.
     * @param exception The exception thrown during authentication
     */
    private void handleAuthenticationError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(LoginActivity.this, "Wrong credentials", Toast.LENGTH_LONG).show();
            editTextEmail.requestFocus();
        } else {
            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Shows an error message in the specified layout and focuses the related view.
     * @param layout The TextInputLayout to show the error in
     * @param viewToFocus The view to focus (can be null)
     * @param errorMessage The error message to display
     */
    private void showError(TextInputLayout layout, View viewToFocus, String errorMessage) {
        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        if (viewToFocus != null) {
            viewToFocus.requestFocus();
        }
        layout.setErrorIconDrawable(null);
        layout.setError(errorMessage);
    }


    /**
     * Toggles the loading state of the UI.
     * @param isLoading Whether the UI should show loading state
     */
    private void toggleLoadingState(boolean isLoading) {
        buttonLogin.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }


    /**
     * Clears all error states from input layouts.
     */
    private void clearErrors() {
        emailLayout.setErrorEnabled(false);
        passwordLayout.setErrorEnabled(false);
    }


    /**
     * Navigates to the registration screen.
     */
    private void navigateToRegister() {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
        finish();
    }


    /**
     * Navigates to the home screen.
     */
    private void navigateToHome() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }
}