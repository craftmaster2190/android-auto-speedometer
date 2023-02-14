package com.craftmaster2190.myapplication;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarUiController;

/**
 * @author Andrey Pavlenko
 */
public class MainCarActivity extends CarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(this.getClass().getSimpleName(), "onCreate start");
        super.onCreate(savedInstanceState);
        setIgnoreConfigChanges(0xFFFFFFFF);
        CarUiController ctrl = getCarUiController();
        ctrl.getStatusBarController().hideAppHeader();
        ctrl.getMenuController().hideMenuButton();

        setContentView(R.layout.hello_world);
        Log.i(this.getClass().getSimpleName(), "onCreate finish");
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        Log.i(this.getClass().getSimpleName(), "Configuration changed: " + configuration);
        super.onConfigurationChanged(configuration);
    }
}
