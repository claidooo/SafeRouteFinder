
package com.cjsm.saferoutefinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FabMenuHandler {
    private final Activity activity;
    private boolean isFabOpen = false;
    private final View fabMenu;

    public FabMenuHandler(Activity activity,
                          FloatingActionButton fabMain,
                          FloatingActionButton fabAccount,
                          FloatingActionButton fabCommunity,
                          FloatingActionButton fabSettings,
                          FloatingActionButton fabLogout,
                          View fabMenu) {
        this.activity = activity;
        this.fabMenu = fabMenu;

        fabMain.setOnClickListener(v -> toggleFabMenu());

        fabAccount.setOnClickListener(v -> openAccountSettings());
        fabCommunity.setOnClickListener(v -> showCommunityTab());
        fabSettings.setOnClickListener(v -> openSettings());
    }

    private void toggleFabMenu() {
        fabMenu.setVisibility(isFabOpen ? View.GONE : View.VISIBLE);
        isFabOpen = !isFabOpen;
    }

    private void openAccountSettings() {
        // Inflate the layout
        View dialogView = activity.getLayoutInflater().inflate(R.layout.activity_account_settings, null);

        // Reference UI elements inside the popup

        View logoutButton = dialogView.findViewById(R.id.btn_logout);

        // Create and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(activity, R.style.TransparentDialog)
                .setView(dialogView)
                .create();

        // Dismiss dialog when back button is clicked


        // Handle logout confirmation
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setTitle("Confirm Logout")
                    .setMessage("Do you wish to log out?")
                    .setPositiveButton("Logout", (d, which) -> System.exit(0))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        dialog.show();
    }



    private void showCommunityTab() {
        // Inflate the layout
        View dialogView = activity.getLayoutInflater().inflate(R.layout.activity_communitytab, null);

        // Optionally set up close button behavior
        View backButton = dialogView.findViewById(R.id.back_Button);
        AlertDialog dialog = new AlertDialog.Builder(activity, R.style.TransparentDialog)
                .setView(dialogView)
                .create();

        // Close popup on back button click
        backButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }


    private void openSettings() {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }


}

