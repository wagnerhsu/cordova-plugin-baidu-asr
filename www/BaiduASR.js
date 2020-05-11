/**
 * cordova is available under the MIT License (2008).
 * See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) Matt Kane 2010
 * Copyright (c) 2011, IBM Corporation
 * Copyright (c) 2012-2017, Adobe Systems
 */


var exec = cordova.require("cordova/exec");


var BaiduASR = {
    /**
     * 在线识别
     * @param successCallback
     * @param errorCallback
     */
    startOnline: function (successCallback, errorCallback) {

        if (errorCallback == null) {
            errorCallback = function () {
            };
        }

        if (typeof errorCallback !== "function") {
            console.log("BaiduASR.startOnline failure: failure parameter not a function");
            return;
        }

        if (typeof successCallback !== "function") {
            console.log("BaiduASR.startOnline failure: success callback parameter must be a function");
            return;
        }

        exec(successCallback, errorCallback, 'BaiduASR', 'startOnline', []);
    },
};

module.exports = BaiduASR;
