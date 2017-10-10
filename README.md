
# React-native-connectivity-status

## Getting started

Add `react-native-connectivity-status` module to your project

`$ yarn add react-native-connectivity-status`

Follow either one of the installation guides, we suggest you to follow the `Automatic installation` one.


### Automatic installation

`$ react-native link react-native-connectivity-status`

<br>

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-connectivity-status` and add `RNConnectivityStatus.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNConnectivityStatus.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.nearit.connectivity.RNConnectivityStatusPackage;` to the imports at the top of the file
  - Add `new RNConnectivityStatusPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-connectivity-status'
  	project(':react-native-connectivity-status').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-connectivity-status/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-connectivity-status')
  	```


## Usage

Interactively check Location Services and Bluetooth status
```js
import RNConnectivityStatus from 'react-native-connectivity-status'

// Check if Location Services are enabled (on Android) or app has Location permission (on iOS)
const locationServicesAvailable = await RNConnectivityStatus.isLocationEnabled()

// Check if Bluetooth is ON
const bluetoothIsOn = await RNConnectivityStatus.isBluetoothEnabled()
```

Subscribe to updates
```js
import { addConnectivityStatusListener } from 'react-native-connectivity-status'

const connectivityStatusSubscription = addConnectivityStatusListener(({ eventType, status }) => {
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