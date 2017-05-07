package com.example.guchen.revdo2linux;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by guchen on 17-4-6.
 */

public class SocketClient {
    // private Context context;
    private String rmaddr;

    public SocketClient(String rmaddr){
        // this.context=context;
        this.rmaddr=rmaddr;
    }

    public void sendToServer(String cmd){
        Socket socket=null;
        Log.w("xxx","send to server...");
        try {
            socket=new Socket(rmaddr,54321);
            //send data to server
            PrintWriter out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            out.println(cmd);
            /*
            //receive data form server return
            BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String mstr=br.readLine();
            String isSent;
            Log.w("xxx",mstr);
            if(mstr.equals(cmd)) {
                isSent = "命令发送成功";
               // Message.obtain(MainActivity.handlerConnect, 333, "Revdo：已连接").sendToTarget();
            }
            else {
                isSent="命令发送失败";
              //  Message.obtain(MainActivity.handlerConnect, 333, "命令发送失败").sendToTarget();
            }
            Message msg=new Message();
            msg.what=111;
            msg.obj=isSent;
            //ControlClass.handler1.sendMessage(msg);
            //out.close();
            //br.close();
            //socket.close();
            */
        }catch(Exception e)
        {
            Log.e("xxx","exception");
           // Message.obtain(MainActivity.handlerConnect, 333, "Revdo：已断开").sendToTarget();
        }
    }

    public void recvFromServer(String cmd){
        Socket socket=null;
        Log.e("dddd", "sent id");
        try {
            socket=new Socket(rmaddr,54321);
            //send data to server
            PrintWriter out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            out.println(cmd);
            //receive data form server return
            BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String mstr=br.readLine();
            String isSent;
            if(mstr.equals(cmd)) isSent="命令发送成功";
            else isSent="命令发送失败";
            Message msg=new Message();
            msg.what=111;
            msg.obj=isSent;
           // ControlClass.handler1.sendMessage(msg);
            out.close();
            br.close();
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch(Exception e)
        {
            Log.e("xxx",e.toString());
        }
    }
}
