# Cordova Baidu ASR 语音识别插件
================================

百度云ASR语音识别的cordova插件，支持iOS和android，仅在线识别。

由于iOS的静态库太大，仅保留 arm64 部分，删掉了 armv7, i386, x86_64，故只能在真机上调试。


## Installation

    cordova plugin add cordova-baidu-asr --variable APP_ID=<百度云提供> --variable API_KEY=<百度云提供> --variable SECRET_KEY=<百度云提供>


### Usage

在线识别:
```js
    BaiduASR.startOnline((result)=>{
            console.log(JSON.stringify(result));
        });
```
