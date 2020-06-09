//
//  CDVBaiduASR.h
//  myTestCordova
//
//  Created by hankers.yan on 2020/5/11.
//

#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>

@interface CDVBaiduASR : CDVPlugin

- (void)startOnline:(CDVInvokedUrlCommand *)command;
- (void)startLong:(CDVInvokedUrlCommand *)command;
- (void)stopLong:(CDVInvokedUrlCommand *)command;

@end
