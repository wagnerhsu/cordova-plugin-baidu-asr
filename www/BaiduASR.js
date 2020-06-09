var exec = cordova.require("cordova/exec");

var BaiduASR = {
    /**
     * 在线识别
     * @param successCallback
     * @param errorCallback
     */
    startOnline: function(successCallback, errorCallback) {

        if (errorCallback == null) {
            errorCallback = function() {};
        }

        if (typeof successCallback !== "function") {
            console.log("BaiduASR.startOnline failure: success callback parameter must be a function");
            return;
        }

        exec(successCallback, errorCallback, 'BaiduASR', 'startOnline', []);
    },
    /**
     * 长语音
     * @param successCallback
     * @param errorCallback
     */
    startLong: function(successCallback, errorCallback) {

        if (errorCallback == null) {
            errorCallback = function() {};
        }

        if (typeof successCallback !== "function") {
            console.log("BaiduASR.startLong failure: success callback parameter must be a function");
            return;
        }

        exec(successCallback, errorCallback, 'BaiduASR', 'startLong', []);
    },
    /**
     * 结束长语音
     * @param successCallback
     * @param errorCallback
     */
    stopLong: function(successCallback, errorCallback) {

        if (errorCallback == null) {
            errorCallback = function() {};
        }

        exec(successCallback, errorCallback, 'BaiduASR', 'stopLong', []);
    },

};

module.exports = BaiduASR;