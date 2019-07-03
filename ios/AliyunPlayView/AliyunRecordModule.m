//
//  AliyunRecordModule.m
//  ZhiZhuoUser
//
//  Created by 何晏波 on 2019/5/17.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "AliyunRecordModule.h"

@implementation AliyunRecordModule
RCT_EXPORT_MODULE();


//短视频上传
RCT_EXPORT_METHOD(uploadVideo:(NSDictionary *)params)
{
  
  dispatch_async(dispatch_get_main_queue(), ^{
    NSString *accessKeyId = [RCTConvert NSString:params[@"accessKeyId"]];
    NSString *accessKeySecret = [RCTConvert NSString:params[@"accessKeySecret"]];
    NSString *securityToken = [RCTConvert NSString:params[@"securityToken"]];
    NSString *mp4Path = [RCTConvert NSString:params[@"mp4Path"]];
     _type = [RCTConvert NSString:params[@"type"]];
    NSLog(@"类型%@%@",_type,mp4Path);
  NSLog(@"accessKeyId%@,accessKeySecret%@,securityToken%@,mp4Path%@",accessKeyId,accessKeySecret,securityToken,mp4Path);
    _client = [[VODUploadSVideoClient alloc] init];
    _client.delegate = self;
    _client.transcode = false;//是否转码，建议开启转码
    
    // ini/Users/heyanbei/Desktop/ZhiZhuoUser/ZhiZhuoUser/ios/Classes/Toolst video info
    VodSVideoInfo *svideoInfo = [VodSVideoInfo new];
    svideoInfo.title = [self currentTimeStr];
      [svideoInfo setTitle:@"iOS_video"];
      [svideoInfo setDesc:@"iOS_desc"];
      [svideoInfo setTags:@"iOS_Tags"];
    [svideoInfo setCateId:@(561654619)];
    
    // get fisrt pic
    UIImage *img = [self getScreenShotImageFromVideoPath:mp4Path];
    
    NSLog(@"img  width:%g height:%g", img.size.width, img.size.height);
    
    // Create path.
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *filePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:@"Image.png"];
    
    // Save image.
    [UIImagePNGRepresentation(img) writeToFile:filePath atomically:YES];
    //  ALAssetsLibrary *library = [[ALAssetsLibrary alloc] init];
    //  [library writeVideoAtPathToSavedPhotosAlbum:[NSURL fileURLWithPath:filePath] completionBlock:^(NSURL *assetURL, NSError *error) {
    //    // upload
    //    BOOL res = [client uploadWithVideoPath:mp4Path imagePath:filePath svideoInfo: svideoInfo accessKeyId:accessKeyId accessKeySecret:accessKeySecret accessToken:securityToken];
    //  }];
    NSLog(@"video Path  %@", mp4Path);
    NSLog(@"img Path  %@", filePath);
    // upload
    BOOL res = [_client uploadWithVideoPath:mp4Path imagePath:filePath svideoInfo: svideoInfo accessKeyId:accessKeyId accessKeySecret:accessKeySecret accessToken:securityToken];
    
  });
  
}

//获取当前时间戳
- (NSString *)currentTimeStr{
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:0];//获取当前时间0秒后的时间
  NSTimeInterval time=[date timeIntervalSince1970]*1000;// *1000 是精确到毫秒，不乘就是精确到秒
  NSString *timeString = [NSString stringWithFormat:@"%.0f", time];
  return timeString;
}


/**
 *  获取视频的缩略图方法
 *
 *  @param filePath 视频的本地路径
 *
 *  @return 视频截图
 */
- (UIImage *)getScreenShotImageFromVideoPath:(NSString *)filePath{
  UIImage *shotImage;
  //视频路径URL
  NSURL *fileURL = [NSURL fileURLWithPath:filePath];
  AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:fileURL options:nil];
  AVAssetImageGenerator *gen = [[AVAssetImageGenerator alloc] initWithAsset:asset];
  gen.appliesPreferredTrackTransform = YES;
  CMTime time = CMTimeMakeWithSeconds(0.0, 600);
  NSError *error = nil;
  CMTime actualTime;
  CGImageRef image = [gen copyCGImageAtTime:time actualTime:&actualTime error:&error];
  shotImage = [[UIImage alloc] initWithCGImage:image];
  CGImageRelease(image);
  return shotImage;
}


/**
 上传成功
 @param vid 视频vid
 @param imageUrl 图片路径
 */
- (void)uploadSuccessWithResult:(VodSVideoUploadResult *)result{
  NSLog(@"上传成功%@",result.videoId);
  [self sendEventWithName:@"getUploadState" body:@{@"code":@(0),@"message":@"上传成功",@"vid":result.videoId,@"type":_type}];
}

/**
 上传失败
 @param code 错误码
 @param message 错误日志
 */
- (void)uploadFailedWithCode:(NSString *)code message:(NSString *)message{
  
  NSLog(@"上传失败vid%@",message);
  [self sendEventWithName:@"getUploadState" body:@{@"code":@(-1),@"message":@"上传失败"}];
};


/**
 上传进度
 @param uploadedSize 已上传的文件大小
 @param totalSize 文件总大小
 */
- (void)uploadProgressWithUploadedSize:(long long)uploadedSize totalSize:(long long)totalSize{
  [self sendEventWithName:@"getProgressState" body:@(uploadedSize*1.0/totalSize)];
};


/**
 token过期
 */
- (void)uploadTokenExpired{
  NSLog(@"uploadTokenExpired");
};


/**
 开始重试
 */
- (void)uploadRetry{
  NSLog(@"uploadRetry");
};


/**
 重试完成，继续上传
 */
- (void)uploadRetryResume{
  NSLog(@"uploadRetryResume");
};

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"getUploadState"];
}


