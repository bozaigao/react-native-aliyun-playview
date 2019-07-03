//
//  AliyunPlayerView.h
//  AliyunPlayerDemo
//
//  Created by 何晏波 on 2019/5/15.
//

#import <UIKit/UIKit.h>
#import <React/RCTComponent.h>
#import <AliyunVodPlayerSDK/AliyunVodPlayerSDK.h>

@interface AliyunPlayerView : UIView <AliyunVodPlayerDelegate>

@property (nonatomic, strong) AliyunVodPlayer *aliPlayer;
@property (nonatomic, copy) RCTBubblingEventBlock onEventCallback;
@property (nonatomic, copy) RCTBubblingEventBlock onGetAliyunMediaInfo;
@property (nonatomic, copy) RCTBubblingEventBlock onPlayingCallback;
@property (nonatomic, copy) RCTBubblingEventBlock onSwitchToQuality;
@property (nonatomic, copy) RCTBubblingEventBlock onPlayBackErrorModel;


@end
