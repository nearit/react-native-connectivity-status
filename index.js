
import { Platform, NativeModules, NativeEventEmitter } from 'react-native'

const { RNConnectivityStatus } = NativeModules

export default class ConnectivityManager {
  static _eventEmitter = new NativeEventEmitter(RNConnectivityStatus)

  static addStatusListener (connectivityListener) {
    return ConnectivityManager._eventEmitter.addListener('RNConnectivityStatus', connectivityListener)
  }

  static isBluetoothEnabled () {
    return RNConnectivityStatus.isBluetoothEnabled()
  }

  static areLocationServicesEnabled () {
    return RNConnectivityStatus.areLocationServicesEnabled()
  }

  static isLocationPermissionGranted () {
    return RNConnectivityStatus.isLocationPermissionGranted()
  }

}
