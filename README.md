
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

// Check if Bluetooth is ON
const bluetoothIsOn = await ConnectivityManager.isBluetoothEnabled()
```

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
```js
import ConnectivityManager from 'react-native-connectivity-status'

// Ask user to turn on Location Services
ConnectivityManager.enableLocation()

// Ask user to turn on Bluetooth
ConnectivityManager.enableBluetooth()
```

**ATTENTION:** On `iOS` the `enableLocation` method won't ask the user for `Location Permissions` but will redirect to
`iOS` location settings screen to allow the user to enable `Location Services`.
In case `Location Services` are active but `Location Permissions` have yet to be asked to the user the method call will throw an error.

**ATTENTION:** Starting from `iOS 11` redirect to specific Settings page has been disabled, so the `enableLocation` method will only redirect to the `Settings` main screen.

---
Made with :sparkles: & :heart: by [Mattia Panzeri](https://github.com/panz3r) and [contributors](https://github.com/nearit/react-native-connectivity-status/graphs/contributors)