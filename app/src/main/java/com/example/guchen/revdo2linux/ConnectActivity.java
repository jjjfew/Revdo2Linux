package com.example.guchen.revdo2linux;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class ConnectActivity extends Activity {
    private TextView tv1;
    private String locAddress;//存储本机ip，例：本地ip ：192.168.1.
    private Runtime run = Runtime.getRuntime();//获取当前运行环境，来执行ping，相当于windows的cmd
    private String ping = "ping -c 1 -w 0.5 " ;//其中 -c 1为发送的次数，-w 表示发送后等待响应的时间
    private Process proc = null;
    private ProgressBar mProgressBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        mProgressBar=(ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setMax(256);
        mProgressBar.setProgress(0);

        tv1= (TextView) findViewById(R.id.textViewConnect);
        tv1.setText("搜索中...");
        scan();
    }

    private Handler handler1 = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case 222:// 服务器消息
                    break;
                case 333:// 扫描完毕消息
                    //Toast.makeText(ConnectActivity.this, "扫描到主机："+(String)msg.obj, Toast.LENGTH_LONG).show();
                    final String ipaddrValue=(String)msg.obj;
                    LinearLayout rl=	(LinearLayout)findViewById(R.id.layoutConnect);
                    Button btTemp=new Button(ConnectActivity.this);
                    btTemp.setText((String)msg.obj);
                    btTemp.setWidth(80);
                    rl.addView(btTemp);
                    btTemp.setOnClickListener(new Button.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            Intent intent1=new Intent();
                            intent1.setClass(ConnectActivity.this,MainActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("ipaddr",ipaddrValue);
                            intent1.putExtras(bundle);
                            startActivity(intent1);
                            ConnectActivity.this.finish();
                        }
                    });
                    break;
                case 444://扫描失败
                    Toast.makeText(ConnectActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    /**
     * 扫描局域网内ip，找到对应服务器
     * 由于华为android7.0对内存使用的限制，同时并发256个thread，会造成内存溢出，因为分为128个thread。
     */

    public void scan(){

        locAddress = getLocAddrIndex();//获取本地ip前3段

        if(locAddress.equals("")){
            Toast.makeText(ConnectActivity.this, "扫描失败，请检查wifi网络", Toast.LENGTH_LONG).show();
            return ;
        }

        for (int i = 0; i < 128; i++) {//创建256个线程分别去ping 0-256地址
            final int j = i; //存放ip最后一位地址 0-255,必须声明为final，否则不同thread会更改这个值
            new Thread(new Runnable() {
                public void run() {
                    String p = ping + locAddress + j;
                    String current_ip = locAddress + j;
                    // Log.w("xxx", "current_ip " + current_ip);
                    try {
                        proc = run.exec(p);
                        int result = proc.waitFor();  //成功执行返回码为0
                        if ((result == 0) && (j != 1)) {
                            Log.w("xxx", "连接成功" + current_ip);
                            Message.obtain(handler1, 333, current_ip).sendToTarget();//返回扫描完毕消息
                        } else {
                            // Log.w("xxx", "fail " + current_ip);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    } finally {
                        //Log.w("xxx","destroy "+ j);
                        mProgressBar.incrementProgressBy(1);  //进度增长1
                        if(mProgressBar.getProgress()==256)runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv1.setText("搜索完成");
                            }
                        });
                        proc.destroy(); //关闭ping线程
                    }

                    p = ping + locAddress + j*2;
                    current_ip = locAddress + j*2;
                    try {
                        proc = run.exec(p);
                        int result = proc.waitFor();  //成功执行返回码为0
                        if ((result == 0) && (j != 1)) {
                            Log.w("xxx", "连接成功" + current_ip);
                            Message.obtain(handler1, 333, current_ip).sendToTarget();//返回扫描完毕消息
                        } else {
                            // Log.w("xxx", "fail " + current_ip);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    } finally {
                        //Log.w("xxx","destroy "+ j);
                        mProgressBar.incrementProgressBy(1);  //进度增长1
                        if(mProgressBar.getProgress()==256)runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv1.setText("搜索完成");
                            }
                        });
                        proc.destroy(); //关闭ping线程
                    }
                }
            }).start();
        }
}

    //获得本机IP
    private String getlocalip(){
        /*
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        Log.d("xxx", "int ip "+ipAddress);
        if(ipAddress==0)return null;
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));
        */
        return "192.168.43.1";
    }

    //获取IP前缀
    public String getLocAddrIndex(){

        String str = getlocalip();

        if(!str.equals("")){
            return str.substring(0,str.lastIndexOf(".")+1);
        }

        return null;
    }

}
