package com.example.dcn.battery3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReceiverOnly extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mIntent = new Intent(context, StepService.class);
        context.startService(mIntent);
    }
}
