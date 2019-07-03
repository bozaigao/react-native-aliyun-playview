import React, { Component } from 'react';
import { findNodeHandle, requireNativeComponent, UIManager } from 'react-native';
const AliyunPlayer = requireNativeComponent('AliyunPlay', AliyunPlayView);
export default class AliyunPlayView extends Component {
    constructor() {
        super(...arguments);
        this.stop = () => {
            this.sendCommand("stop");
        };
        this.pause = () => {
            this.sendCommand("pause");
        };
        this.resume = () => {
            this.sendCommand("resume");
        };
        this.reset = () => {
            this.sendCommand("reset");
        };
        this.rePlay = () => {
            this.sendCommand("rePlay");
        };
        this.seekToTime = (time) => {
            this.sendCommand("seekToTime", [time]);
        };
    }
    sendCommand(command, params = []) {
        UIManager.dispatchViewManagerCommand(findNodeHandle(this), UIManager['AliyunPlay'].Commands[command], params);
    }
    render() {
        return <AliyunPlayer {...this.props}/>;
    }
}
//# sourceMappingURL=AliyunPlayView.js.map