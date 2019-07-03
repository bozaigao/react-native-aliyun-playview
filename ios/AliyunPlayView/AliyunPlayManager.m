//  Created by react-native-create-bridge
#import <Foundation/Foundation.h>
#import <React/RCTUIManager.h>
#import "AliyunPlayManager.h"

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#elif __has_include(“RCTBridge.h”)
#import “RCTBridge.h”
#else
#import “React/RCTBridge.h” // Required when used as a Pod in a Swift project
#endif

@implementation AliyunPlayManager

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

RCT_EXPORT_VIEW_PROPERTY(prepareAsyncParams, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(muteMode, BOOL)
RCT_EXPORT_VIEW_PROPERTY(quality, NSInteger)
RCT_EXPORT_VIEW_PROPERTY(volume, float)
RCT_EXPORT_VIEW_PROPERTY(brightness, float)

RCT_EXPORT_VIEW_PROPERTY(onGetAliyunMediaInfo, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onEventCallback, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onPlayingCallback, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onPlayBackErrorModel, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onSwitchToQuality, RCTBubblingEventBlock)



- (UIView *)view
{
    AliyunPlayerView *playerView = [AliyunPlayerView new];
    self.playerView = playerView;
    return playerView;
}

#pragma mark - 开始播放
RCT_EXPORT_METHOD(start :(nonnull NSNumber *)reactTag) {
  [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
    AliyunPlayerView *playerView = (AliyunPlayerView *) viewRegistry[reactTag];
    [self.playerView.aliPlayer start];
  }];
}

RCT_EXPORT_METHOD(reset :(nonnull NSNumber *)reactTag) {
  NSLog(@"重置播放器");
  [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
    AliyunPlayerView *playerView = (AliyunPlayerView *) viewRegistry[reactTag];
    [self.playerView.aliPlayer reset];
  }];
}

RCT_EXPORT_METHOD(rePlay :(nonnull NSNumber *)reactTag) {
  NSLog(@"重置播放器");
  [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
    AliyunPlayerView *playerView = (AliyunPlayerView *) viewRegistry[reactTag];
    [self.playerView.aliPlayer replay];
  }];
}

RCT_EXPORT_METHOD(resume :(nonnull NSNumber *)reactTag) {
  
  [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
    AliyunPlayerView *playerView = (AliyunPlayerView *) viewRegistry[reactTag];
    [self.playerView.aliPlayer resume];
  }];
}

RCT_EXPORT_METHOD(pause :(nonnull NSNumber *)reactTag) {
  
  [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
    AliyunPlayerView *playerView = (AliyunPlayerView *) viewRegistry[reactTag];
   [self.playerView.aliPlayer pause];
  }];
}

RCT_EXPORT_METHOD(stop :(nonnull NSNumber *)reactTag) {
  [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
    AliyunPlayerView *playerView = (AliyunPlayerView *) viewRegistry[reactTag];
    [self.playerView.aliPlayer stop];
    [self.playerView.aliPlayer releasePlayer];
  }];
}

RCT_EXPORT_METHOD(seekToTime :(nonnull NSNumber *)reactTag time:(double )time ) {
  NSLog(@"seekToTime");
  [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
    AliyunPlayerView *playerView = (AliyunPlayerView *) viewRegistry[reactTag];
    [self.playerView.aliPlayer seekToTime:time];
  }];
}

//getAliyunMediaInfo

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

@end
