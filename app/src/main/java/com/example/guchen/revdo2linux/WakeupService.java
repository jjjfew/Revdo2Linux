package com.example.guchen.revdo2linux;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class WakeupService extends Service {
    public static String myappid="58e60ddd";
    private VoiceWakeuper mIvw;
    private int curThresh = 10;
    // 唤醒结果内容
    private String resultString;

    public WakeupService() {
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

        //以下是所有讯飞语音都要初始化用到的
        StringBuffer param1 = new StringBuffer();
        // 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符
        param1.append("appid="+myappid);
        // 参数间使用半角“,”分隔。
        param1.append(",");
        // 设置使用v5+
        param1.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC); //不加这个默认会使用讯飞语记SpeechConstant.MODE_PLUS
        param1.append(",");
        // SpeechUtility接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        param1.append(SpeechConstant.FORCE_LOGIN+"=true");
        SpeechUtility.createUtility(this, param1.toString());
        // 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
        // Setting.setShowLog(false);

        //以下只和唤醒相关
        //1.加载唤醒词资源，resPath为唤醒资源路径
        StringBuffer param = new StringBuffer();
        String resPath = ResourceUtil.generateResourcePath(WakeupService.this,
                ResourceUtil.RESOURCE_TYPE.assets, "ivw/"+myappid+".jet");
        param.append(ResourceUtil.IVW_RES_PATH + "=" + resPath);
        param.append("," + ResourceUtil.ENGINE_START + "=" + SpeechConstant.ENG_IVW);

        boolean st = SpeechUtility.getUtility().setParameter(ResourceUtil.ENGINE_START, param.toString());

        if (!st) {
            //tv1.setText("启动本地引擎失败");
            Log.w("xxx","启动本地引擎失败");
        } else {
            // tv1.setText("启动本地引擎成功");
            Log.w("xxx","启动本地引擎成功");
        }


        //2.创建VoiceWakeuper对象
        mIvw = VoiceWakeuper.createWakeuper(WakeupService.this, null);

        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            //3.设置唤醒参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
            //唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "1:" + curThresh);
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "2:" + curThresh);
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "3:" + curThresh);
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "4:" + curThresh);

            //设置当前业务类型为唤醒
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            //设置唤醒一直保持，直到调用stopListening，传入0则完成一次唤醒后，会话立即结束（默认0）
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            //4.开始唤醒
            int ret = mIvw.startListening(mWakeuperListener);
            if (ret != 0) {
                //tv1.setText("语音唤醒失败,错误码:" + ret);
                Log.w("xxx","语音唤醒失败,错误码:" + ret);
            } else {
                //tv1.setText("语音唤醒启动成功");
                Log.w("xxx","语音唤醒启动成功");
            }
        } else
            //tv1.setText("唤醒未初始化");
            Log.w("xxx","唤醒未初始化");
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.w("XXX","onStartCommand");



        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onCreate();
        Log.w("XXX","onDestroy");
    }

    //听写监听器
    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);

                StringBuffer buffer = new StringBuffer();
                buffer.append(object.optString("id"));
                resultString =buffer.toString();
                Toast.makeText(WakeupService.this, resultString, Toast.LENGTH_SHORT).show();
                //mIvw.stopListening();
                wakeActivity(resultString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(SpeechError error) {
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if (SpeechEvent.EVENT_IVW_RESULT == eventType) {
                //当使用唤醒+识别功能时获取识别结果
                //arg1:是否最后一个结果，1:是，0:否。
                RecognizerResult reslut = ((RecognizerResult) obj.get(SpeechEvent.KEY_EVENT_IVW_RESULT));
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };

    private String getResource() {
        return ResourceUtil.generateResourcePath(WakeupService.this,
                ResourceUtil.RESOURCE_TYPE.assets, "ivw/"+myappid+".jet");
    }

    public void wakeActivity(String output){
        if (output.equals("0")) {   //芝麻开门，返回桌面
            Log.w("xxx","返回桌面");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SocketClient socketClient=new SocketClient(MainActivity.serverIP);
                    socketClient.sendToServer("start:home");
                }
            }).start();
        }else if (output.equals("1")) {  //打开导航
            Log.w("xxx","打开导航");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SocketClient socketClient=new SocketClient(MainActivity.serverIP);
                    socketClient.sendToServer("start:navi");
                }
            }).start();
        }else if (output.equals("2")) {  //打开摄像
            Log.w("xxx","打开摄像");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SocketClient socketClient=new SocketClient(MainActivity.serverIP);
                    socketClient.sendToServer("start:camera");
                }
            }).start();
        } else if (output.equals("3")) {  //连接手机
            Log.w("xxx","连接手机");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SocketClient socketClient=new SocketClient(MainActivity.serverIP);
                    socketClient.sendToServer("start:connect");
                }
            }).start();
        } else if (output.equals("4")) {  //接听电话
            Log.w("xxx","接听电话");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SocketClient socketClient=new SocketClient(MainActivity.serverIP);
                    socketClient.sendToServer("start:phone");
                }
            }).start();
        }
    }
}
