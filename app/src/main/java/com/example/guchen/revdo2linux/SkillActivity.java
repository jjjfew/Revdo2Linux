package com.example.guchen.revdo2linux;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.ListView;

import static com.example.guchen.revdo2linux.MainActivity.mSharedPreferences;
import static com.example.guchen.revdo2linux.MainActivity.serverIP;
import static com.example.guchen.revdo2linux.SkillWeatherService.timer;

public class SkillActivity extends AppCompatActivity {
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill);

        mListView = (ListView) findViewById(R.id.listView);

        SkillAdapter myadapter=new SkillAdapter(this);
        mListView.setAdapter(myadapter);

        /*
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                Intent intent = new Intent();
                intent.setClass(SkillActivity.this, SkillInfoActivity.class);
                Bundle bundle = new Bundle();
                if (arg2 == 0)
                    bundle.putString("name", "显示天气");
                else if (arg2 == 1)
                    bundle.putString("name", "车辆跟踪");
                else if (arg2 == 2)
                    bundle.putString("name", "语音消息");
                else if (arg2 == 3)
                    bundle.putString("name", "自动挂断");
                else if (arg2 == 4)
                    bundle.putString("name", "语音拨号");
                intent.putExtras(bundle);
                startActivity(intent);

                if(mListView.isItemChecked(0)==true) {
                    edit.putBoolean("weatherEnable", true);
                    edit.commit(); //更新数据
                        //启动显示天气服务
                        Intent intent2 = new Intent(SkillActivity.this, SkillWeatherService.class);
                        startService(intent2);
                }else {
                    edit.putBoolean("weatherEnable", false);
                    edit.commit(); //更新数据
                        //关闭显示天气服务
                        Intent intent2 = new Intent(SkillActivity.this, SkillWeatherService.class);
                        stopService(intent2);
                        SkillWeatherService.timer.cancel();//必须同时关掉定时器，否则timer还会发送
                    //清空镜像上之前显示的天气
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SocketClient socketClient=new SocketClient(MainActivity.serverIP);
                            socketClient.sendToServer("weather:"+"close");
                        }
                    }).start();
                    //清空手机客户端首页上的天气信息
                    Message message=new Message();
                    message.what = 333;
                    message.obj = "";
                    MainActivity.handlerWeather.sendMessage(message);
                }

                if(mListView.isItemChecked(2)==true) {
                    edit.putBoolean("smsReadEnable", true);
                    edit.commit(); //更新数据
                }else {
                    edit.putBoolean("smsReadEnable", false);
                    edit.commit(); //更新数据
                }

                //显示被选中的item总数
                Log.w("xxx",mListView.getCheckedItemCount()+"");

            }
        });
        */

    }
}
