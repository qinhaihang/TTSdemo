package com.sensetime.qinhaihang_vendor.ttsdemo;

import com.baidu.tts.client.TtsMode;
import com.sensetime.qinhaihang_vendor.ttsdemo.util.OfflineResource;

/**
 * @author qinhaihang_vendor
 * @version $Rev$
 * @time 2019/2/20 15:54
 * @des
 * @packgename com.sensetime.qinhaihang_vendor.ttsdemo
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes
 */
public class Config {
    //自己demo的应用key
    public static final String appId = "15594045";

    public static final String appKey = "aZdoiixarWpjr1pS4oGp814f";

    public static final String secretKey = "ClhQU47P9qTXFpDhc54rVBkXU5PGBiFN";

    //百度demo的key
//    public static final  String appId = "11005757";
//
//    public static final  String appKey = "Ovcz19MGzIKoDDb3IsFFncG1";
//
//    public static final  String secretKey = "e72ebb6d43387fc7f85205ca7e6706e2";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    public static final TtsMode ttsMode = TtsMode.MIX;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    public static final String offlineVoice = OfflineResource.VOICE_MALE;

    public static final String TEMP_DIR = "/sdcard/baiduTTS"; // 重要！请手动将assets目录下的3个dat 文件复制到该目录

    // 请确保该PATH下有这个文件
    public static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";

    // 请确保该PATH下有这个文件 ，m15是离线男声
    public static final String MODEL_FILENAME =
            TEMP_DIR + "/" + "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";
}
