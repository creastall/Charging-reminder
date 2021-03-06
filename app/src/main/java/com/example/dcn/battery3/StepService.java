package com.example.dcn.battery3;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * 主进程 双进程通讯
 * Created by db on 2018/1/11.
 */
public class StepService extends Service {
    BatteryReceiver mReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return new ProcessConnection.Stub() {
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification());
        //绑定建立链接
        bindService(new Intent(this, GuardService.class),
                mServiceConnection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //链接上
            Log.d("test", "StepService:建立链接");
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            mReceiver = new BatteryReceiver(null);
            registerReceiver(mReceiver, intentFilter);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //断开链接
            startService(new Intent(StepService.this, GuardService.class));
            //重新绑定
            bindService(new Intent(StepService.this, GuardService.class),
                    mServiceConnection, Context.BIND_IMPORTANT);
        }
    };
}
