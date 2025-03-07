package com.cjsm.saferoutefinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private WebView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private double userLat = 14.5835, userLng = 120.9842; // Default to Adamson Univ.

    private Map<String, Double> streetRatings = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize WebView
        mapView = findViewById(R.id.mapView);
        WebSettings webSettings = mapView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mapView.setWebViewClient(new WebViewClient());
        mapView.addJavascriptInterface(new WebAppInterface(this), "Android");

        loadStreetRatings();

        // Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            getUserLocation();
        }
    }

    private void loadStreetRatings() {
        try {
            InputStream is = getAssets().open("ratings.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray streets = jsonObject.getJSONArray("streets");

            for (int i = 0; i < streets.length(); i++) {
                JSONObject street = streets.getJSONObject(i);
                String name = street.getString("name");
                double rating = street.getDouble("rating");
                streetRatings.put(name, rating);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateSafestRoute(double startLat, double startLng, double endLat, double endLng) {
        try {
            JSONArray route = new JSONArray();

            // Simulate safest path logic (this should be replaced with actual pathfinding)
            JSONObject segment1 = new JSONObject();
            segment1.put("lat", startLat);
            segment1.put("lng", startLng);
            segment1.put("street", "San Marcelino St");

            JSONObject segment2 = new JSONObject();
            segment2.put("lat", (startLat + endLat) / 2);
            segment2.put("lng", (startLng + endLng) / 2);
            segment2.put("street", "Taft Avenue");

            JSONObject segment3 = new JSONObject();
            segment3.put("lat", endLat);
            segment3.put("lng", endLng);
            segment3.put("street", "Gen. Luna St");

            route.put(segment1);
            route.put(segment2);
            route.put(segment3);

            String routeJson = route.toString();
            Log.d("DEBUG", "Generated route JSON: " + routeJson);

            sendRouteToWebView(routeJson);

            Log.d("DEBUG", "Calculating safest route from " + startLat + ", " + startLng + " to " + endLat + ", " + endLng);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRouteToWebView(String routeJson) {
        Log.d("DEBUG", "Sending route to WebView: " + routeJson);
        runOnUiThread(() -> mapView.evaluateJavascript("drawSafestRoute(" + routeJson + ")", null));
    }

    public class WebAppInterface {
        private MainActivity activity;

        public WebAppInterface(MainActivity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public void sendDestination(double lat, double lng) {
            activity.runOnUiThread(() -> {
                Toast.makeText(activity, "Destination: " + lat + ", " + lng, Toast.LENGTH_SHORT).show();
                Log.d("DEBUG", "Destination received: " + lat + ", " + lng);
                activity.setDestination(lat, lng);
            });
        }
    }
    private double destLat, destLng;

    public void setDestination(double lat, double lng) {
        this.destLat = lat;
        this.destLng = lng;
        calculateSafestRoute(userLat, userLng, destLat, destLng);
    }


    // Get user's location
    private void getUserLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLat = location.getLatitude();
                            userLng = location.getLongitude();
                        }
                        loadMap();
                    }
                });
    }

    // Load the map with user location
    private void loadMap() {
        String mapUrl = "file:///android_asset/map.html?lat=" + userLat + "&lng=" + userLng;
        mapView.loadUrl(mapUrl);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                loadMap(); // Load default map if permission is denied
            }
        }
    }
}

