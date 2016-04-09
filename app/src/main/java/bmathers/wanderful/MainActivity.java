package bmathers.wanderful;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;

public class MainActivity extends AppCompatActivity {

    final String APIKey = "AIzaSyAIVptHzsnZfwojldq400b74Rfm1GIFwmw";

    @Override
    protected void onResume() {
        super.onResume();
        //launch event on the pebble
//        Context context = getApplicationContext();
//
//        boolean isConnected = PebbleKit.isWatchConnected(context);
//
//        if (isConnected) {
//            // Launch the sports app
//            //TODO: Eventually change to launch Wanderful
//            PebbleKit.startAppOnPebble(context, Constants.SPORTS_UUID);
//
//            Toast.makeText(context, "Launching...", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(context, "Watch is not connected!", Toast.LENGTH_LONG).show();
//        }

        boolean isConnected = PebbleKit.isWatchConnected(this);
        Toast.makeText(this, "Pebble " + (isConnected ? "is" : "is not") + " connected!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button launchButton = (Button)findViewById(R.id.launch_button);
        launchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //test calls to google maps api
            }

        });
    }
}
