//
//  AliyunPlayerView.m
//  AliyunPlayerDemo
//
//  Created by 何晏波 on 2019/5/15.
//

#import "AliyunPlayerView.h"
#define WeakObj(o) __weak typeof(o) o##Weak = o;

@interface AliyunPlayerView ()

@property (nonatomic, strong) NSDictionary *prepareAsyncParams;
@property (nonatomic, strong) NSTimer *timer;

@end

@implementation AliyunPlayerView

- (void)dealloc {
  if (_aliPlayer) {
      // 销毁
    [_aliPlayer releasePlayer];
    if (_timer) {
      [self clearTimer];
    }
    _aliPlayer = nil;
  }
}

#pragma mark - Props config
- (void)setPrepareAsyncParams:(NSDictionary *)prepareAsyncParams {
  _prepareAsyncParams = prepareAsyncParams;
  [self setupAliPlayer];
}

- (void)setMuteMode:(BOOL)muteMode {
    self.aliPlayer.muteMode = muteMode;
}

- (void)setQuality:(NSInteger)quality {
    self.aliPlayer.quality = quality;
}

- (void)setVolume:(float)volume {
    self.aliPlayer.volume = volume;
}

- (void)setBrightness:(float)brightness {
    self.aliPlayer.brightness = brightness;
}

- (void)setupAliPlayer {
  [self addSubview:self.aliPlayer.playerView];
  
  NSString *type = [_prepareAsyncParams objectForKey:@"type"];
  if ([type isEqualToString:@"vidSts"]) {
    NSString *vid = [_prepareAsyncParams objectForKey:@"vid"];
    NSString *accessKeyId = [_prepareAsyncParams objectForKey:@"accessKeyId"];
    NSString *accessKeySecret = [_prepareAsyncParams objectForKey:@"accessKeySecret"];
    NSString *securityToken = [_prepareAsyncParams objectForKey:@"securityToken"];
    [self.aliPlayer prepareWithVid:vid accessKeyId:accessKeyId accessKeySecret:accessKeySecret securityToken:securityToken];
  }
}

- (void) layoutSubviews {
  [super layoutSubviews];
  for(UIView* view in self.subviews) {
    [view setFrame:self.bounds];
  }
}

#pragma mark - 播放器初始化
-(AliyunVodPlayer *)aliPlayer{
  if (!_aliPlayer) {
    _aliPlayer = [[AliyunVodPlayer alloc] init];
    _aliPlayer.delegate = self;
    _aliPlayer.quality= 0;
    _aliPlayer.circlePlay = YES;
    _aliPlayer.autoPlay = NO;
    [_aliPlayer setDisplayMode: AliyunVodPlayerDisplayModeFitWithCropping];
    NSArray *pathArray = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *docDir = [pathArray objectAtIndex:0];
    //maxsize: MB, maxDuration: second
    [_aliPlayer setPlayingCache:YES saveDir:docDir maxSize:3000 maxDuration:100000];
  }
  return _aliPlayer;
}

#pragma mark - AliyunVodPlayerDelegate
- (void)vodPlayer:(AliyunVodPlayer *)vodPlayer onEventCallback:(AliyunVodPlayerEvent)event{
  NSLog(@"onEventCallback: %ld", event);
  
  NSMutableDictionary *callbackExt = [NSMutableDictionary dictionary];
  //这里监控播放事件回调
  //主要事件如下：
  switch (event) {
    case AliyunVodPlayerEventPrepareDone:
      //播放准备完成时触发
      [_aliPlayer start];
      [_aliPlayer setCirclePlay:false];
      if (self.onGetAliyunMediaInfo) {
          NSMutableDictionary *info = [NSMutableDictionary dictionary];
          
          AliyunVodPlayerVideo *video = [vodPlayer getAliyunMediaInfo];
          info[@"videoId"] = video.videoId;
          info[@"title"] = video.title;
          info[@"duration"] = @(video.duration);
          info[@"coverUrl"] = video.coverUrl;
          info[@"videoQuality"] = @(video.videoQuality);
          info[@"videoDefinition"] = video.videoDefinition;
          info[@"allSupportQualitys"] = video.allSupportQualitys;
          self.onGetAliyunMediaInfo(info);
      }
      break;
    case AliyunVodPlayerEventPlay:
      //暂停后恢复播放时触发
      [self setupTimer];
      break;
    case AliyunVodPlayerEventFirstFrame:
      //播放视频首帧显示出来时触发
      [self setupTimer];
      break;
    case AliyunVodPlayerEventPause:
      //视频暂停时触发
      [self clearTimer];
      break;
    case AliyunVodPlayerEventStop:
      //主动使用stop接口时触发
      [self clearTimer];
      break;
    case AliyunVodPlayerEventFinish:
      //视频正常播放完成时触发
      NSLog(@"视频播放完毕");
       [self clearTimer];
      break;
    case AliyunVodPlayerEventBeginLoading:
      //视频开始载入时触发
      break;
    case AliyunVodPlayerEventEndLoading:
      //视频加载完成时触发
      break;
    case AliyunVodPlayerEventSeekDone:
      //视频Seek完成时触发
      break;
    default:
      break;
  }

  
  NSLog(@"视频播放事件%d",event);
  [callbackExt setObject:@(event) forKey:@"event"];
  if (self.onEventCallback) {
    self.onEventCallback(callbackExt);
  }
}

