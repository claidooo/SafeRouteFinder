package com.cjsm.saferoutefinder;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private WebView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private FloatingActionButton fabMain, fabAccount, fabRatings, fabSettings, fabLogout;
    private View fabMenu;
    private boolean isFabOpen = false;
    private double userLat = 14.5839, userLng = 120.9833; // Default Manila location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("DEBUG", "✅ App Started");

        // Initialize WebView
        mapView = findViewById(R.id.mapView);
        WebSettings webSettings = mapView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);

        mapView.setWebChromeClient(new WebChromeClient());
        mapView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("DEBUG", "🌍 WebView Loaded: " + url);
            }
        });

        mapView.addJavascriptInterface(new WebAppInterface(this), "Android");

        // Initialize Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestCurrentLocation();

        // Initialize FAB menu
        fabMain = findViewById(R.id.fab_main);
        fabMenu = findViewById(R.id.fab_menu);
        fabAccount = findViewById(R.id.fab_account);
        fabRatings = findViewById(R.id.fab_ratings);
        fabSettings = findViewById(R.id.fab_settings);
        fabLogout = findViewById(R.id.fab_logout);

        // Toggle FAB Menu
        fabMain.setOnClickListener(v -> toggleFabMenu());

        // Handle Menu Button Clicks
        fabAccount.setOnClickListener(v -> openAccountSettings());
        fabRatings.setOnClickListener(v -> showRatingsPopup());
        fabSettings.setOnClickListener(v -> openSettings());
        fabLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    // ✅ Toggle FAB Menu
    private void toggleFabMenu() {
        if (isFabOpen) {
            fabMenu.setVisibility(View.GONE);
        } else {
            fabMenu.setVisibility(View.VISIBLE);
        }
        isFabOpen = !isFabOpen;
    }

    // ✅ Request Location Permissions & Fetch User Location
    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    // ✅ Get Real-Time Location
    private void getCurrentLocation() {
        Log.d("DEBUG", "📡 Requesting Location...");
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                } else {
                    Log.e("DEBUG", "⚠️ Location is NULL, using default location.");
                }
                loadMap();
            }
        });
    }

    // ✅ Load Map with User's Location
    private void loadMap() {
        String mapUrl = "file:///android_asset/map.html?lat=" + userLat + "&lng=" + userLng;
        Log.d("DEBUG", "🗺️ Loading Map: " + mapUrl);
        runOnUiThread(() -> mapView.loadUrl(mapUrl));
    }

    // ✅ Handle Destination Selection from WebView
    public class WebAppInterface {
        private MainActivity activity;

        public WebAppInterface(MainActivity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public void sendDestination(double lat, double lng) {
            activity.runOnUiThread(() -> {
                Toast.makeText(activity, "Destination: " + lat + ", " + lng, Toast.LENGTH_SHORT).show();
                Log.d("DEBUG", "📍 Destination: " + lat + ", " + lng);
            });
        }
    }

    // ✅ Handle Permission Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                loadMap();
            }
        }
    }

    // ✅ Handle Account Settings
    private void openAccountSettings() {
        //Intent intent = new Intent(MainActivity.this, AccountSettingsActivity.class);
        //startActivity(intent);
    }

    // ✅ Handle Ratings Popup
    private void showRatingsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate This Area")
                .setMessage("How would you rate the safety of this street?")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // ✅ Handle App Settings
    private void openSettings() {
        //Intent intent = new Intent(MainActivity.this, activity.class);
        //startActivity(intent);
    }

    // ✅ Handle Logout Confirmation
    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> finish())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
