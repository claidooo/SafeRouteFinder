package com.cjsm.saferoutefinder;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AccountSettingsActivity extends AppCompatActivity {
    private Button btnLogout;
    private Button btnBack; // Changed to Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        // Initialize buttons
        btnLogout = findViewById(R.id.btn_logout);
        btnBack = findViewById(R.id.btn_back); // Button instead of ImageButton

        // Set a listener for the Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Set a listener for the Logout button
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    // Show logout confirmation dialog
    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountSettingsActivity.this);
        builder.setTitle("Confirm Logout")
                .setMessage("Do you wish to log out?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Perform logout action
    private void performLogout() {
        // You can add any logout logic here (e.g., clearing session, authentication token)
        System.exit(0); // Close the AccountSettingsActivity to prevent the user from going back
    }

    // Override onBackPressed() to handle the back button press
    @Override
    public void onBackPressed() {
        // When the back button is pressed, just finish the activity (go back to MainActivity)
        Intent intent = new Intent(AccountSettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close the AccountSettingsActivity
    }
}
