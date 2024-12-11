package com.example.netflixplus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.netflixplus.R;
import com.example.netflixplus.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    private View rootView;
    private TextView profileName;
    private TextView profileEmail;
    private Button logoutButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        setupViews();
        loadUserProfile();
        return rootView;
    }


    private void setupViews() {
        profileName = rootView.findViewById(R.id.profile_name);
        profileEmail = rootView.findViewById(R.id.profile_email);
        logoutButton = rootView.findViewById(R.id.logout_button);

        // Setup click listeners
        logoutButton.setOnClickListener(v -> handleLogout());

        // Other menu items click listeners
        rootView.findViewById(R.id.membership_billing_button).setOnClickListener(v ->
                showFeatureNotAvailable("Membership & Billing"));

        rootView.findViewById(R.id.app_settings_button).setOnClickListener(v ->
                showFeatureNotAvailable("App Settings"));

        rootView.findViewById(R.id.account_button).setOnClickListener(v ->
                showFeatureNotAvailable("Account"));

        rootView.findViewById(R.id.help_button).setOnClickListener(v ->
                showFeatureNotAvailable("Help"));
    }


    /**
     *  Create mock user profile in profile page
     * */
    private void loadUserProfile() {
        // TODO: Load actual user data from your auth system
        // For now, we'll use placeholder data
        profileName.setText("John Doe");
        profileEmail.setText("john.doe@example.com");
    }


    /**
     * Go to login page after click on logout button
     * */
    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        navigateToLogin();
    }


    /**
     * Redirect to login Activity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        // Clear the back stack so user can't go back after logout
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish(); // Close the HomePageActivity
    }


    /**
     * Show message of feature coming soon after click on mock option buttons
     * */
    private void showFeatureNotAvailable(String featureName) {
        Toast.makeText(requireContext(),
                featureName + " feature coming soon",
                Toast.LENGTH_SHORT).show();
    }
}