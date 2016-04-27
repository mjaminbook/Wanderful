package bmathers.wanderful;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity{

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 0;
    final String APIKey = "AIzaSyAIVptHzsnZfwojldq400b74Rfm1GIFwmw";

    //public final static String TRIP_LENGTH = "bmathers.wanderful";




    @Override
    protected void onResume() {
        super.onResume();
       // launch event on the pebble
        Context context = getApplicationContext();

        boolean isConnected = PebbleKit.isWatchConnected(context);

        if (isConnected) {
            // Launch the sports app
            //TODO: Eventually change to launch Wanderful
            PebbleKit.startAppOnPebble(context, Constants.SPORTS_UUID);

            Toast.makeText(context, "Launching...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Watch is not connected!", Toast.LENGTH_LONG).show();
        }

       // boolean isConnected = PebbleKit.isWatchConnected(this);
        Toast.makeText(this, "Pebble " + (isConnected ? "is" : "is not") + " connected!", Toast.LENGTH_LONG).show();
    }

//calculates and returns length of trip in hours
    private double getTime(){
        Spinner mySpinner=(Spinner) findViewById(R.id.time_spinner);
        int timePosition = mySpinner.getSelectedItemPosition();
        int minutes = (timePosition + 1) * 15;
        double hours = (double)minutes/ 60.0;
        return hours;
    }

    //gets selected mode of transport method and returns the string accepted by the pebble app
    //I did not put any way of detecting errors in here, for safety i default to return bicycling
    private String getMethodGerund(){
        Spinner vehicle = (Spinner) findViewById(R.id.vehicle_spinner);
        String transportation = vehicle.getSelectedItem().toString();
        if (transportation.equals("Drive")){
            return "driving";
        }else if (transportation.equals("Walk")){
            return "walking";
        } else{
            return "bicycling";
        }

    }

    //adds spinners and associated lists of values
    private void addSpinners(){
        Spinner timeSpinner = (Spinner)findViewById(R.id.time_spinner);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this, R.array.time_array, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

        Spinner vehicleSpinner = (Spinner)findViewById(R.id.vehicle_spinner) ;
        ArrayAdapter<CharSequence> vehicleAdapter = ArrayAdapter.createFromResource(this, R.array.vehicle_array, android.R.layout.simple_spinner_item);
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleSpinner.setAdapter(vehicleAdapter);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "Main Activity Created");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        this.addSpinners();

        Button launchButton = (Button)findViewById(R.id.launch_button);
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                double tripLength = getTime();
                String vehicle = getMethodGerund();
                System.out.println(tripLength + " " + vehicle);

                // should intent have shit from pebble?

                intent.putExtra("tripDuration", tripLength);
                intent.putExtra("vehicle", vehicle);
                startActivity(intent);
            }
        });
    }
}
