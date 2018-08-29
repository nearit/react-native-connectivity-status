//
//  RNConnectivityStatus.h
//
//  Created by Mattia Panzeri on 10/10/2017.
//  Latest changes by Federico Boschini on 08/29/2018
//  Copyright © 2017 Near Srl. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTUtils.h>

#import <CoreLocation/CoreLocation.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface RNConnectivityStatus : RCTEventEmitter <RCTBridgeModule, CLLocationManagerDelegate, CBCentralManagerDelegate>

#define RN_CONNECTIVITY_STATUS_TOPIC @"RNConnectivityStatus"
#define EVENT_TYPE @"eventType"
#define EVENT_STATUS @"status"

typedef NS_ENUM(NSInteger, LocationPermissionState) {
    LocationPermissionOff,
    LocationPermissionWhenInUse,
    LocationPermissionAlways
};

@end
