package com.cjsm.saferoutefinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WebView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private double userLat = 14.5839, userLng = 120.9833; // Default location (Manila)
    private String startStreet, destinationStreet;
    private Map<String, Double> streetRatings = new HashMap<>();

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

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e("WEBVIEW_ERROR", "❌ WebView Error: " + description);
            }
        });

        mapView.addJavascriptInterface(new WebAppInterface(this), "Android");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadStreetRatings();
        requestCurrentLocation(); // Get location before loading map
    }

    // ✅ Request Location Permissions & Fetch User Location
    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    // ✅ Get Real-Time Location & Convert to Street Name
    private void getCurrentLocation() {
        Log.d("DEBUG", "📡 Requesting Location...");
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                    startStreet = getStreetFromLocation(userLat, userLng);
                    Log.d("DEBUG", "🏠 Start Street: " + startStreet);
                } else {
                    Log.e("DEBUG", "⚠️ Location is NULL, using default location.");
                }
                loadMap(); // Load the map with updated location
            }
        });
    }

    // ✅ Convert Coordinates to Street Name
    private String getStreetFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getThoroughfare(); // Extract street name
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Street"; // Default fallback
    }

    // ✅ Load Map with User's Location
    private void loadMap() {
        String mapUrl = "file:///android_asset/map.html?lat=" + userLat + "&lng=" + userLng;
        Log.d("DEBUG", "🗺️ Loading Map: " + mapUrl);

        if (getAssets() != null) {
            try {
                InputStream is = getAssets().open("map.html");
                is.close(); // If this succeeds, file exists
            } catch (IOException e) {
                Log.e("ERROR", "❌ map.html NOT FOUND in assets!");
                return;
            }
        }

        runOnUiThread(() -> mapView.loadUrl(mapUrl));
    }

    // ✅ Load Street Ratings from JSON (for Safest Path Calculation)
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
            Log.d("DEBUG", "✅ Street Ratings Loaded");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR", "❌ Failed to load street ratings.");
        }
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
                destinationStreet = getStreetFromLocation(lat, lng);
                Toast.makeText(activity, "Destination: " + destinationStreet, Toast.LENGTH_SHORT).show();
                Log.d("DEBUG", "📍 Destination: " + destinationStreet);
                activity.findSafestRoute();
            });
        }
    }

    // ✅ Find Safest Route Using Street Ratings
    private void findSafestRoute() {
        if (startStreet == null || destinationStreet == null) {
            Log.e("ERROR", "⚠️ Start or Destination is missing.");
            return;
        }

        List<String> safestPath = SafestPathFinder.findSafestPath(this, startStreet, destinationStreet);
        Log.d("SAFEST_ROUTE", "🚦 Safest Path: " + safestPath);

        JSONArray routeJson = new JSONArray();
        for (String street : safestPath) {
            JSONObject point = new JSONObject();
            try {
                point.put("street", street);
                routeJson.put(point);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sendRouteToWebView(routeJson.toString());
    }

    // ✅ Send Route Data to WebView
    private void sendRouteToWebView(String routeJson) {
        Log.d("DEBUG", "🚀 Sending route to WebView: " + routeJson);
        runOnUiThread(() -> mapView.evaluateJavascript("drawSafestRoute(" + routeJson + ")", null));
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
}
