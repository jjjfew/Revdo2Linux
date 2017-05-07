package com.example.guchen.revdo2linux;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

public class SmsReadService extends Service {
    String TAG="xxx";
    String msg;  //合成的短信消息
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认云端发音人
    public static String voicerCloud="xiaoyan";

    public SmsReadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.w("XXX","onCreate");
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.w("XXX","onStartCommand");

//Step1. 加载appid对应的so文件
        StringBuffer param = new StringBuffer();
        param.append("appid="+WakeupService.myappid);
        param.append(",");
// 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(this, param.toString());

//Step2. 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);

        if (intent != null) {
            String address = intent.getStringExtra("sms_address");
            if (address != null) {
                //textView.append("\n\n发件人：\n" + address);
                String bodyString = intent.getStringExtra("sms_body");
                if (bodyString != null) {
                   // textView.append("\n短信内容：\n" + bodyString);
                }
                msg="发件人"+address+"短信内容"+bodyString;
                showTip(msg);
                Log.w(TAG,msg);
                setParam();
                mTts.startSpeaking(msg, mTtsListener);
            }
        }

        return START_STICKY;
    }

    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                //showTip("初始化失败,错误码："+code);
                Log.w(TAG,"初始化失败,错误码："+code);
                //Toast.makeText(this,"初始化失败,错误码："+code,Toast.LENGTH_SHORT).show();
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            Log.w(TAG,"开始播放");
            //showTip("开始播放");
        }
        @Override
        public void onSpeakPaused() {
            Log.w(TAG,"暂停播放");
            //showTip("暂停播放");
        }
        @Override
        public void onSpeakResumed() {
            Log.w(TAG,"继续播放");
            //showTip("继续播放");
        }
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            // mPercentForBuffering = percent;
            // showTip(String.format(getString(R.string.tts_toast_format),
            //         mPercentForBuffering, mPercentForPlaying));
        }
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            //mPercentForPlaying = percent;
            //showTip(String.format(getString(R.string.tts_toast_format),
            //        mPercentForBuffering, mPercentForPlaying));
        }
        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                //showTip("播放完成");
                Log.w(TAG,"播放完成");
            } else if (error != null) {
                //showTip(error.getPlainDescription(true));
                Log.w(TAG,error.getPlainDescription(true));
            }
        }
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            // if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //    String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //    Log.d(TAG, "session id =" + sid);
            // }
        }
    };

    private void setParam(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //设置合成
        //设置使用云端引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME,voicerCloud);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }

    private void showTip(final String str){
                Toast mToast = Toast.makeText(SmsReadService.this,"",Toast.LENGTH_SHORT);
                mToast.setText(str);
                mToast.show();
    }

}
