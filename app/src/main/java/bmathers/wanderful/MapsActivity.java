package bmathers.wanderful;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private static final int LOCATION_INTERVAL = 1000;
    //For saving instance state in case of instance destruction
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "requesting_updates";
    private static final String LOCATION_KEY = "location";
    private static final String TRIP_DURATION_KEY = "trip_duration";
    private static final String TRANSPORTATION_KEY = "transportation_mode";

    private String modeOfTransportation;
    private double tripDuration;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Marker mMarker;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderApi fusedLocationProviderApi;
    //Can use in later implementations
    private boolean mRequestingLocationUpdates = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.d("MapsActivity", "Activity Created");
        Intent intent = getIntent();
        modeOfTransportation = intent.getStringExtra("vehicle");
        Log.d("MapsActivity", "Transportation Mode is " + modeOfTransportation);
        //TODO: The second parameter in the getDoubleExtra is a default value. idk what that should be
        tripDuration = intent.getDoubleExtra("tripDuration", 15.0);
        Log.d("MapsActivity", "Trip Duration is " + tripDuration);

        updateValuesFromBundle(savedInstanceState);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //creates instance of GoogleApiClient
        if (mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //creates Location Request
        createLocationRequest();
        //might need below code if we need to change location settings
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
//        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocation is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            if (savedInstanceState.keySet().contains(TRIP_DURATION_KEY)){
                tripDuration = savedInstanceState.getDouble(TRIP_DURATION_KEY);
            }

            if (savedInstanceState.keySet().contains(TRANSPORTATION_KEY)){
                modeOfTransportation = savedInstanceState.getString(TRANSPORTATION_KEY);
            }

            updateUI();
        }
    }

//    private void getLocation(){
//        LocationRequest locationRequest;
//        locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(LOCATION_INTERVAL);
//        locationRequest.setFastestInterval(LOCATION_INTERVAL);
//        fusedLocationProviderApi = LocationServices.FusedLocationApi;
//    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart(){
        //connect api client
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop(){
        //might want to modify if we want directions to continue when phone is locked
        mGoogleApiClient.disconnect();
        //We are not calling stopLocationUpdates() here because we want them to continue when focus shifts
        super.onStop();
    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putDouble(TRIP_DURATION_KEY, tripDuration);
        savedInstanceState.putString(TRANSPORTATION_KEY, modeOfTransportation);
        super.onSaveInstanceState(savedInstanceState);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
// Here, thisActivity is the current activity
        mMap = googleMap;
        //getIntent();

        //checks to see if user granted location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        else{
            Log.d("MapsActivity", "Error: User has not granted location permission");
        }

        if(mCurrentLocation != null) {
            //
        }

    }

    @Override
    public void onConnected(Bundle connectionHint){
        Log.d("MapsActivity", "Connected to Google Services API");
        acquireLastLocation();

        if (mRequestingLocationUpdates){
            Log.d("MapsActivity", "Requesting Updates");
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("MapsActivity", "Permission Granted to Request Location Updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else{
            Log.d("MapsActivity", "Permission Denied");
        }
    }

    private void acquireLastLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("MapsActivity", "Location Permission granted. setting Last Location");
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//            fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient,  mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        }
        else{
            Log.d("MapsActivity", "Permission Denied");
        }

        if(mCurrentLocation != null){
            Log.d("MapsActivity", "Location Set");
            Log.d("MapsActivity", "Lat:" + mCurrentLocation.getLatitude());
            Log.d("MapsActivity", "Long:" + mCurrentLocation.getLongitude());
        }
        else {
            Log.d("MapsActivity", "mCurrentLocation is null");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("MapsActivity", "Connection to Google API Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("MapsActivity", "Location Updated");
        mCurrentLocation = location;
        updateUI();
    }

    private void updateUI(){
        //Can be updated to show changes in the map
        Toast.makeText(this, "location :" + mCurrentLocation.getLatitude() + " , " + mCurrentLocation.getLongitude(), Toast.LENGTH_SHORT).show();

        LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(loc).title("You are here."));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }
}
