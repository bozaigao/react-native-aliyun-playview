package com.example.aliyunplayview;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.alibaba.sdk.android.vod.upload.VODSVideoUploadCallback;
import com.alibaba.sdk.android.vod.upload.VODSVideoUploadClient;
import com.alibaba.sdk.android.vod.upload.VODSVideoUploadClientImpl;
import com.alibaba.sdk.android.vod.upload.model.SvideoInfo;
import com.alibaba.sdk.android.vod.upload.session.VodHttpClientConfig;
import com.alibaba.sdk.android.vod.upload.session.VodSessionCreateInfo;
import com.example.aliyunplayview.util.PermissionChecker;
import com.example.aliyunplayview.util.ToastUtils;
import com.example.aliyunplayview.util.Utils;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class AliyunRecordModule extends ReactContextBaseJavaModule {
    private static final String TAG = "AliyunRecordModule";
    private ReactContext reactContext;
    private ByteBuffer mReadBuf;
    private int mOutAudioTrackIndex;
    private int mOutVideoTrackIndex;
    private MediaFormat mAudioFormat;
    private MediaFormat mVideoFormat;

    public AliyunRecordModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "AliyunRecordModule";
    }

    /**
     * 上传视频
     */
    @ReactMethod
    public void uploadVideo(ReadableMap params) {
        if (isPermissionOK()) {
            final String type = params.getString("type");
            String videoPath = params.getString("mp4Path");
            // String imagePath = params.getString("imagePath");
            if (videoPath.contains("file:")) {
                videoPath = videoPath.substring(7, videoPath.length());
            }
            String accessKeyId = params.getString("accessKeyId");
            String accessKeySecret = params.getString("accessKeySecret");
            String securityToken = params.getString("securityToken");
            File file = new File(videoPath);
            if (file.exists()) {
                Log.e("TAG", "视频文件存在");
            } else {
                Log.e("TAG", "视频文件不存在");
            }

            final String imagePath = Utils.getFirstFramePath(videoPath, this.reactContext);
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Log.e("TAG", "图片文件存在");
            } else {
                Log.e("TAG", "图片文件不存在");
            }

            // 1.初始化短视频上传对象
            final VODSVideoUploadClient vodsVideoUploadClient = new VODSVideoUploadClientImpl(this.reactContext.getApplicationContext());
            vodsVideoUploadClient.init();

            // 参数请确保存在，如不存在SDK内部将会直接将错误throw Exception
            // 文件路径保证存在之外因为Android 6.0之后需要动态获取权限，请开发者自行实现获取"文件读写权限".
            VodHttpClientConfig vodHttpClientConfig = new VodHttpClientConfig.Builder()
                    .setMaxRetryCount(2)             // 重试次数
                    .setConnectionTimeout(15 * 1000) // 连接超时
                    .setSocketTimeout(15 * 1000)     // socket超时
                    .build();

            // 构建短视频VideoInfo,常见的描述，标题，详情都可以设置
            SvideoInfo svideoInfo = new SvideoInfo();
            svideoInfo.setTitle(new File(videoPath).getName());
            svideoInfo.setDesc("");
            svideoInfo.setCateId(561654619);

            // 构建点播上传参数(重要)
            VodSessionCreateInfo vodSessionCreateInfo = new VodSessionCreateInfo.Builder()
                    .setPartSize(200000)            //设置视频上传切片大小
                    .setImagePath(imagePath)        // 图片地址
                    .setVideoPath(videoPath)        // 视频地址
                    .setAccessKeyId(accessKeyId)    // 临时accessKeyId
                    .setAccessKeySecret(accessKeySecret)    // 临时accessKeySecret
                    .setSecurityToken(securityToken)        // securityToken
                    // .setRequestID(requestID)                // requestID，开发者可以传将获取STS返回的requestID设置也可以不设.
                    .setIsTranscode(false)                   // 是否转码.如开启转码请AppSever务必监听服务端转码成功的通知
                    .setSvideoInfo(svideoInfo)              // 短视频视频信息
                    .setVodHttpClientConfig(vodHttpClientConfig)    //网络参数
                    .setExpriedTime("20000")
                    .build();


            vodsVideoUploadClient.uploadWithVideoAndImg(vodSessionCreateInfo, new VODSVideoUploadCallback() {
                @Override
                public void onUploadSucceed(String videoId, String imageUrl) {
                    //上传成功返回视频ID和图片URL.
                    Log.d(TAG, "onUploadSucceed" + "videoId:" + videoId + "imageUrl" + imageUrl);
                    WritableMap map = Arguments.createMap();
                    map.putString("vid", videoId);
                    map.putString("imageUrl", imageUrl);
//                    promise.resolve(map);
                    vodsVideoUploadClient.release();
                    UploadModel uploadModel = new UploadModel();
                    uploadModel.code = 0;
                    uploadModel.message = "上传成功";
                    uploadModel.vid = videoId;
                    uploadModel.type = type;
                    sendUploadState(uploadModel);
                }

                @Override
                public void onUploadFailed(String code, String message) {
                    //上传失败返回错误码和message.错误码有详细的错误信息请开发者仔细阅读
                    Log.d(TAG, "onUploadFailed" + "code" + code + "message" + message);
//                    promise.reject(code, message);
                    vodsVideoUploadClient.release();
                    UploadModel uploadModel = new UploadModel();
                    uploadModel.code = -1;
                    uploadModel.message = "上传失败";
                    sendUploadState(uploadModel);
                }

                @Override
                public void onUploadProgress(long uploadedSize, long totalSize) {
                    //上传的进度回调,非UI线程
                    Log.d(TAG, "onUploadProgress" + uploadedSize / totalSize);
                    sendProgrtessState(uploadedSize * 100 / totalSize);
                    // progress = uploadedSize * 100 / totalSize;
                    // handler.sendEmptyMessage(0);
                }

                @Override
                public void onSTSTokenExpried() {
                    Log.d(TAG, "onSTSTokenExpried");
                    //STS token过期之后刷新STStoken，如正在上传将会断点续传
                    // vodsVideoUploadClient.refreshSTSToken(accessKeyId,accessKeySecret,securityToken,expriedTime);
//                    promise.reject("401", "token 过期，请重新操作");
                    vodsVideoUploadClient.release();
                    UploadModel uploadModel = new UploadModel();
                    uploadModel.code = -1;
                    uploadModel.message = "token过期";
                    sendUploadState(uploadModel);
                }

                @Override
                public void onUploadRetry(String code, String message) {
                    //上传重试的提醒
                    Log.d(TAG, "onUploadRetry" + "code" + code + "message" + message);
                }

                @Override
                public void onUploadRetryResume() {
                    //上传重试成功的回调.告知用户重试成功
                    Log.d(TAG, "onUploadRetryResume");
                }
            });
        }
    }

    public void sendUploadState(UploadModel uploadModel) {
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("getUploadState", new Gson().toJson(uploadModel));
    }

    public void sendProgrtessState(long progress) {
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("getProgressState", "" + progress);
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this.reactContext.getCurrentActivity());
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.s(this.reactContext, "请给予相应的权限。");
        }
        return isPermissionOK;
    }


    /**
     * @author 何晏波
     * @QQ 1054539528
     * @date 2019/6/3
     * @function: 将多个小视频拼接为一个视频
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @ReactMethod
    public void mergeVideoToOneVideo(ReadableMap params, Callback successCallback) {
        final ArrayList videoPaths = (params.getArray("videoPaths")).toArrayList();
        final boolean isFront = params.getBoolean("isFront");
        String ouputFilePath = fixDegree(videoPaths, isFront);
        successCallback.invoke("file:///" + ouputFilePath);
    }


    /**
     * @author 何晏波
     * @QQ 1054539528
     * @date 2019-06-18
     * @function: 矫正视频角度
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private String fixDegree(ArrayList videoPaths, boolean isFront) {
        String mOutFilename = "";
        MediaMuxer mMuxer = null;
        if (videoPaths.size() != 0) {
            String path = videoPaths.get(0).toString();
            Log.e("TAG", "拼接视频路径" + path);
            mOutFilename = path.substring(0, path.lastIndexOf("/") + 1) + new Date().getTime() + ".mp4";
        }
        mReadBuf = ByteBuffer.allocate(1048576);
        boolean getAudioFormat = false;
        boolean getVideoFormat = false;
        Iterator videoIterator = videoPaths.iterator();

        //--------step 1 MediaExtractor拿到多媒体信息，用于MediaMuxer创建文件
        while (videoIterator.hasNext()) {
            String videoPath = (String) videoIterator.next();
            MediaExtractor extractor = new MediaExtractor();

            try {
                extractor.setDataSource(videoPath);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            int trackIndex;
            if (!getVideoFormat) {
                trackIndex = this.selectTrack(extractor, "video/");
                if (trackIndex < 0) {
                    Log.e(TAG, "No video track found in " + videoPath);
                } else {
                    extractor.selectTrack(trackIndex);
                    mVideoFormat = extractor.getTrackFormat(trackIndex);
                    getVideoFormat = true;
                }
            }

            if (!getAudioFormat) {
                trackIndex = this.selectTrack(extractor, "audio/");
                if (trackIndex < 0) {
                    Log.e(TAG, "No audio track found in " + videoPath);
                } else {
                    extractor.selectTrack(trackIndex);
                    mAudioFormat = extractor.getTrackFormat(trackIndex);
                    getAudioFormat = true;
                }
            }

            extractor.release();
            if (getVideoFormat && getAudioFormat) {
                break;
            }
        }

        try {
            mMuxer = new MediaMuxer(mOutFilename, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            //矫正视频拼接后视频顺时针旋转
//            if (isFront) {
//                mMuxer.setOrientationHint(270);
//            } else {
//                mMuxer.setOrientationHint(90);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (getVideoFormat) {
            mOutVideoTrackIndex = mMuxer.addTrack(mVideoFormat);
        }
        if (getAudioFormat) {
            mOutAudioTrackIndex = mMuxer.addTrack(mAudioFormat);
        }
        mMuxer.start();
        //--------step 1 end---------------------------//


        //--------step 2 遍历文件，MediaExtractor读取帧数据，MediaMuxer写入帧数据，并记录帧信息
        long ptsOffset = 0L;
        Iterator trackIndex = videoPaths.iterator();
        while (trackIndex.hasNext()) {
            String videoPath = (String) trackIndex.next();
            boolean hasVideo = true;
            boolean hasAudio = true;
            MediaExtractor videoExtractor = new MediaExtractor();

            try {
                videoExtractor.setDataSource(videoPath);
            } catch (Exception var27) {
                var27.printStackTrace();
            }

            int inVideoTrackIndex = this.selectTrack(videoExtractor, "video/");
            if (inVideoTrackIndex < 0) {
                hasVideo = false;
            }

            videoExtractor.selectTrack(inVideoTrackIndex);
            MediaExtractor audioExtractor = new MediaExtractor();

            try {
                audioExtractor.setDataSource(videoPath);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int inAudioTrackIndex = this.selectTrack(audioExtractor, "audio/");
            if (inAudioTrackIndex < 0) {
                hasAudio = false;
            }

            audioExtractor.selectTrack(inAudioTrackIndex);
            boolean bMediaDone = false;
            long presentationTimeUs = 0L;
            long audioPts = 0L;
            long videoPts = 0L;

            while (!bMediaDone) {
                if (!hasVideo && !hasAudio) {
                    break;
                }

                int outTrackIndex;
                MediaExtractor extractor;
                int currenttrackIndex;
                if ((!hasVideo || audioPts - videoPts <= 50000L) && hasAudio) {
                    currenttrackIndex = inAudioTrackIndex;
                    outTrackIndex = mOutAudioTrackIndex;
                    extractor = audioExtractor;
                } else {
                    currenttrackIndex = inVideoTrackIndex;
                    outTrackIndex = mOutVideoTrackIndex;
                    extractor = videoExtractor;
                }

                mReadBuf.rewind();
                int chunkSize = extractor.readSampleData(mReadBuf, 0);//读取帧数据
                if (chunkSize < 0) {
                    if (currenttrackIndex == inVideoTrackIndex) {
                        hasVideo = false;
                    } else if (currenttrackIndex == inAudioTrackIndex) {
                        hasAudio = false;
                    }
                } else {
                    if (extractor.getSampleTrackIndex() != currenttrackIndex) {
                        Log.e(TAG, "WEIRD: got sample from track " + extractor.getSampleTrackIndex() + ", expected " + currenttrackIndex);
                    }

                    presentationTimeUs = extractor.getSampleTime();//读取帧的pts
                    if (currenttrackIndex == inVideoTrackIndex) {
                        videoPts = presentationTimeUs;
                    } else {
                        audioPts = presentationTimeUs;
                    }

                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    info.offset = 0;
                    info.size = chunkSize;
                    info.presentationTimeUs = ptsOffset + presentationTimeUs;//pts重新计算
                    if ((extractor.getSampleFlags() & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                        info.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    }

                    mReadBuf.rewind();
                    Log.i(TAG, String.format("write sample track %d, size %d, pts %d flag %d", new Object[]{Integer.valueOf(outTrackIndex), Integer.valueOf(info.size), Long.valueOf(info.presentationTimeUs), Integer.valueOf(info.flags)}));
                    mMuxer.writeSampleData(outTrackIndex, mReadBuf, info);//写入文件
                    extractor.advance();
                }
            }

            //记录当前文件的最后一个pts，作为下一个文件的pts offset
            ptsOffset += videoPts > audioPts ? videoPts : audioPts;
            ptsOffset += 10000L;//前一个文件的最后一帧与后一个文件的第一帧，差10ms，只是估计值，不准确，但能用

            Log.i(TAG, "finish one file, ptsOffset " + ptsOffset);

            videoExtractor.release();
            audioExtractor.release();
        }

        if (mMuxer != null) {
            try {
                mMuxer.stop();
                mMuxer.release();
            } catch (Exception e) {
                Log.e(TAG, "Muxer close error. No data was written");
            }

            mMuxer = null;
        }

        return mOutFilename;
    }


    private int selectTrack(MediaExtractor extractor, String mimePrefix) {
        int numTracks = extractor.getTrackCount();

        for (int i = 0; i < numTracks; ++i) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString("mime");
            if (mime.startsWith(mimePrefix)) {
                return i;
            }
        }

        return -1;
    }
}
