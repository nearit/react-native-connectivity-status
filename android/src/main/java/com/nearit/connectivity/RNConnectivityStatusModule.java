
package com.nearit.connectivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class RNConnectivityStatusModule extends ReactContextBaseJavaModule implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private final ReactApplicationContext reactContext;

  private final static String RN_CONNECTIVITY_STATUS_TOPIC = "RNConnectivityStatus";
  private final static String EVENT_TYPE = "eventType";
  private final static String EVENT_STATUS = "status";

  private GoogleApiClient mGoogleApiClient;
  private static final int NEAR_BLUETOOTH_SETTINGS_CODE = 4000;
  private static final int NEAR_LOCATION_SETTINGS_CODE = 5000;

  private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();

      if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
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
      boolean isGpsEnabled = false;
      if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
      }

      final WritableMap eventMap = new WritableNativeMap();
      eventMap.putString(EVENT_TYPE, "location");
      eventMap.putBoolean(EVENT_STATUS, isGpsEnabled);
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

  /**
   * Asks to enable bluetooth
   */
  @ReactMethod
  public void enableBluetooth(final Promise promise) {
    try {
      if (!checkBluetooth()) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        getReactApplicationContext().startActivityForResult(enableBtIntent, NEAR_BLUETOOTH_SETTINGS_CODE, new Bundle());
      }

      promise.resolve(true);
    } catch (Exception e) {
      promise.reject("BLE_ACTIVATION_ERROR", e.getMessage());
    }
  }

  @ReactMethod
  public void isLocationEnabled(final Promise promise) {
    LocationManager lm = (LocationManager) reactContext.getSystemService(Context.LOCATION_SERVICE);
    try {
      promise.resolve(lm.isProviderEnabled(LocationManager.GPS_PROVIDER));
    } catch (Exception e) {
      promise.reject("LOCATION_CHECK_ERROR", e.getMessage());
    }
  }

  /**
   * Asks to enable location services.
   */
  @ReactMethod
  public void enableLocation(final Promise promise) {
    mGoogleApiClient = new GoogleApiClient.Builder(getReactApplicationContext())
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();

    mGoogleApiClient.connect();

    promise.resolve(true);
  }

  /**
   * GoogleApiClient interfaces
   */
  @Override
  public void onConnected(@Nullable Bundle bundle) {
    final LocationRequest locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_LOW_POWER);

    final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setNeedBle(true);

    final PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override
      public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
          try {
            status.startResolutionForResult(getCurrentActivity(), NEAR_LOCATION_SETTINGS_CODE);
          } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
          }
        }
      }
    });
  }

  @Override
  public void onConnectionSuspended(int i) {
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
  }

  /**
   * Private methods
   */
  private boolean checkBluetooth() {
    return BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled();
  }
}