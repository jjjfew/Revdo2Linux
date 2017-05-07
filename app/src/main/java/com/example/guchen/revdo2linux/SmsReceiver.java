package com.example.guchen.revdo2linux;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    public static final String  action= "android.provider.Telephony.SMS_RECEIVED";

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        boolean smsReadEnable=MainActivity.mSharedPreferences.getBoolean("smsReadEnable",false);
        if ((action.equals(intent.getAction()))&&(smsReadEnable==true)){
            Intent i = new Intent(context, SmsReadService.class);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SmsMessage[] msgs = getMessageFromIntent(intent);
            StringBuilder sBuilder = new StringBuilder();
            if (msgs != null && msgs.length > 0 ) {
                for (SmsMessage msg : msgs) {
                    sBuilder.append("接收到了短信：\n发件人是：");
                    sBuilder.append(msg.getDisplayOriginatingAddress());
                    sBuilder.append("\n------短信内容-------\n");
                    sBuilder.append(msg.getDisplayMessageBody());
                    i.putExtra("sms_address", msg.getDisplayOriginatingAddress());
                    i.putExtra("sms_body", msg.getDisplayMessageBody());
                }
            }
            Toast.makeText(context, sBuilder.toString(), Toast.LENGTH_LONG).show();
            context.startService(i);
        }
    }

    public static SmsMessage[] getMessageFromIntent(Intent intent) {
        SmsMessage retmeMessage[] = null;
        Bundle bundle = intent.getExtras();
        Object pdus[] = (Object[]) bundle.get("pdus");
        retmeMessage = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            byte[] bytedata = (byte[]) pdus[i];
            retmeMessage[i]  = SmsMessage.createFromPdu(bytedata);
        }
        return retmeMessage;
    }
}
