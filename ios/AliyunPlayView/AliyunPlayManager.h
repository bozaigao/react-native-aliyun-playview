//
//  AliyunPlayManager.h
//  AliyunPalyDemo
//
//  Created by 何晏波 on 2019/5/15.
//
// import RCTViewManager
#if __has_include(<React/RCTViewManager.h>)
#import <React/RCTViewManager.h>
#elif __has_include(“RCTViewManager.h”)
#import “RCTViewManager.h”
#else
#import “React/RCTViewManager.h” // Required when used as a Pod in a Swift project
#endif
#import <React/RCTBridgeModule.h>
#import "AliyunPlayerView.h"


// Subclass your view manager off the RCTViewManager
// http://facebook.github.io/react-native/docs/native-components-ios.html#ios-mapview-example
@interface AliyunPlayManager : RCTViewManager
@property (nonatomic, strong) AliyunPlayerView *playerView;
@end
