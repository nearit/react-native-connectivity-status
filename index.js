
import { NativeModules, NativeEventEmitter } from 'react-native'

const { RNConnectivityStatus } = NativeModules

export const addConnectivityStatusListener = connectivityListener => new NativeEventEmitter(RNConnectivityStatus).addListener("RNConnectivityStatus", connectivityListener)

export default RNConnectivityStatus
