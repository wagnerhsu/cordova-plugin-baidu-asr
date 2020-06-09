package com.baidu.cordova;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.ChainRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.baidu.voicerecognition.android.ui.DigitalDialogInput;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BaiduASR extends CordovaPlugin {

    private static String TAG = BaiduASR.class.getSimpleName();

    private CallbackContext mCallback;
    private static Handler handler;
    private static ChainRecogListener chainRecogListener;
    private static MyRecognizer myRecognizer;
    private DigitalDialogInput input;
    private final static int REQUEST_CODE_RECOGNIZE_ONLINE = 2;
    private boolean enableOffline = false;
    private boolean isStartingLong = false;

    public BaiduASR() {

    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        initPermission();

        if (myRecognizer == null) {
            handler = new Handler() {

                /*
                 * @param msg
                 */
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    handleMsg(msg);
                }

            };
            Handler emptyHandler = new Handler() {

                /*
                 * @param msg
                 */
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }

            };
            try {
                IRecogListener listener = new MessageStatusRecogListener(emptyHandler);
                myRecognizer = new MyRecognizer(android.os.Build.VERSION.SDK_INT >= 21 ? cordova.getActivity().getWindow().getContext() : cordova.getActivity().getApplicationContext(), listener);
//        if (enableOffline) {
//            // 基于DEMO集成1.4 加载离线资源步骤(离线时使用)。offlineParams是固定值，复制到您的代码里即可
//            Map<String, Object> offlineParams = OfflineRecogParams.fetchOfflineParams();
//            myRecognizer.loadOfflineEngine(offlineParams);
//        }

                chainRecogListener = new ChainRecogListener();
                // DigitalDialogInput 输入 ，MessageStatusRecogListener可替换为用户自己业务逻辑的listener
                chainRecogListener.addListener(new MessageStatusRecogListener(handler));

                myRecognizer.setEventListener(chainRecogListener); // 替换掉原来的listener

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    protected void handleMsg(Message msg) {
        Log.d(TAG, msg.toString());
        try {
            if (isStartingLong) {
                if (msg.what == 6) {
                    JSONObject ori = new JSONObject(msg.obj.toString());
                    if (ori.has("final_result")) {
                        JSONObject ret = new JSONObject();
                        try {
                            ret.put("result", ori.get("final_result"));
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
//                    mCallback.success(ret);
                        PluginResult rst = new PluginResult(PluginResult.Status.OK, ret);
                        rst.setKeepCallback(true);
                        mCallback.sendPluginResult(rst);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback id used when calling back into JavaScript.
     * @return True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        cordova.getThreadPool().execute(() -> {
            try {
                Method method = BaiduASR.class.getDeclaredMethod(action, JSONArray.class, CallbackContext.class);
                method.invoke(BaiduASR.this, args, callbackContext);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        });
        return true;
    }

    /**
     * 在线识别
     *
     * @param data
     * @param callbackContext
     * @throws JSONException
     */
    void startOnline(JSONArray data, CallbackContext callbackContext) throws JSONException {

        final Map<String, Object> params = new HashMap<>();

        // BaiduASRDigitalDialog的输入参数
        input = new DigitalDialogInput(myRecognizer, chainRecogListener, params);
        BaiduASRDigitalDialog.setInput(input); // 传递input信息，在BaiduASRDialog中读取,
        Intent intent = new Intent(cordova.getActivity(), BaiduASRDigitalDialog.class);

        // 修改对话框样式
        // intent.putExtra(BaiduASRDigitalDialog.PARAM_DIALOG_THEME, BaiduASRDigitalDialog.THEME_ORANGE_DEEPBG);

        //设置回调
        mCallback = callbackContext;

        //把当前plugin设置为startActivityForResult的返回 ****重要****
        cordova.setActivityResultCallback(this);
        //启动扫描页面
        cordova.getActivity().startActivityForResult(intent, REQUEST_CODE_RECOGNIZE_ONLINE);
    }

    /**
     * 长语音
     *
     * @param data
     * @param callbackContext
     * @throws JSONException
     */
    void startLong(JSONArray data, CallbackContext callbackContext) throws JSONException {

        final Map<String, Object> params = new HashMap<>();

        params.put("vad.endpoint-timeout", "0");
        params.put("enable.numberformat", "true");
        params.put("accept-audio-volume", "false");

        Log.i(TAG, "设置的start输入参数：" + params);
        // 复制此段可以自动检测常规错误
        Context ctx = android.os.Build.VERSION.SDK_INT >= 21 ? cordova.getActivity().getWindow().getContext() : cordova.getActivity().getApplicationContext();
        (new AutoCheck(ctx, new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        ; // 可以用下面一行替代，在logcat中查看代码
                         Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, enableOffline)).checkAsr(params);

        // 这里打印出params， 填写至您自己的app中，直接调用下面这行代码即可。
        // DEMO集成步骤2.2 开始识别
        myRecognizer.start(params);
        isStartingLong = true;

        //设置回调
        mCallback = callbackContext;
    }

    /**
     * 结束长语音
     *
     * @param data
     * @param callbackContext
     * @throws JSONException
     */
    void stopLong(JSONArray data, CallbackContext callbackContext) throws JSONException {

        myRecognizer.stop();
        isStartingLong = false;

        //设置回调
        mCallback = null;
        callbackContext.success(new JSONObject());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_RECOGNIZE_ONLINE) {
                ArrayList results = data.getStringArrayListExtra("results");
                String result = (results != null && results.size() > 0) ? ((String)results.get(0)) : "";
                Log.d(TAG, result);
                JSONObject ret = new JSONObject();
                try {
                    ret.put("result", result);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                mCallback.success(ret);
            }
        }

    }

    private void initPermission() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                /* 下面是蓝牙用的，可以不申请
                Manifest.permission.BROADCAST_STICKY,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
                */
        };

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(cordova.getContext(), perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(cordova.getActivity(), toApplyList.toArray(tmpList), 123);
        }

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }


}