- (void)vodPlayer:(AliyunVodPlayer *)vodPlayer playBackErrorModel:(AliyunPlayerVideoErrorModel *)errorModel{
  //播放出错时触发，通过errorModel可以查看错误码、错误信息、视频ID、视频地址和requestId。
  NSLog(@"errorModel: %d", errorModel.errorCode);
    if (self.onPlayBackErrorModel) {
        self.onPlayBackErrorModel(@{
                                 @"errorCode": @(errorModel.errorCode),
                                 @"errorMsg": errorModel.errorMsg,
                                 @"errorVid": errorModel.errorVid,
                                 @"errorUrl": errorModel.errorUrl,
                                 @"errorRequestId": errorModel.errorRequestId
                                 }
                                );
    }
}
- (void)vodPlayer:(AliyunVodPlayer*)vodPlayer willSwitchToQuality:(AliyunVodPlayerVideoQuality)quality videoDefinition:(NSString*)videoDefinition{
  //将要切换清晰度时触发
  NSLog(@"willSwitchToQuality:%@", videoDefinition);
    if (self.onSwitchToQuality) {
        self.onSwitchToQuality(@{
                                 @"type": @"will",
                                 @"quality": @(quality),
                                 @"videoDefinition": videoDefinition}
                               );
    }
}
- (void)vodPlayer:(AliyunVodPlayer *)vodPlayer didSwitchToQuality:(AliyunVodPlayerVideoQuality)quality videoDefinition:(NSString*)videoDefinition{
    //清晰度切换完成后触发
    if (self.onSwitchToQuality) {
        self.onSwitchToQuality(@{
                                 @"type": @"did",
                                 @"quality": @(quality),
                                 @"videoDefinition": videoDefinition}
                                );
    }
    
}
- (void)vodPlayer:(AliyunVodPlayer*)vodPlayer failSwitchToQuality:(AliyunVodPlayerVideoQuality)quality videoDefinition:(NSString*)videoDefinition{
  //清晰度切换失败触发
    if (self.onSwitchToQuality) {
        self.onSwitchToQuality(@{
                                 @"type": @"fail",
                                 @"quality": @(quality),
                                 @"videoDefinition": videoDefinition}
                               );
    }
    
}
- (void)onCircleStartWithVodPlayer:(AliyunVodPlayer*)vodPlayer{
  //开启循环播放功能，开始循环播放时接收此事件。
}
- (void)onTimeExpiredErrorWithVodPlayer:(AliyunVodPlayer *)vodPlayer{
  //播放器鉴权数据过期回调，出现过期可重新prepare新的地址或进行UI上的错误提醒。
}
/*
 *功能：播放过程中鉴权即将过期时提供的回调消息（过期前一分钟回调）
 *参数：videoid：过期时播放的videoId
 *参数：quality：过期时播放的清晰度，playauth播放方式和STS播放方式有效。
 *参数：videoDefinition：过期时播放的清晰度，MPS播放方式时有效。
 *备注：使用方法参考高级播放器-点播。
 */
- (void)vodPlayerPlaybackAddressExpiredWithVideoId:(NSString *)videoId quality:(AliyunVodPlayerVideoQuality)quality videoDefinition:(NSString*)videoDefinition{
  //鉴权有效期为2小时，在这个回调里面可以提前请求新的鉴权，stop上一次播放，prepare新的地址，seek到当前位置
}

#pragma mark - Timer
- (void)setupTimer {
  if (!_timer) {
    WeakObj(self)
    _timer = [NSTimer scheduledTimerWithTimeInterval:1 repeats:YES block:^(NSTimer * _Nonnull timer) {
      if (selfWeak.aliPlayer) {
        NSDictionary *callbackExt = @{
                               @"currentTime": @(selfWeak.aliPlayer.currentTime),
                               @"duration": @(selfWeak.aliPlayer.duration)
                               };
        if (selfWeak.onPlayingCallback) {
          selfWeak.onPlayingCallback(callbackExt);
        }
        NSLog(@"Timer is keep running... %@", callbackExt);
      }
    }];
    [[NSRunLoop currentRunLoop] addTimer:_timer forMode:NSRunLoopCommonModes];
  } else {
    [self clearTimer];
    [self setupTimer];
  }
}

- (void)clearTimer {
  [_timer invalidate];
  _timer = nil;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

@end
