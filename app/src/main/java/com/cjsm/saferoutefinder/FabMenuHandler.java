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
        Intent intent = new Intent(activity, AccountSettingsActivity.class);
        activity.startActivity(intent);
    }

    private void showCommunityTab() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Community Tab")
                .setMessage("test")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void openSettings() {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }


}
