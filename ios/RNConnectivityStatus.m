//
//  RNConnectivityStatus.m
//
//  Created by Mattia Panzeri on 10/10/2017.
//  Copyright © 2017 Near Srl. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RNConnectivityStatus.h"

#define SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(v)  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)

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
        case CBManagerStateUnknown:
        case CBManagerStateResetting:
        case CBManagerStateUnsupported:
        case CBManagerStateUnauthorized:
        case CBManagerStatePoweredOff:
            return NO;

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

RCT_EXPORT_METHOD(enableBluetooth:(RCTPromiseResolveBlock) resolve
                         rejecter:(RCTPromiseRejectBlock) reject)
{
    NSLog(@"iOS: trying to open Bluetooth settings");
    if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"10.0")) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"App-Prefs:root=Bluetooth"]];
    } else {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"prefs:root=Bluetooth"]];
    }

    resolve(@(YES));
}

// MARK: CBCentralManager Delegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    BOOL centralBtIsActive = central && [self isBluetoothActiveState:central.state];
  
    [self sendActiveState:centralBtIsActive
                forType:@"bluetooth"];
}

// MARK: Location Permissions

- (BOOL)isLocationActiveState {
  switch (CLLocationManager.authorizationStatus) {
    case kCLAuthorizationStatusNotDetermined:
    case kCLAuthorizationStatusRestricted:
    case kCLAuthorizationStatusDenied:
      return NO;
      
    case kCLAuthorizationStatusAuthorizedWhenInUse:
    case kCLAuthorizationStatusAuthorizedAlways:
      return YES;
      
    default:
      return NO;
  }
}

// MARK: Location Services

RCT_EXPORT_METHOD(areLocationServicesEnabled:(RCTPromiseResolveBlock) resolve
                           rejecter:(RCTPromiseRejectBlock) reject) {
    resolve(@(CLLocationManager.locationServicesEnabled));
}

RCT_EXPORT_METHOD(enableLocation:(RCTPromiseResolveBlock) resolve
                          reject:(RCTPromiseRejectBlock) reject)
{
    NSURL* settingsUrl;
    if (!CLLocationManager.locationServicesEnabled) {
        if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"10.0")) {
            settingsUrl = [NSURL URLWithString:@"App-Prefs:root=Privacy&path=LOCATION"];
        } else {
            settingsUrl = [NSURL URLWithString:@"prefs:root=LOCATION_SERVICES"];
        }
    } else if (CLLocationManager.authorizationStatus != kCLAuthorizationStatusNotDetermined) {
        settingsUrl = [NSURL URLWithString:UIApplicationOpenSettingsURLString];
    } else {
        NSLog(@"iOS: Location permission request needed");
        reject(@"ERR_ENABLE_LOCATION", @"Location Services are enabled but Location permission request is required", nil);
    }

    if (settingsUrl != nil) {
        NSLog(@"iOS: trying to open Location settings at %@", settingsUrl);
        [[UIApplication sharedApplication] openURL:settingsUrl options:@{} completionHandler:^(BOOL success) {
            resolve(@(success));
        }];
    }
}

// MARK: CLLocationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
    [self sendActiveState:CLLocationManager.locationServicesEnabled
                  forType:@"location"];
}

@end
