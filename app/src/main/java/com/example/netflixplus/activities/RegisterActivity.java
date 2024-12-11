/**
 * Activity handling user registration functionality for the Netflix copy application.
 * Manages user input validation, Firebase authentication, and navigation between screens.
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText editTextEmail, editTextPassword, editTextRepeatedPassword;
    private TextInputLayout emailLayout, passwordLayout, repeatPasswordLayout;
    private Button buttonRegister;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private TextView textViewLogin;

    private static final int MIN_PASSWORD_LENGTH = 6;

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
        setContentView(R.layout.activity_register);
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
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        repeatPasswordLayout = findViewById(R.id.repeat_password_layout);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextRepeatedPassword = findViewById(R.id.repeat_password);
        buttonRegister = findViewById(R.id.register_button);
        progressBar = findViewById(R.id.progressBar);
        textViewLogin = findViewById(R.id.loginNow);
    }


    /**
     * Sets up click listeners for interactive elements.
     */
    private void setupClickListeners() {
        textViewLogin.setOnClickListener(v -> navigateToLogin());
        buttonRegister.setOnClickListener(v -> handleRegistration());
    }


    /**
     * Handles the registration process including input validation and Firebase authentication.
     */
    private void handleRegistration() {
        toggleLoadingState(true);
        clearErrors();

        String email = String.valueOf(editTextEmail.getText());
        String password = String.valueOf(editTextPassword.getText());
        String repeatedPassword = String.valueOf(editTextRepeatedPassword.getText());

        if (!validateInputs(email, password, repeatedPassword)) {
            toggleLoadingState(false);
            return;
        }

        createFirebaseUser(email, password);
    }


    /**
     * Validates all user inputs for registration.
     */
    private boolean validateInputs(String email, String password, String repeatedPassword) {
        if (TextUtils.isEmpty(email)) {
            showError(emailLayout, editTextEmail, "Enter email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            showError(passwordLayout, null, "Enter password");
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError(passwordLayout, editTextPassword, "Minimum of 6 characters");
            return false;
        }

        if (!password.equals(repeatedPassword)) {
            showError(repeatPasswordLayout, editTextRepeatedPassword, "Passwords don't match");
            return false;
        }

        return true;
    }


    /**
     * Creates a new user account with Firebase Authentication.
     */
    private void createFirebaseUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this::handleRegistrationResult);
    }


    /**
     * Handles the result of the Firebase user creation attempt.
     */
    private void handleRegistrationResult(@NonNull Task<AuthResult> task) {
        toggleLoadingState(false);

        if (task.isSuccessful()) {
            Toast.makeText(RegisterActivity.this, "Account created.", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
            return;
        }

        handleRegistrationError(task.getException());
    }


    /**
     * Handles various types of registration errors.
     */
    private void handleRegistrationError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            showError(emailLayout, editTextEmail, "This email is already registered");
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            showError(emailLayout, editTextEmail, "Enter a valid email");
        } else {
            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Shows an error message in the specified layout and focuses the related view.
     */
    private void showError(TextInputLayout layout, View viewToFocus, String errorMessage) {
        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        if (viewToFocus != null) {
            viewToFocus.requestFocus();
        }
        layout.setErrorIconDrawable(null);
        layout.setError(errorMessage);
    }


    /**
     * Toggles the loading state of the UI.
     */
    private void toggleLoadingState(boolean isLoading) {
        buttonRegister.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }


    /**
     * Clears all error states from input layouts.
     */
    private void clearErrors() {
        emailLayout.setErrorEnabled(false);
        passwordLayout.setErrorEnabled(false);
        repeatPasswordLayout.setErrorEnabled(false);
    }


    /**
     * Navigates to the login screen.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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