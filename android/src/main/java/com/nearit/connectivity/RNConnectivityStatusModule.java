package com.nearit.connectivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RNConnectivityStatusModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private final static String RN_CONNECTIVITY_STATUS_TOPIC = "RNConnectivityStatus";
    private final static String EVENT_TYPE = "eventType";
    private final static String EVENT_STATUS = "status";

    // Location permission status
    private static final String PERMISSION_LOCATION_GRANTED = "Location.Permission.Granted.Always";
    private static final String PERMISSION_LOCATION_DENIED = "Location.Permission.Denied";

    private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                boolean active = false;
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        active = false;
                        break;
                    case BluetoothAdapter.STATE_ON:
                        active = true;
                        break;
                }

                final WritableMap eventMap = new WritableNativeMap();
                eventMap.putString(EVENT_TYPE, "bluetooth");
                eventMap.putBoolean(EVENT_STATUS, active);
                getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(RN_CONNECTIVITY_STATUS_TOPIC, eventMap);
            }
        }
    };

    private final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final boolean locationEnabled = intent.getAction() != null
                    && intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)
                    && checkLocationServices();

            final WritableMap eventMap = new WritableNativeMap();
            eventMap.putString(EVENT_TYPE, "location");
            eventMap.putBoolean(EVENT_STATUS, locationEnabled);
            getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(RN_CONNECTIVITY_STATUS_TOPIC, eventMap);
        }
    };

    public RNConnectivityStatusModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNConnectivityStatus";
    }

    @javax.annotation.Nullable
    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("LocationGrantedAlways", PERMISSION_LOCATION_GRANTED);
                put("LocationDenied", PERMISSION_LOCATION_DENIED);
            }
        });
    }

    @Override
    public void initialize() {
        super.initialize();

        final IntentFilter btFilter = new IntentFilter();
        btFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        reactContext.getApplicationContext().registerReceiver(mBtReceiver, btFilter);

        final IntentFilter locationFilter = new IntentFilter();
        locationFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        reactContext.getApplicationContext().registerReceiver(mLocationReceiver, locationFilter);
    }

    @ReactMethod
    public void isBluetoothEnabled(final Promise promise) {
        try {
            promise.resolve(checkBluetooth());
        } catch (Exception e) {
            promise.reject("BLE_CHECK_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void areLocationServicesEnabled(final Promise promise) {
        try {
            promise.resolve(checkLocationServices());
        } catch (Exception e) {
            promise.reject("LOCATION_CHECK_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void isLocationPermissionGranted(final Promise promise) {
        try {
            if (checkLocationPermission()) {
                promise.resolve(PERMISSION_LOCATION_GRANTED);
            } else {
                promise.resolve(PERMISSION_LOCATION_DENIED);
            }
        } catch (Exception e) {
            promise.reject("LOCATION_PERMISSION_CHECK_ERROR", e.getMessage());
        }
    }

    /**
     * Private methods
     */
    private boolean checkBluetooth() {
        return BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    private boolean checkLocationServices() {
        final LocationManager locationManager = (LocationManager) getReactApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        return (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                | (locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(getReactApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}