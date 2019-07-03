
package com.example.aliyunplayview;


import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

public class RNAliyunPlayerModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public RNAliyunPlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNAliyunPlayer";
    }
}