//
//  RNConnectivityStatus.h
//
//  Created by Mattia Panzeri on 10/10/2017.
//  Copyright © 2017 Near Srl. All rights reserved.
//

#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#import "RCTUtils.h"
#else
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTUtils.h>
#endif

#import <CoreLocation/CoreLocation.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface RNConnectivityStatus : RCTEventEmitter <RCTBridgeModule, CLLocationManagerDelegate, CBCentralManagerDelegate>

#define RN_CONNECTIVITY_STATUS_TOPIC @"RNConnectivityStatus"
#define EVENT_TYPE @"eventType"
#define EVENT_STATUS @"status"

@end
