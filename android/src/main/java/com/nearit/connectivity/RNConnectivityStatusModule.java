
package com.nearit.connectivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNConnectivityStatusModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  
  private final static String RN_CONNECTIVITY_STATUS_TOPIC = "RNConnectivityStatus";
  private final static String EVENT_TYPE = "eventType";
  private final static String EVENT_STATUS = "status";

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
          final String action = intent.getAction();

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
  
  @ReactMethod
  public void isBluetoothEnabled(final Promise promise) {
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      try {
          promise.resolve(bluetoothAdapter != null && bluetoothAdapter.isEnabled());
      } catch (Exception e) {
          promise.reject("BLE_CHECK_ERROR", e.getMessage());
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
}