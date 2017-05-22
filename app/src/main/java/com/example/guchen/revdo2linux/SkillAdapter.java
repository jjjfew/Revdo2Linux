package com.example.guchen.revdo2linux;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.guchen.revdo2linux.MainActivity.mSharedPreferences;

/**
 * Created by guchen on 17-5-19.
 */

public class SkillAdapter extends BaseAdapter {
    public static ProgressDialog progDialog;
    private TextView textViewSkill;
    private Switch switchSkill;
    private Context mcontext;
    private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局 /*构造函数*/
    final SharedPreferences.Editor edit = mSharedPreferences.edit();

    //设置要list中要显示的数据
    private ArrayList<HashMap<String, Object>> getDate(){
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String,Object>>();
	/*为动态数组添加数据*/
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("skillName", "显示天气 >");
        listItem.add(map);
        map = new HashMap<String, Object>();
        map.put("skillName", "语音消息 >");
        listItem.add(map);
        map = new HashMap<String, Object>();
        map.put("skillName", "自动挂断 >");
        listItem.add(map);
        map = new HashMap<String, Object>();
        map.put("skillName", "语音拨号 >");
        listItem.add(map);

        return listItem;
    }

    public SkillAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        mcontext=context;
    }

    @Override
    public int getCount() {
        return getDate().size();//返回数组的长度
    }
    @Override
    public Object getItem ( int position){
        return null;
    }
    @Override
    public long getItemId ( int position){
        return 0;
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent) {
        //观察convertView随ListView滚动情况
        Log.w("MyListViewBase", "getView " + position + " " + convertView);
        //inflate就相当于将一个xml中定义的布局找出来.
        convertView = mInflater.inflate(R.layout.skill_item, null);
					/*得到各个控件的对象*/
        textViewSkill = (TextView) convertView.findViewById(R.id.textViewSkill);
        //convertView.setTag(holder); //绑定ViewHolder对象
        textViewSkill.setText(getDate().get(position).get("skillName").toString());
        textViewSkill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent();
                intent.setClass(mcontext, SkillInfoActivity.class);
                Bundle bundle = new Bundle();
                if (position == 0)
                    bundle.putString("name", "显示天气");
                else if (position == 1)
                    bundle.putString("name", "语音消息");
                else if (position == 2)
                    bundle.putString("name", "自动挂断");
                else if (position == 3)
                    bundle.putString("name", "语音拨号");
                intent.putExtras(bundle);
                showProgressDialog("加载远程数据...");  //显示等待加载进度框
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (SkillAdapter.progDialog != null) {
                            SystemClock.sleep(1000);
                            SkillAdapter.progDialog.dismiss();  //1秒后隐藏进度条
                            mcontext.startActivity(intent);
                        }
                    }
                }).start();
            }
        });

        switchSkill=(Switch)convertView.findViewById(R.id.switchSkill);
        if(position==0){
            //选中第一行和第三行
            boolean weatherEnable= mSharedPreferences.getBoolean("weatherEnable",false);
            if(weatherEnable==true) switchSkill.setChecked(true);
            else switchSkill.setChecked(false);
        }else if(position==1) {
            boolean smsReadEnable = mSharedPreferences.getBoolean("smsReadEnable", false);
            if (smsReadEnable == true) switchSkill.setChecked(true);
            else switchSkill.setChecked(false);
        }

        switchSkill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    if(position==0) {
                        edit.putBoolean("weatherEnable", true);
                        edit.commit(); //更新数据
                        //启动显示天气服务
                        Intent intent2 = new Intent(mcontext, SkillWeatherService.class);
                        mcontext.startService(intent2);
                    }else if(position==1){
                        edit.putBoolean("smsReadEnable", true);
                        edit.commit(); //更新数据
                    }
                    Log.w("xxx", "你开启了" + position);//开启第几行skill
                    showProgressDialog("开启远程Skill...");  //显示等待加载进度框
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (SkillAdapter.progDialog != null) {
                                SystemClock.sleep(1000);
                                SkillAdapter.progDialog.dismiss();  //1秒后隐藏进度条
                            }
                        }
                    }).start();
                } else {
                    if(position==0) {
                        edit.putBoolean("weatherEnable", false);
                        edit.commit(); //更新数据
                        //关闭显示天气服务
                        Intent intent2 = new Intent(mcontext, SkillWeatherService.class);
                        mcontext.stopService(intent2);
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
                    }else if(position==1){
                        edit.putBoolean("smsReadEnable", false);
                        edit.commit(); //更新数据
                    }
                    Log.w("xxx", "你关闭了" + position);//关闭第几行skill
                    showProgressDialog("关闭远程Skill...");  //显示等待加载进度框
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (SkillAdapter.progDialog != null) {
                                SystemClock.sleep(1000);
                                SkillAdapter.progDialog.dismiss();  //1秒后隐藏进度条
                            }
                        }
                    }).start();
                }
            }
        });
        return convertView;
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog(String disInfo) {
        if (progDialog == null)
            progDialog = new ProgressDialog(mcontext);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage(disInfo+"\n");
        progDialog.show();
    }
}
