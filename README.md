# Cordova Baidu ASR 语音识别插件
================================

百度云ASR语音识别的cordova插件，支持iOS和android，仅在线识别。


## Installation


1、Run

    cordova plugin add cordova-baidu-asr --variable APP_ID=<百度云提供> --variable API_KEY=<百度云提供> --variable SECRET_KEY=<百度云提供>


### Supported Platforms

- Android
- iOS


### Using the plugin ###

在线识别:
```js
    BaiduASR.startOnline((result)=>{
            console.log(JSON.stringify(result));
        });
```
