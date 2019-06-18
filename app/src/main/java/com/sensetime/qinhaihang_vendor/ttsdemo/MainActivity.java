package com.sensetime.qinhaihang_vendor.ttsdemo;

import android.Manifest;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.sensetime.qinhaihang_vendor.qhhpermissionutils.PermissionHelper;
import com.sensetime.qinhaihang_vendor.qhhpermissionutils.callback.ICallbackManager;
import com.sensetime.qinhaihang_vendor.ttsdemo.util.OfflineResource;

import java.io.File;
import java.io.IOException;

import static com.sensetime.qinhaihang_vendor.ttsdemo.Config.MODEL_FILENAME;
import static com.sensetime.qinhaihang_vendor.ttsdemo.Config.TEMP_DIR;
import static com.sensetime.qinhaihang_vendor.ttsdemo.Config.TEXT_FILENAME;
import static com.sensetime.qinhaihang_vendor.ttsdemo.Config.offlineVoice;
import static com.sensetime.qinhaihang_vendor.ttsdemo.Config.ttsMode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mInput;
    private SpeechSynthesizer mSpeechSynthesizer;
    private OfflineResource mOfflineResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestPermission();
    }

    private void initView() {
        findViewById(R.id.btn_combine).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        mInput = findViewById(R.id.et_input);
    }

    private void requestPermission() {
        PermissionHelper.getInstance().init(this)
                .setmRequestCallback(new ICallbackManager.IRequestCallback() {
                    @Override
                    public void onAllPermissonGranted(boolean flag) {
                        initTTS();
                    }
                })
                .checkPermission(
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_SETTINGS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE
                );

    }

    private void initTTS() {

        boolean isMix = ttsMode.equals(TtsMode.MIX);
        boolean isSuccess;
        if (isMix) {
            // 检查2个离线资源是否可读
            isSuccess = checkOfflineResources();
            if (!isSuccess) {
                Log.d("qhh","开始拷贝资源");
                mOfflineResource = createOfflineResource(offlineVoice);
            } else {
                mOfflineResource = new OfflineResource(this);
                Log.d("qhh","离线资源存在并且可读, 目录：" + TEMP_DIR);
//                print("离线资源存在并且可读, 目录：" + TEMP_DIR);
            }
        }

        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(this);

        int result = mSpeechSynthesizer.setAppId(Config.appId);
        Log.d("qhh","setAppId result = " + result);
        result = mSpeechSynthesizer.setApiKey(Config.appKey, Config.secretKey);
        Log.d("qhh","setApiKey result = " + result);



        // 4. 支持离线的话，需要设置离线模型
        if (isMix) {
            // 检查离线授权文件是否下载成功，离线授权文件联网时SDK自动下载管理，有效期3年，3年后的最后一个月自动更新。
            isSuccess = checkAuth();
            if (!isSuccess) {
                return;
            }
            // 文本模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            Log.i("qhh","TextFilename = "+mOfflineResource.getTextFilename());
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mOfflineResource.getTextFilename());
            // 声学模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            Log.i("qhh","ModelFilename = "+mOfflineResource.getModelFilename());
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mOfflineResource.getModelFilename(offlineVoice));
        }

        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);

        // 6. 初始化
        result = mSpeechSynthesizer.initTts(ttsMode);
        Log.d("qhh","initTts result = " + result);
    }

    /**
     * 检查 TEXT_FILENAME, MODEL_FILENAME 这2个文件是否存在，不存在请自行从assets目录里手动复制
     *
     * @return
     */
    private boolean checkOfflineResources() {
        String[] filenames = {TEXT_FILENAME, MODEL_FILENAME};
        for (String path : filenames) {
            File f = new File(path);
            if (!f.canRead()) {
                //print("[ERROR] 文件不存在或者不可读取，请从assets目录复制同名文件到：" + path);
                //print("[ERROR] 初始化失败！！！");
                Log.d("qhh","[ERROR] 文件不存在或者不可读取，请从assets目录复制同名文件到：" + path);
                return false;
            }
        }
        return true;
    }

    /**
     * 检查appId ak sk 是否填写正确，另外检查官网应用内设置的包名是否与运行时的包名一致。本demo的包名定义在build.gradle文件中
     *
     * @return
     */
    private boolean checkAuth() {
        AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            //print("【error】鉴权失败 errorMsg=" + errorMsg);
            Log.d("qhh","【error】鉴权失败 errorMsg=" + errorMsg);
            Toast.makeText(this,"【error】鉴权失败 errorMsg=" + errorMsg,Toast.LENGTH_SHORT).show();
            return false;
        } else {
            //print("验证通过，离线正式授权文件存在。");
            Log.d("qhh","验证通过，离线正式授权文件存在。");
            return true;
        }
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            Log.e("qhh","【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }

    private void speak() {
        /* 以下参数每次合成时都可以修改
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
         *  设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5"); 设置合成的音量，0-9 ，默认 5
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5"); 设置合成的语速，0-9 ，默认 5
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5"); 设置合成的语调，0-9 ，默认 5
         *
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
         *  MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
         *  MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
         *  MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
         *  MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
         */
        if (mSpeechSynthesizer == null) {
            Log.d("qhh","[ERROR], 初始化失败");
            Toast.makeText(this,"[ERROR], 初始化失败",Toast.LENGTH_SHORT).show();
            return;
        }

        String text = mInput.getText().toString().trim();

        int result = mSpeechSynthesizer.speak(text);
        Log.d("qhh","speak result = " + result);
    }

    private void stop() {
        Log.d("qhh","停止合成引擎 按钮已经点击");
        int result = mSpeechSynthesizer.stop();
        Log.d("qhh","stop result = " + result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_combine:
                speak();
                break;
            case R.id.btn_cancel:
                stop();
                break;
            default:
                break;
        }
    }
}
