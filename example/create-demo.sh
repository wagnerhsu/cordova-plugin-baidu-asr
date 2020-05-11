cordova create test_asr io.hankers.hanzi5 TEST_ASR
cd test_asr
cordova platform add ios android
cordova plugin add cordova-plugin-device --searchpath ../../../../../cordova/plugins
cordova plugin add cordova-plugin-ionic-webview --searchpath ../../../../../cordova/plugins
cordova plugin add cordova-baidu-asr --searchpath ../../.. --variable APP_ID=19574551 --variable API_KEY=GOUXCYGHzYY7M2tQNzs9Cvfg --variable SECRET_KEY=lRDy4CbY8QFEWRUMCtNgxcf95OXDZM6t
cp -r ../index.html ./www/
cp ../config.xml .
cordova prepare