/**
 *  多个视频合成为一个视频输出到指定路径,注意区分是否3D视频
 *
 *  @param tArray       视频文件NSURL地址
 *  @param storePath    沙盒目录下的文件夹
 *  @param storeName    合成的文件名字
 *  @param successBlock 成功block
 *  @param failureBlcok 失败block
 */
RCT_EXPORT_METHOD(mergeVideoToOneVideo:(NSDictionary *)params :(RCTResponseSenderBlock)callback)
{
  NSArray *tArray = [RCTConvert NSStringArray:params[@"videoPaths"]];


  AVMutableComposition *mixComposition = [self mergeVideos:tArray];
  
  NSArray *cachePaths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory
                                                            , NSUserDomainMask
                                                     
                                                            , YES);
  NSString*path=[cachePaths objectAtIndex:0];
  NSString *outputFileUrl =  [[NSString alloc]initWithFormat:@"%@/%ld.mp4",path,(long)[[NSDate date] timeIntervalSince1970]*1000];
  NSLog(@"合成后的视频储存路径%@",outputFileUrl);
  [self storeAVMutableComposition:mixComposition withStoreUrl:outputFileUrl success:^{
    NSLog(@"视频合成成功%@",outputFileUrl);
    callback(@[outputFileUrl]);
  }];
}


/**
 *  多个视频合成为一个
 *
 *  @param array 多个视频的NSURL地址
 *
 *  @return 返回AVMutableComposition
 */
-(AVMutableComposition *)mergeVideos:(NSArray*)array
{
  AVMutableComposition* mixComposition = [AVMutableComposition composition];
  AVMutableCompositionTrack *a_compositionVideoTrack = [mixComposition addMutableTrackWithMediaType:AVMediaTypeVideo preferredTrackID:kCMPersistentTrackID_Invalid];
  a_compositionVideoTrack.preferredTransform =  CGAffineTransformMakeRotation(M_PI/2);
  //只合并视频，导出后声音会消失，所以需要把声音插入到混淆器中
  //添加音频,添加本地其他音乐也可以,与视频一致
  AVMutableCompositionTrack *audioTrack = [mixComposition addMutableTrackWithMediaType:AVMediaTypeAudio preferredTrackID:kCMPersistentTrackID_Invalid];
  
  Float64 tmpDuration =0.0f;
  for (NSInteger i=0; i<array.count; i++)
  {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([fileManager fileExistsAtPath:array[i]]) {
      NSLog(@"视频文件存在%@",array[i]);
    }
    else {
      NSLog(@"视频文件存不存在");
    }
    NSDictionary *optDict = [NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:AVURLAssetPreferPreciseDurationAndTimingKey];
    AVURLAsset *videoAsset = [[AVURLAsset alloc]initWithURL:[NSURL fileURLWithPath:array[i]] options:optDict];
    
    CMTimeRange video_timeRange = CMTimeRangeMake(kCMTimeZero,videoAsset.duration);
    
    /**
     *  依次加入每个asset
     *
     *  @param TimeRange 加入的asset持续时间
     *  @param Track     加入的asset类型,这里都是video
     *  @param Time      从哪个时间点加入asset,这里用了CMTime下面的CMTimeMakeWithSeconds(tmpDuration, 0),timesacle为0
     *
     */
    NSError *error;
    [a_compositionVideoTrack insertTimeRange:video_timeRange ofTrack:[[videoAsset tracksWithMediaType:AVMediaTypeVideo] objectAtIndex:0] atTime:CMTimeMakeWithSeconds(tmpDuration, 0) error:&error];
    
    [audioTrack insertTimeRange:video_timeRange ofTrack:[[videoAsset tracksWithMediaType:AVMediaTypeAudio]objectAtIndex:0] atTime:CMTimeMakeWithSeconds(tmpDuration, 0) error:&error];
    
    if(error){
      NSLog(@"报错啦%@",error);
    }
    
    tmpDuration += CMTimeGetSeconds(videoAsset.duration);
    NSLog(@"视频长度%f",tmpDuration);
    
  }
  return mixComposition;
}

/**
 *  存储合成的视频
 *
 *  @param mixComposition mixComposition参数
 *  @param storeUrl       存储的路径
 *  @param successBlock   successBlock
 *  @param failureBlcok   failureBlcok
 */
-(void)storeAVMutableComposition:(AVMutableComposition*)mixComposition withStoreUrl:(NSString *)storeUrl success:(void (^)(void))successBlock
{
   __weak typeof(self) welf = self;
  AVAssetExportSession* _assetExport = [[AVAssetExportSession alloc] initWithAsset:mixComposition presetName:AVAssetExportPresetHighestQuality];
  _assetExport.outputFileType = AVFileTypeMPEG4;
  //    _assetExport.outputFileType = @"public.mpeg-4";
  _assetExport.outputURL = [NSURL fileURLWithPath:storeUrl];
  [_assetExport exportAsynchronouslyWithCompletionHandler:^{
    switch (_assetExport.status) {
      case AVAssetExportSessionStatusUnknown:
        NSLog(@"exporter Unknow");
        break;
      case AVAssetExportSessionStatusCancelled:
        NSLog(@"exporter Canceled");
        break;
      case AVAssetExportSessionStatusFailed:
        NSLog(@"exporter Failed");
        break;
      case AVAssetExportSessionStatusWaiting:
        NSLog(@"exporter Waiting");
        break;
      case AVAssetExportSessionStatusExporting:
        NSLog(@"exporter Exporting");
        break;
      case AVAssetExportSessionStatusCompleted:
        NSLog(@"exporter Completed");
        break;
    } dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
      //在系统相册存储一份
//      UISaveVideoAtPathToSavedPhotosAlbum([storeUrl path], nil, nil, nil);
      successBlock();
    });
  }];
}

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

@end
