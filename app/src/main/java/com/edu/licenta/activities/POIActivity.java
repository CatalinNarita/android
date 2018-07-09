package com.edu.licenta.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.activities.R;
import com.edu.licenta.model.GetNearbyPlacesData;
import com.edu.licenta.utils.VolleyUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naritc
 * on 11-Jun-18.
 */

public class POIActivity extends AppCompatActivity implements RecognitionListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, AdapterView.OnItemSelectedListener, SeekBar.OnSeekBarChangeListener {

    SpeechRecognizer speechRecognizer;
    TextView textView;
    GoogleMap googleMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastlocation;
    private Marker currentLocationmMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 10000;
    double latitude, longitude;
    String searchQuery;

    SeekBar seekBar;
    LocationManager locationManager;
    Location location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);

        checkLocationPermission();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Spinner spinner = findViewById(R.id.poi_spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.poi_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);


        textView = findViewById(R.id.speech_result_text_view);

        seekBar = findViewById(R.id.seek_bar_poi);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(50000);

        ButterKnife.bind(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            PROXIMITY_RADIUS = progress;
            textView.setText(getString(R.string.range,String.valueOf(progress)));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                System.out.println("audio error");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                System.out.println("client error");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                System.out.println("permission error");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                System.out.println("network error");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                System.out.println("timeout error");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                System.out.println("no match error");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                System.out.println("recognizer busy error");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                System.out.println("server error");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                System.out.println("speech timeout error");
                break;
            default:
                System.out.println("understand error");
                break;
        }
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override
    public void onPartialResults(Bundle arg0) {
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
    }

    @Override
    public void onResults(Bundle results) {

        ArrayList<String> matches = results

                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + matches.get(0));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void searchAddresses(String location) {

        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

        if (googleMap != null) {
            googleMap.clear();
        }
        String url = getUrl(latitude, longitude, location);
        dataTransfer[0] = googleMap;
        dataTransfer[1] = url;

        getNearbyPlacesData.execute(dataTransfer);

        CameraPosition position = CameraPosition.builder()
                .target(new LatLng(latitude, longitude))
                .zoom(16)
                .bearing(0)
                .tilt(45)
                .build();

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @OnClick(R.id.record_sound_btn)
    public void startListening() {

        boolean connAvailable = VolleyUtils.checkIfConnAvailable(getApplicationContext());

        if (connAvailable) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(this);

            Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

            speechRecognizer.startListening(recognizerIntent);
        } else {
            VolleyUtils.buildAlertDialog(getString(R.string.error_title), "No connections available!", POIActivity.this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) throws SecurityException {
        googleMap.setMyLocationEnabled(true);

        this.googleMap = googleMap;

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        CameraPosition position = CameraPosition.builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(16)
                .bearing(0)
                .tilt(45)
                .build();

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));

        bulidGoogleApiClient();
    }

    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastlocation = location;
        if (currentLocationmMarker != null) {
            currentLocationmMarker.remove();

        }
        Log.d("lat = ", "" + latitude);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationmMarker = googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyBLEPBRfw7sMb73Mr88L91Jqh3tuE4mKsE");

        Log.d("MapsActivity", "url = " + googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;

        } else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            bulidGoogleApiClient();
                        }
                        googleMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        searchQuery = parent.getItemAtPosition(position).toString().toLowerCase();
        searchQuery = searchQuery.substring(0, searchQuery.length() -1);
        if (searchQuery.equals("club")) {
            searchQuery = "night_club";
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @OnClick(R.id.search_poi)
    public void searchPOI() {
        searchAddresses(searchQuery);
    }
}
