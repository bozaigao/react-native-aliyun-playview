/**
 * @filename AliyunPlayView.tsx
 * @author 何晏波
 * @QQ 1054539528
 * @date 2019/5/15
 * @Description: 封装阿里云点播播放器
 */
import React, {Component} from 'react'
import {findNodeHandle, requireNativeComponent, UIManager} from 'react-native'
//@ts-ignore
const AliyunPlayer = requireNativeComponent('AliyunPlay', AliyunPlayView);

interface Props {
    style: any;
    prepareAsyncParams: any;
    onEventCallback?: any;
    onPlayingCallback?: any;
}

export default class AliyunPlayView extends Component<Props> {


    /**
     *  功能：停止播放视频
     */
    stop = () => {
        this.sendCommand("stop");
    }

    /**
     *  功能：暂停播放视频
     *  备注：在start播放视频之后可以调用pause进行暂停。
     */
    pause = () => {
        this.sendCommand("pause");
    }

    /**
     *   功能：恢复播放视频
     *   备注：在pause暂停视频之后可以调用resume进行播放。
     */
    resume = () => {
        this.sendCommand("resume");
    }

    /**
     *   功能：恢复播放视频
     *   备注：重置播放器。
     */
    reset = () => {
        this.sendCommand("reset");
    }

    /**
     *   功能：恢复播放视频
     *   备注：重置播放器。
     */
    rePlay = () => {
        this.sendCommand("rePlay");
    }

    /**
     *  功能：跳转到指定位置进行播放，单位为秒
     * @param time
     */
    seekToTime = (time) => {
        this.sendCommand("seekToTime", [time]);
    }

    /**
     * 调用原生方法
     *
     * @private
     */
    sendCommand(command, params = []) {
        //这里适配使用的react-native版本是0.51.0,如果是高版本的话这里记得修改一下
        //@ts-ignore
        UIManager.dispatchViewManagerCommand(
            findNodeHandle(this),
            UIManager['AliyunPlay'].Commands[command],
            params
        );
    }

    render() {
        return <AliyunPlayer {...this.props} />
    }
}
