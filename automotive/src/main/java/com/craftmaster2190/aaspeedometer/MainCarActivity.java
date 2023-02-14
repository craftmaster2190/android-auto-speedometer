package com.craftmaster2190.aaspeedometer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarUiController;

import java.util.concurrent.TimeUnit;

public class MainCarActivity extends CarActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private AwesomeSpeedometer awesomeSpeedometer;
    private TextView compassDirection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(this.getClass().getSimpleName(), "onCreate start");
        super.onCreate(savedInstanceState);
        setIgnoreConfigChanges(0xFFFFFFFF);
        CarUiController ctrl = getCarUiController();
        ctrl.getStatusBarController().hideAppHeader();
        ctrl.getMenuController().hideMenuButton();

        setContentView(R.layout.speedometer_frame);
        awesomeSpeedometer = (AwesomeSpeedometer) findViewById(R.id.awesomeSpeedometer);
        awesomeSpeedometer.setMinMaxSpeed(0.0f, 80.0f);
        awesomeSpeedometer.setUnit("mph");

        compassDirection = (TextView) findViewById(R.id.compassDirection);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Log.i(this.getClass().getSimpleName(), "onCreate finish");
    }

    @Override
    public void onStart() {
        Log.i(this.getClass().getSimpleName(), "onStart");
        super.onStart();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.w(this.getClass().getSimpleName(), "Unable to get permissions.");
            return;
        }

        locationListener = (Location location) -> {
            float metersPerSecond = location.getSpeed();
            float kilometersPerHour = metersPerSecond * 3.6f;
            float milesPerHour = metersPerSecond * 2.23694f;

            float bearing = location.getBearing();
            CompassDirection compassDirection = CompassDirection.fromBearing(bearing);

            Log.i(this.getClass().getSimpleName(), "Got location: " + location +
                    " speeds=(" + metersPerSecond + "mps, " +
                    kilometersPerHour + "kph, " +
                    milesPerHour + "mph) " +
                    "bearing=(" + bearing + ", compass=" + compassDirection + ")");
            this.getMainExecutor().execute(() -> {
                awesomeSpeedometer.speedTo(milesPerHour);
                this.compassDirection.setText(compassDirection.getAbbreviation());
            });
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                TimeUnit.MILLISECONDS.toSeconds(1), 1.0f, locationListener);
        Log.i(this.getClass().getSimpleName(), "requestLocationUpdates should start");
    }

    @Override
    public void onStop() {
        Log.i(this.getClass().getSimpleName(), "onStop");
        locationManager.removeUpdates(locationListener);
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        Log.i(this.getClass().getSimpleName(), "Configuration changed: " + configuration);
        super.onConfigurationChanged(configuration);
    }
}
