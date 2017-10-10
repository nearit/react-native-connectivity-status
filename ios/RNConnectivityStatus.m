//
//  RNConnectivityStatus.m
//
//  Created by Mattia Panzeri on 10/10/2017.
//  Copyright © 2017 Near Srl. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RNConnectivityStatus.h"

@implementation RNConnectivityStatus {
  bool hasListeners;
  CLLocationManager *locationManager;
  CBCentralManager *bluetoothManager;
}

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
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
      bluetoothManager = [[CBCentralManager alloc] initWithDelegate:self queue:dispatch_get_main_queue()];
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
  
  if (locationManager) {
    [self sendActiveState:[self isLocationActiveState]
                  forType:@"location"];
  }
  
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

RCT_EXPORT_METHOD(isBluetoothEnabled:(RCTPromiseResolveBlock) resolve
                            rejecter:(RCTPromiseRejectBlock) reject)
{
    BOOL btIsActive = bluetoothManager && bluetoothManager.state == CBCentralManagerStatePoweredOn;
    resolve(@(btIsActive));
}

// MARK: CBCentralManager Delegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
  [self sendActiveState:@(central.state == CBCentralManagerStatePoweredOn)
                forType:@"bluetooth"];
}

// MARK: Location

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

RCT_EXPORT_METHOD(isLocationEnabled:(RCTPromiseResolveBlock) resolve
                           rejecter:(RCTPromiseRejectBlock) reject)
{
  resolve(@([self isLocationActiveState]));
}

// MARK: CLLocationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
  [self sendActiveState:[self isLocationActiveState]
                forType:@"location"];
}

@end
