package com.example.guchen.revdo2linux;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.guchen.revdo2linux.AmapNavi.MapActivity;

public class MainActivity extends AppCompatActivity {
    public static SharedPreferences mSharedPreferences=null;
    private Button buttonNavi,buttonSkill,buttonConnect;
    private TextView textView;
    private int i=0;
    public static Handler handlerWeather;
    //public static String serverIP=null; //搜索模式
    //public static String serverIP="192.168.43.216"; //nanopi2的固定热点ip
    public static String serverIP="192.168.199.214"; //thinpad的固定wifi-ip

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences("test", MODE_PRIVATE);//仅本程序可访问，生成test.xml\

        try {
            Bundle bundle = this.getIntent().getExtras();
            serverIP = bundle.getString("ipaddr");
        }catch (Exception e){

        }

        buttonNavi=(Button)findViewById(R.id.buttonNavi);
        buttonNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.setClass(MainActivity.this, BaiduNaviActivity.class);
                intent.setClass(MainActivity.this, MapActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        buttonSkill=(Button)findViewById(R.id.buttonSkill);
        buttonSkill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(MainActivity.this, SkillActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        buttonConnect=(Button)findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(MainActivity.this, ConnectActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        textView=(TextView)findViewById(R.id.textView);
        handlerWeather=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                textView.setText((String)msg.obj);
            }
        };

        //启动语音唤醒后台服务
        Intent intent=new Intent(MainActivity.this,WakeupService.class);
        startService(intent);

        boolean weatherEnable=mSharedPreferences.getBoolean("weatherEnable",false);
        if((weatherEnable==true)&&(serverIP!=null)) {
            //启动显示天气服务
            Intent intent2 = new Intent(MainActivity.this, SkillWeatherService.class);
            startService(intent2);
        }

    }

    //重新返回MainActivity时，会执行下面的程序
    @Override
    protected void onRestart() {  //onStop的activity重新到前台会触发
        // TODO Auto-generated method stub
        super.onRestart();


    }
}
