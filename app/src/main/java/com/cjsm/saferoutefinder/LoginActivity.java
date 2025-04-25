package com.cjsm.saferoutefinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    // Arrays to store valid student numbers and passwords
    private String[] StudentNumbers = {
            "202213742", "202213624", "20221549", "202213509", "123"};

    private String[] Passwords = {
            "amagi", "carmz", "cloud", "ashe", "try"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize UI elements
        EditText studentNumberInput = findViewById(R.id.studentNumberInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String studentNumber = studentNumberInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Validate input fields
            if (studentNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Check if the entered student number exists in the list
                boolean isValidLogin = false;
                for (int i = 0; i < StudentNumbers.length; i++) {
                    if (StudentNumbers[i].equals(studentNumber) && Passwords[i].equals(password)) {
                        isValidLogin = true;
                        break;
                    }
                }

                // If valid login, navigate to MainActivity
                if (isValidLogin) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close LoginActivity
                } else {
                    // Show error message if login is invalid
                    Toast.makeText(LoginActivity.this, "Invalid student number or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}