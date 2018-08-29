
# React-native-connectivity-status
> A [React Native](https://facebook.github.io/react-native/) module to check Bluetooth and Location status on Android and iOS

[![license](https://img.shields.io/github/license/nearit/react-native-connectivity-status.svg)](https://github.com/nearit/react-native-connectivity-status/blob/master/LICENSE.md)
[![Build Status](https://travis-ci.org/nearit/react-native-connectivity-status.svg)](https://travis-ci.org/nearit/react-native-connectivity-status)
[![npm](https://img.shields.io/npm/v/react-native-connectivity-status.svg)](https://www.npmjs.com/package/react-native-connectivity-status)

[![React Native](https://img.shields.io/badge/RN-0.41.2+-green.svg)](https://facebook.github.io/react-native/)
![platforms](https://img.shields.io/badge/platforms-Android%20%7C%20iOS-brightgreen.svg)

[![Gitter](https://img.shields.io/gitter/room/nearit/Lobby.svg)](https://gitter.im/nearit/Lobby)

## Getting started

Add `react-native-connectivity-status` module to your project

`$ yarn add react-native-connectivity-status`

And link it

`$ react-native link react-native-connectivity-status`

<br/>

## Usage

### Check Status
Interactively check Location Services and Bluetooth status
```js
import ConnectivityManager from 'react-native-connectivity-status'

// Check if Location Services are enabled
const locationServicesAvailable = await ConnectivityManager.isLocationEnabled()

// Check Location permission
const locationPermission = await ConnectivityManager.isLocationPermissionGranted()
switch(locationPermission) {
    case "Location.Permission.Denied":
    	// ...
        break;
    case "Location.Permission.Granted.Always":
        // ...
        break;
    case "Location.Permission.Granted.WhenInUse":
	// ...
        break;
    default:
        // ...
}

// Check if Bluetooth is ON
const bluetoothIsOn = await ConnectivityManager.isBluetoothEnabled()
```

**Note:** On Android, Location permission state will map on `Location.Permission.Denied` and `Location.Permission.Granted.Always` only.

Subscribe to updates
```js
import ConnectivityManager from 'react-native-connectivity-status'

const connectivityStatusSubscription = ConnectivityManager.addStatusListener(({ eventType, status }) => {
	switch (eventType) {
		case 'bluetooth':
					console.log(`Bluetooth is ${status ? 'ON' : 'OFF'}`)
				break
		case 'location':
					console.log(`Location Services are ${status ? 'AVAILABLE' : 'NOT available'}`)
				break
	}
})
...
// Remeber to unsubscribe from connectivity status events
connectivityStatusSubscription.remove()
```

### Enable services

**NOTE:** Due to possible app rejection from Apple (caused by illegal usage of private URL Scheme "prefs:root" or "App-Prefs:root"), methods for enabling bluetooth and location services have been removed from this module.

---
Made with :sparkles: & :heart: by [Mattia Panzeri](https://github.com/panz3r) and [contributors](https://github.com/nearit/react-native-connectivity-status/graphs/contributors)
