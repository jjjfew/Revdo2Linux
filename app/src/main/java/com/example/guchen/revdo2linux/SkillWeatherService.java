package com.example.guchen.revdo2linux;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class SkillWeatherService extends Service {
    String httpUrl = "http://apis.baidu.com/heweather/pro/attractions";
    String httpArg = "cityid=CN10101010018A";
    private String resultJson=null;
    private String tempt=null;
    public static Timer timer;//定时器，用于发送天气信息

    public SkillWeatherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w("XXX", "onCreate");

        //联网显示温度
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(isNetwork()) {
                    resultJson = requestWeather(httpUrl, httpArg);
                    Log.w("xxx",resultJson);
                    if(resultJson!=null) {  //不判断是否得到数据，解析错误会闪退
                        try {
                            JSONObject jsonParser = new JSONObject(resultJson);
                            JSONArray jsonArray = jsonParser.getJSONArray("HeWeather data service 3.0");
                            JSONObject today = jsonArray.getJSONObject(0).getJSONArray("daily_forecast").getJSONObject(0);
                            tempt= today.getJSONObject("tmp").getString("min");  //今天的最低温度
                            Log.w("XXX", "max:"+tempt);
                            Message message=new Message();
                            message.what = 333;
                            message.obj = "weather:"+tempt;
                            MainActivity.handlerWeather.sendMessage(message);
                            //每隔5秒发送天所到服务器
                            timer=new Timer();
                            TimerTask timerTask=new TimerTask() {
                                @Override
                                public void run() {
                                    Log.w("XXX","send weather to server...");
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SocketClient socketClient=new SocketClient(MainActivity.serverIP);
                                            socketClient.sendToServer("weather:"+tempt);
                                        }
                                    }).start();
                                }
                            };
                            timer.schedule(timerTask,1000,5000);//1秒后执行一次，接着按5秒间隔循环执行
                        } catch (JSONException e) {
                            //可能在下午时没有max信息，就显示null
                            Message message=new Message();
                            message.what = 333;
                            message.obj = "weather:"+null;
                            MainActivity.handlerWeather.sendMessage(message);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    SocketClient socketClient=new SocketClient(MainActivity.serverIP);
                                    socketClient.sendToServer("weather:"+tempt);
                                }
                            }).start();
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }

    public static String requestWeather(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        String result=null;
        httpUrl = httpUrl + "?" + httpArg;
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey",  "eb85b80f6388242d239b31fc3a1d888b");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    boolean isNetwork(){
        ConnectivityManager mConnectivityManager;
        NetworkInfo mNetworkInfo;
        boolean ret=false;
        try {
            mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo!=null) {
                if (mNetworkInfo.isAvailable())
                    Log.w("XXX", "当前网络可用");
                ret= true;
            } else
                Log.w("XXX", "当前网络不可用");
        }catch (Exception e){}
        return ret;
    }
}
