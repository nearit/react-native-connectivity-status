//
//  RNConnectivityStatus.m
//
//  Created by Mattia Panzeri on 10/10/2017.
//  Latest changes by Federico Boschini on 08/29/2018
//  Copyright © 2017 Near Srl. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RNConnectivityStatus.h"

#define SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(v)  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)

// Location permission status
NSString* const RNCS_PERMISSION_LOCATION_GRANTED_ALWAYS = @"Location.Permission.Granted.Always";
NSString* const RNCS_PERMISSION_LOCATION_GRANTED_WHEN_IN_USE = @"Location.Permission.Granted.WhenInUse";
NSString* const RNCS_PERMISSION_LOCATION_DENIED = @"Location.Permission.Denied";

@implementation RNConnectivityStatus {
  bool hasListeners;
  CLLocationManager *locationManager;
  CBCentralManager *bluetoothManager;
}

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

RCT_EXPORT_MODULE()

- (instancetype) init
{
  self = [super init];
  
  if (self != nil) {
    if(!locationManager) {
      locationManager = [[CLLocationManager alloc] init];
      locationManager.delegate = self;
    }
    
    if(!bluetoothManager) {
      bluetoothManager = [[CBCentralManager alloc] initWithDelegate:self
                                                              queue:dispatch_get_main_queue()
                                                            options:@{
                                                                      CBCentralManagerOptionShowPowerAlertKey: @0
                                                                  }];
    }
  }
  
  return self;
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"Permissions": @{
                     @"LocationGrantedAlways": RNCS_PERMISSION_LOCATION_GRANTED_ALWAYS,
                     @"LocationGrantedWhenInUse": RNCS_PERMISSION_LOCATION_GRANTED_ALWAYS,
                     @"LocationDenied": RNCS_PERMISSION_LOCATION_DENIED
                     }
             };
}

// MARK: RCTEventEmitter

- (NSArray<NSString *> *)supportedEvents
{
  return @[
           RN_CONNECTIVITY_STATUS_TOPIC
         ];
}

// Will be called when this module's first listener is added.
- (void)startObserving {
  hasListeners = YES;

  [self sendActiveState:CLLocationManager.locationServicesEnabled
                forType:@"location"];

  if (bluetoothManager) {
    [self centralManagerDidUpdateState:bluetoothManager];
  }
}

// Will be called when this module's last listener is removed, or on dealloc.
- (void)stopObserving {
  hasListeners = NO;
}

- (void)sendActiveState:(BOOL)state forType:(NSString* _Nonnull)eventType {
  if (hasListeners) {
      NSDictionary* event = @{
                              EVENT_TYPE: eventType,
                              EVENT_STATUS: @(state)
                            };

    [self sendEventWithName:RN_CONNECTIVITY_STATUS_TOPIC
                       body:event];
  }
}

// MARK: Bluetooth

- (BOOL)isBluetoothActiveState:(CBManagerState)bluetoothState {
    switch (bluetoothState) {
        case CBManagerStatePoweredOn:
            return YES;
        default:
            return NO;
    }
}

RCT_EXPORT_METHOD(isBluetoothEnabled:(RCTPromiseResolveBlock) resolve
                            rejecter:(RCTPromiseRejectBlock) reject)
{
    BOOL btIsActive = bluetoothManager && [self isBluetoothActiveState:bluetoothManager.state];
    resolve(@(btIsActive));
}

// MARK: CBCentralManager Delegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    BOOL centralBtIsActive = central && [self isBluetoothActiveState:central.state];
  
    [self sendActiveState:centralBtIsActive
                forType:@"bluetooth"];
}

// MARK: Location Permissions

- (LocationPermissionState)isLocationPermissionGranted {
    switch (CLLocationManager.authorizationStatus) {
        case kCLAuthorizationStatusAuthorizedWhenInUse:
            return LocationPermissionWhenInUse;
        case kCLAuthorizationStatusAuthorizedAlways:
            return LocationPermissionAlways;
        default:
            return LocationPermissionOff;
    }
}

RCT_EXPORT_METHOD(isLocationPermissionGranted:(RCTPromiseResolveBlock) resolve
                  rejecter:(RCTPromiseRejectBlock) reject) {
    LocationPermissionState state = [self isLocationPermissionGranted];
    switch (state) {
        case LocationPermissionWhenInUse:
            resolve(RNCS_PERMISSION_LOCATION_GRANTED_WHEN_IN_USE);
            break;
        case LocationPermissionAlways:
            resolve(RNCS_PERMISSION_LOCATION_GRANTED_ALWAYS);
            break;
        case LocationPermissionOff:
            resolve(RNCS_PERMISSION_LOCATION_DENIED);
            break;
        default:
            reject(@"LOCATION_PERMISSION_CHECK_ERROR", @"Can't check location permission", nil);
            break;
    }
}

// MARK: Location Services

RCT_EXPORT_METHOD(areLocationServicesEnabled:(RCTPromiseResolveBlock) resolve
                           rejecter:(RCTPromiseRejectBlock) reject) {
    resolve(@(CLLocationManager.locationServicesEnabled));
}

// MARK: CLLocationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
    [self sendActiveState:CLLocationManager.locationServicesEnabled
                  forType:@"location"];
}

@end
