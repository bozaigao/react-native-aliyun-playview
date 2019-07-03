//
//  AliyunRecordModule.h
//  ZhiZhuoUser
//
//  Created by 何晏波 on 2019/5/17.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTViewManager.h>
#import <AliyunVodPlayerSDK/AliyunVodPlayerSDK.h>
#import <VODUpload/VODUploadSVideoClient.h>

@interface AliyunRecordModule : RCTEventEmitter <RCTBridgeModule, VODUploadSVideoClientDelegate>
@property (nonatomic, strong) VODUploadSVideoClient *client;
@property (nonatomic, strong) NSString *type;
@end
