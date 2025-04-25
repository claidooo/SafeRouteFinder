package com.cjsm.saferoutefinder;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class AccountSettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);


        // Initialize logout button as a local variable
        Button btnLogout = findViewById(R.id.btn_logout);

        // Set listener for the Logout button
        btnLogout.setOnClickListener(v -> performLogout());
    }
    // Show logout confirmation dialog


    // Actual logout logic
    private void performLogout() {
        // Navigate to LoginActivity and clear the back stack
        Intent intent = new Intent(AccountSettingsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();  // Finish current activity and all parent activities
    }
}
