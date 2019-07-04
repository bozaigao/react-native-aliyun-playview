# react-native-aliyun-playview
封装阿里云点播播放器与短视频上传功能，支持视频播放过程中stop()、pause()、resume()、reset()、rePlay()、seekToTime()等Api调用.

### 安装方法
执行npm i react-native-aliyun-playview --save安装组件

react-native link react-native-aliyun-playview链接Android和iOS原生模块
#### Android端额外配置
在项目app build.gradle做以下配置,然后clean build运行
```java
repositories {
    mavenCentral()
    flatDir {
        dirs 'libs','../../node_modules/react-native-aliyun-playview/android/libs' //this way we can find the .aar file in libs folder
    }
}
```
#### iOS端额外配置
选中项目TARGETS=>Embedded Binaries将react-native-aliyun-playview/ios/AliyunSDK目录中的AliThirdparty.framework、AliyunPlayerSDK.framework、AliyunVodPlayerSDK.framework
动态库文件拖入其中，将AliyunLanguageSource.bundle资源包拖入Build Phases=>Copy Bundle Resources中,然后将AliThirdparty.framework、AliyunPlayerSDK.framework、AliyunVodPlayerSDK.framework
同时拖入Pods=>Frameworks=>iOS目录下，不执行该操作，pod依赖里面的react-native-aliyun-playview会提示找不到相关文件，最后进入项目ios目录执行pod install命令完成所有依赖.

![iOS配置1](./iOS_step1.jpeg)   ![iOS配置2](./iOS_step2.jpeg)   ![iOS配置3](./iOS_step3.jpeg)
