package com.cjsm.saferoutefinder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.*;
import com.google.android.material.imageview.ShapeableImageView;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final String TAG = "MainActivity";

    private WebView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private FrameLayout detailLayout;
    private TextView distanceView, estimatedTimeView, arrivalTimeView;
    private ListView postItemListView, yourItemListView;

    private double userLat = 14.5839, userLng = 120.9833;

    // Dummy data
    private final String[] usernames = {
            "XxShadowGamerxX", "MysticDragon92", "TechnoNinja07", "PixelPanda25",
            "LunarEclipse08", "TurboTiger44", "EpicNova23", "QuantumKnight01",
            "CyberFox_42", "StormChaser17"
    };

    private final String[] places = {
            "San Marcelino Street", "Quiapo Market", "University of Santo Tomas",
            "Intramuros", "Manila Cathedral", "Rizal Park", "SM City Manila",
            "Malate District", "Manila Ocean Park", "Baywalk"
    };

    private final String[] timeDates = {
            "Dec 9, 2024 | 11:55PM", "Dec 9, 2024 | 11:30PM", "Dec 9, 2024 | 11:00PM",
            "Dec 9, 2024 | 10:45PM", "Dec 9, 2024 | 10:15PM", "Dec 9, 2024 | 9:50PM",
            "Dec 9, 2024 | 9:30PM", "Dec 9, 2024 | 9:00PM", "Dec 9, 2024 | 8:30PM", "Dec 9, 2024 | 8:00PM"
    };

    private final String[] contentPosts = {
            "Streetlights are dim...", "Saw police patrolling regularly...",
            "Crowded during peak hours...", "Construction near the main intersection...",
            "Several street vendors block sidewalks...", "Loud gatherings near the park...",
            "Some areas are poorly lit...", "Street racing is common...",
            "Road closure near the university...", "A lot of stray dogs..."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "✅ App Started");

        initViews();
        initWebView();
        initSidebar();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestCurrentLocation();
    }

    private void initViews() {
        mapView = findViewById(R.id.mapView);
        postItemListView = findViewById(R.id.scroll);
        yourItemListView = findViewById(R.id.yourscroll);

        View routeDetailsView = getLayoutInflater().inflate(R.layout.route_details_overlay, null);
        detailLayout = routeDetailsView.findViewById(R.id.route_details_overlay);
        distanceView = routeDetailsView.findViewById(R.id.distance_text);
        estimatedTimeView = routeDetailsView.findViewById(R.id.estimated_time_text);
        arrivalTimeView = routeDetailsView.findViewById(R.id.arrival_time_text);
    }

    private void initWebView() {
        WebSettings settings = mapView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);

        settings.setUserAgentString("Mozilla/5.0");

        mapView.setWebChromeClient(new WebChromeClient());
        mapView.setWebViewClient(new CustomWebViewClient());
        mapView.addJavascriptInterface(new WebAppInterface(this), "Android");
        mapView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
    }

    private void initSidebar() {
        LinearLayout sidebar = findViewById(R.id.sidebar);
        ShapeableImageView burgerButton = findViewById(R.id.burgerButton);
        ShapeableImageView profileButton = findViewById(R.id.profile_button);
        ShapeableImageView communityButton = findViewById(R.id.communitybutton);
        ShapeableImageView settingsButton = findViewById(R.id.settingsButton);
        ShapeableImageView routeButton = findViewById(R.id.routeButton);

        final boolean[] sidebarShown = {false};

        burgerButton.setOnClickListener(v -> {
            if (!sidebarShown[0]) {
                sidebar.setVisibility(View.VISIBLE);
                sidebar.post(() -> {
                    sidebar.setTranslationX(-sidebar.getWidth());
                    sidebar.animate().translationX(0).setDuration(250).start();
                    sidebarShown[0] = true;
                });
            } else {
                hideSidebar(sidebar, sidebarShown);
            }
        });

        routeButton.setOnClickListener(v -> {
            mapView.evaluateJavascript("enableRouteMode()", null);
            Toast.makeText(this, "You can now choose your preferred destination", Toast.LENGTH_SHORT).show();
        });

        profileButton.setOnClickListener(v -> {
            Log.d(TAG, "Profile Button Clicked");
            yourItemListView.setAdapter(new CustomAdapter(this, usernames, places, timeDates, contentPosts));
            showDialogs(R.layout.activity_account_settings);
        });

        communityButton.setOnClickListener(v -> {
            Log.d(TAG, "Community Button Clicked");
            postItemListView.setAdapter(new CustomAdapter(this, usernames, places, timeDates, contentPosts));
            showDialogs(R.layout.activity_communitytab);
        });

        settingsButton.setOnClickListener(v -> showDialogs(R.layout.activity_settings));

        mapView.setOnTouchListener((v, event) -> {
            if (sidebarShown[0]) {
                hideSidebar(sidebar, sidebarShown);
                return true;
            }
            return false;
        });
    }

    private void hideSidebar(LinearLayout sidebar, boolean[] sidebarShown) {
        sidebar.animate().translationX(-sidebar.getWidth()).setDuration(250)
                .withEndAction(() -> {
                    sidebar.setVisibility(View.GONE);
                    sidebarShown[0] = false;
                }).start();
    }

    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        Log.d(TAG, "📡 Requesting Location...");
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();
            } else {
                Log.w(TAG, "⚠️ Location is null, using defaults.");
            }
            loadMap();
        });
    }

    private void loadMap() {
        String url = "file:///android_asset/map.html?lat=" + userLat + "&lng=" + userLng;
        Log.d(TAG, "🗺️ Loading Map: " + url);
        mapView.loadUrl(url);
    }

    private void showDialogs(int layoutResId) {
        View dialogView = getLayoutInflater().inflate(layoutResId, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.TransparentDialog)
                .setView(dialogView)
                .create();
        dialog.show();
    }


    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "🌍 WebView Loaded: " + url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(MainActivity.this, "Error loading map: " + description, Toast.LENGTH_LONG).show();
            Log.e("WebViewError", "Error " + errorCode + ": " + description);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if (uri == null || uri.getScheme() == null) return false;

            switch (uri.getScheme()) {
                case "tel":
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)
                            == PackageManager.PERMISSION_GRANTED) {
                        startActivity(new Intent(Intent.ACTION_CALL, uri));
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                    }
                    return true;
                case "sms":
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                default:
                    return false;
            }
        }
    }

    public class WebAppInterface {
        private final Context context;

        WebAppInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void sendDestination(double lat, double lng) {
            runOnUiThread(() ->
                    Toast.makeText(context, "Destination: " + lat + ", " + lng, Toast.LENGTH_SHORT).show()
            );
            Log.d(TAG, "📍 Destination: " + lat + ", " + lng);
        }
    }

    public class JavaScriptInterface {
        Context context;

        JavaScriptInterface(Context c) {
            context = c;
        }

        @JavascriptInterface
        public void showRouteDetails(String distance, String estimatedTime, String arrivalTime) {
            runOnUiThread(() -> showRouteOverlay(distance, estimatedTime, arrivalTime));
        }
    }

    private void showRouteOverlay(String distance, String estimatedTime, String arrivalTime) {
        detailLayout.setVisibility(View.VISIBLE);
        distanceView.setText(distance + " km");
        estimatedTimeView.setText(estimatedTime + " mins");
        arrivalTimeView.setText(arrivalTime);
    }

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

    public static class CustomAdapter extends BaseAdapter {
        private final String[] usernames, places, timeDates, contentPosts;
        private final LayoutInflater inflater;

        public CustomAdapter(Context context, String[] usernames, String[] places, String[] timeDates, String[] contentPosts) {
            this.usernames = usernames;
            this.places = places;
            this.timeDates = timeDates;
            this.contentPosts = contentPosts;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return usernames.length;
        }

        @Override
        public Object getItem(int position) {
            return usernames[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.post, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.user)).setText(usernames[position]);
            ((TextView) convertView.findViewById(R.id.place)).setText(places[position]);
            ((TextView) convertView.findViewById(R.id.timedate)).setText(timeDates[position]);
            ((TextView) convertView.findViewById(R.id.contentpost)).setText(contentPosts[position]);

            return convertView;
        }
    }
}
