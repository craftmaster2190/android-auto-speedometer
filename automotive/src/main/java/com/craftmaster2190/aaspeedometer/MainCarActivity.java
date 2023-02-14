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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.sentry.Sentry;

public class MainCarActivity extends CarActivity {

    private static final float INITIAL_MAX_SPEED = 80.0f;
    public static final float BUFFER = 10f;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private AwesomeSpeedometer awesomeSpeedometer;
    private TextView compassDirection;
    private volatile float maxSpeed = INITIAL_MAX_SPEED;
    private volatile Speeds speeds;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(this.getClass().getSimpleName(), "onCreate start");
        super.onCreate(savedInstanceState);
        setIgnoreConfigChanges(0xFFFFFFFF);
        CarUiController ctrl = getCarUiController();
        ctrl.getStatusBarController().hideAppHeader();
        ctrl.getMenuController().hideMenuButton();

        initSpeedometerUnit();

        setContentView(R.layout.speedometer_frame);
        awesomeSpeedometer = (AwesomeSpeedometer) findViewById(R.id.awesomeSpeedometer);
        awesomeSpeedometer.setMinMaxSpeed(0.0f, maxSpeed);
        awesomeSpeedometer.setUnit(speedometerUnit.getAbbreviation());
        awesomeSpeedometer.setWithTremble(false);

        compassDirection = (TextView) findViewById(R.id.compassDirection);

        awesomeSpeedometer.setOnClickListener(v -> {
            SpeedometerUnit oldSpeedometerUnit = this.speedometerUnit;
            SpeedometerUnit newSpeedometerUnit = toggleSpeedometerUnit();
            String abbreviation = newSpeedometerUnit.getAbbreviation();
            Log.i(this.getClass().getSimpleName(), "Updating speedometerUnit(" +
                    oldSpeedometerUnit + " -> " + newSpeedometerUnit + ")");
            awesomeSpeedometer.setUnit(abbreviation);
            updateSpeeds(speeds);
        });

        compassDirection.setText("Awaiting GPS");

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
            compassDirection.setText("Grant Location Permissions on Phone");
            return;
        }

        locationListener = (Location location) -> {
            float metersPerSecond = location.getSpeed();
            speeds = Speeds.fromMetersPerSecond(metersPerSecond);

            float bearing = location.getBearing();
            CompassDirection compassDirection = null;
            try {
                compassDirection = CompassDirection.fromBearing(bearing);
            } catch (Exception e) {
                String message = "Unable to get compassDirection bearing=" + bearing;
                Log.e(this.getClass().getSimpleName(), message, e);
                Sentry.captureMessage(message + " " + e.getMessage());
            }
            String compassDirectionAbbreviation =
                    compassDirection == null ? null : compassDirection.getAbbreviation();

            Log.i(this.getClass().getSimpleName(), "Got location: " + location +
                    " speeds=(" + metersPerSecond + "mps, " +
                    speeds.getKilometersPerHour() + "kph, " +
                    speeds.getMilesPerHour() + "mph) " +
                    "bearing=(" + bearing + ", compass=" + compassDirection + ")");
            this.getMainExecutor().execute(() -> {
                updateSpeeds(speeds);

                this.compassDirection.setText(compassDirectionAbbreviation == null
                        ? "" : compassDirectionAbbreviation);
            });
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                TimeUnit.MILLISECONDS.toSeconds(1), 1.0f, locationListener);
        Log.i(this.getClass().getSimpleName(), "requestLocationUpdates should start");
    }

    private void updateSpeeds(Speeds speeds) {
        if (speeds != null) {
            float newSpeed = 0;
            switch (speedometerUnit) {
                case MPH:
                    newSpeed = speeds.getMilesPerHour();
                    break;
                case KPH:
                    newSpeed = speeds.getKilometersPerHour();
                    break;
            }

            maxSpeed = Math.max(INITIAL_MAX_SPEED - BUFFER, newSpeed + BUFFER);
            awesomeSpeedometer.setMaxSpeed(maxSpeed);
            awesomeSpeedometer.speedTo(newSpeed, 600);
        }
    }

    @Override
    public void onStop() {
        Log.i(this.getClass().getSimpleName(), "onStop");
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        Log.i(this.getClass().getSimpleName(), "Configuration changed: " + configuration);
        super.onConfigurationChanged(configuration);
    }

    private SpeedometerUnit speedometerUnit;

    private SpeedometerUnit initSpeedometerUnit() {
        return speedometerUnit = SpeedometerUnit.valueOf(
                getSharedPreferences("prefs", Context.MODE_PRIVATE)
                        .getString("speedometerUnit", SpeedometerUnit.MPH.name()));
    }

    private SpeedometerUnit toggleSpeedometerUnit() {
        int speedometerUnitOrdinal = Optional.ofNullable(speedometerUnit).map(Enum::ordinal).orElse(0) + 1;
        speedometerUnitOrdinal = speedometerUnitOrdinal % SpeedometerUnit.values().length;
        speedometerUnit = SpeedometerUnit.values()[speedometerUnitOrdinal];
        getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString("speedometerUnit", speedometerUnit.name()).apply();
        return speedometerUnit;
    }
}
