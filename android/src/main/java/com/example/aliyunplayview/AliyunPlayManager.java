package com.example.aliyunplayview;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.alivc.player.VcPlayerLog;
import com.aliyun.vodplayer.media.AliyunVidSts;
import com.aliyun.vodplayer.media.AliyunVodPlayer;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;
import com.example.aliyunplayview.AliyunPlayerView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class AliyunPlayManager extends SimpleViewManager<AliyunPlayerView> {
    private static final String TAG = "AliyunPlayManager";
    public static final String REACT_CLASS = "AliyunPlay";
    private static final String PLAYING_CALLBACK = "onPlayingCallback";
    private static final String EVENT_CALLBACK = "onEventCallback";

    //视频画面
    private SurfaceView mSurfaceView;
    // 组件view
    private AliyunPlayerView mAliyunPlayerView;
    //播放器
    private AliyunVodPlayer mAliyunVodPlayer;
    // 播放进度计时器
    private ProgressUpdateTimer mProgressUpdateTimer;
    // 事件发送者
    private RCTEventEmitter mEventEmitter;
    private static final int VIDEO_PAUSE = 1;
    private static final int VIDEO_RESUME = 2;
    private static final int VIDEO_STOP = 3;
    private static final int VIDEO_SEEKTOTIME = 4;
    private static final int VIDEO_REPLAY = 5;

    @Override
    public String getName() {
        return REACT_CLASS;
    }


    @Override
    protected AliyunPlayerView createViewInstance(ThemedReactContext context) {
        Log.e("TAG", "组件创建了");
        //reactContext = context;
        mProgressUpdateTimer = null;
        mProgressUpdateTimer = new ProgressUpdateTimer(AliyunPlayManager.this);
        mEventEmitter = context.getJSModule(RCTEventEmitter.class);
        AliyunPlayerView view = new AliyunPlayerView(context);
        mAliyunPlayerView = view;

        mSurfaceView = new SurfaceView(context);
        view.addView(mSurfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();

        mAliyunVodPlayer = new AliyunVodPlayer(context);
        mAliyunVodPlayer.setDisplay(holder);
        mAliyunVodPlayer.setVideoScalingMode(IAliyunVodPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        //增加surfaceView的监听
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                VcPlayerLog.d(TAG, " surfaceCreated = surfaceHolder = " + surfaceHolder);
                mAliyunVodPlayer.setDisplay(surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width,
                                       int height) {
                VcPlayerLog.d(TAG, " surfaceChanged surfaceHolder = " + surfaceHolder + " ,  width = " + width + " , height = " + height);
                mAliyunVodPlayer.surfaceChanged();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                VcPlayerLog.d(TAG, " surfaceDestroyed = surfaceHolder = " + surfaceHolder);
            }
        });

        return view;
    }

    /**
     * 准备视频(异步)
     */
    @ReactProp(name = "prepareAsyncParams")
    public void setPrepareAsyncParams(AliyunPlayerView view, ReadableMap options) {

        String type = options.getString("type");

        switch (type) {
            // 使用vid+STS方式播放（点播用户推荐使用）
            case "vidSts":
                String vid = options.getString("vid");
                String accessKeyId = options.getString("accessKeyId");
                String accessKeySecret = options.getString("accessKeySecret");
                String securityToken = options.getString("securityToken");

                AliyunVidSts mVidSts = new AliyunVidSts();
                mVidSts.setVid(vid);
                mVidSts.setAcId(accessKeyId);
                mVidSts.setAkSceret(accessKeySecret);
                mVidSts.setSecurityToken(securityToken);

                if (mAliyunVodPlayer != null) {
                    mAliyunVodPlayer.prepareAsync(mVidSts);
                }
                break;

            default:
                Log.e(TAG, "prepareAsync" + type);
                break;
        }
    }


    @Override
    protected void addEventEmitters(ThemedReactContext reactContext, AliyunPlayerView view) {
        this.onListener(reactContext, view);
    }

    /**
     * 播放器监听事件
     *
     * @param reactContext
     * @param view
     */
    private void onListener(final ThemedReactContext reactContext, AliyunPlayerView view) {

        Log.e(TAG, "版本号" + AliyunVodPlayer.getSDKVersion());

        mAliyunVodPlayer.setOnPreparedListener(new IAliyunVodPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {
                Log.e(TAG, "onPrepared1" + mAliyunVodPlayer.getPlayerState().ordinal());
                mAliyunVodPlayer.start();
                //准备完成触发

                // TODO：待优化的 listener 处理，应该新建个独立文件处理？
                WritableMap body = Arguments.createMap();
                body.putInt("event", mAliyunVodPlayer.getPlayerState().ordinal());
                mEventEmitter.receiveEvent(mAliyunPlayerView.getId(), EVENT_CALLBACK, body);
            }
        });

        // 第一帧显示
        mAliyunVodPlayer.setOnFirstFrameStartListener(new IAliyunVodPlayer.OnFirstFrameStartListener() {
            @Override
            public void onFirstFrameStart() {
                Log.e(TAG, "onPrepared2" + mAliyunVodPlayer.getPlayerState().ordinal());
                // 开始启动更新进度的定时器
                startProgressUpdateTimer();
                // TODO：待优化的 listener 处理，应该新建个独立文件处理？
                WritableMap body = Arguments.createMap();
                body.putInt("event", mAliyunVodPlayer.getPlayerState().ordinal());
                mEventEmitter.receiveEvent(mAliyunPlayerView.getId(), EVENT_CALLBACK, body);
            }
        });

        mAliyunVodPlayer.setOnErrorListener(new IAliyunVodPlayer.OnErrorListener() {
            @Override
            public void onError(int errorCode, int errorEvent, String errorMsg) {
                Log.e(TAG, "onError" + errorMsg);
                // 停止定时器
                stopProgressUpdateTimer();
            }
        });

        mAliyunVodPlayer.setOnCompletionListener(new IAliyunVodPlayer.OnCompletionListener() {
            @Override
            public void onCompletion() {
                stopProgressUpdateTimer();
                mProgressUpdateTimer = null;
                mProgressUpdateTimer = new ProgressUpdateTimer(AliyunPlayManager.this);
                WritableMap body = Arguments.createMap();
                body.putInt("event", mAliyunVodPlayer.getPlayerState().ordinal());
                Log.e("TAG", "视频播放完成" + mAliyunVodPlayer.getPlayerState().ordinal());
                mEventEmitter.receiveEvent(mAliyunPlayerView.getId(), EVENT_CALLBACK, body);
            }
        });


    }

    /**
     * 开始播放进度计时器
     */
    private void startProgressUpdateTimer() {
        if (mProgressUpdateTimer != null) {
            mProgressUpdateTimer.removeMessages(0);
            mProgressUpdateTimer.sendEmptyMessageDelayed(0, 1000);
        }
    }

    /**
     * 停止播放进度计时器
     */
    private void stopProgressUpdateTimer() {
        if (mProgressUpdateTimer != null) {
            mProgressUpdateTimer.removeMessages(0);
        }
    }

    /**
     * 更新播放进度
     */
    private void handlePlayingMessage(Message message) {
        if (mAliyunVodPlayer != null) {
            long currentTime = mAliyunVodPlayer.getCurrentPosition();
            long duration = mAliyunVodPlayer.getDuration();
            WritableMap body = Arguments.createMap();
            body.putString("currentTime", currentTime + "");
            body.putString("duration", duration + "");
            mEventEmitter.receiveEvent(mAliyunPlayerView.getId(), PLAYING_CALLBACK, body);
        }

        startProgressUpdateTimer();
    }

    /**
     * 播放进度计时器
     */
    private static class ProgressUpdateTimer extends Handler {
        private WeakReference<AliyunPlayManager> managerWeakReference;

        ProgressUpdateTimer(AliyunPlayManager playManager) {
            managerWeakReference = new WeakReference<AliyunPlayManager>(playManager);
        }

        @Override
        public void handleMessage(Message msg) {
            AliyunPlayManager playManager = managerWeakReference.get();
            if (playManager != null) {
                playManager.handlePlayingMessage(msg);
            }
            super.handleMessage(msg);
        }
    }

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder builder = MapBuilder.builder();
        builder.put(EVENT_CALLBACK, MapBuilder.of("registrationName", EVENT_CALLBACK));
        builder.put(PLAYING_CALLBACK, MapBuilder.of("registrationName", PLAYING_CALLBACK));
        return builder.build();
    }


    @Override
    public void receiveCommand(AliyunPlayerView root, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case VIDEO_PAUSE:
                if (mAliyunVodPlayer != null &&
                        mAliyunVodPlayer.isPlaying()) {
                    mAliyunVodPlayer.pause();
                }
                break;
            case VIDEO_RESUME:
                if (mAliyunVodPlayer != null) {
                    mAliyunVodPlayer.resume();
                }
                break;
            case VIDEO_STOP:
                if (mAliyunVodPlayer != null) {
                    mAliyunVodPlayer.stop();
                }
                break;
            case VIDEO_SEEKTOTIME:
                if (mAliyunVodPlayer != null) {
                    mAliyunVodPlayer.seekTo(args.getInt(0));
                }
                break;
            case VIDEO_REPLAY:
                Log.e("TAG", "重新播放");
                if (mAliyunVodPlayer != null) {
                    mAliyunVodPlayer.replay();
                }
                break;
        }
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        Map<String, Integer> map = this.CreateMap(
                "pause", VIDEO_PAUSE,
                "resume", VIDEO_RESUME,
                "stop", VIDEO_STOP,
                "seekToTime", VIDEO_SEEKTOTIME,
                "rePlay", VIDEO_REPLAY
        );

        return map;
    }

    private <K, V> Map<K, V> CreateMap(
            K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map map = new HashMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return map;
    }

    @Override
    public void onDropViewInstance(AliyunPlayerView view) {
        Log.e("TAG", "组件销毁了");
        mProgressUpdateTimer = null;
        mAliyunVodPlayer.stop();
        mAliyunVodPlayer.release();
        mAliyunVodPlayer = null;
        view.destroyDrawingCache();
        super.onDropViewInstance(view);
    }
}
