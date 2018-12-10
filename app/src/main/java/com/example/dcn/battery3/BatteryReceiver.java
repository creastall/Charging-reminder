package com.example.dcn.battery3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.widget.TextView;

public class BatteryReceiver extends BroadcastReceiver {
    private TextView pow;

    public BatteryReceiver(TextView pow) {
        this.pow = pow;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getAction();
        System.out.println("接收广播："+name);
        switch (name) {
            case Intent.ACTION_POWER_CONNECTED:
                NoticeCenter.sendNotice(R.mipmap.ic_launcher, "", "正在充电", "", context);
                break;
            case Intent.ACTION_BATTERY_CHANGED:
                int current = intent.getExtras().getInt("level");// 获得当前电量
                int total = intent.getExtras().getInt("scale");// 获得总电量
                int percent = current * 100 / total;
                int state = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                SharedPreferences read = context.getSharedPreferences("notice", context.MODE_PRIVATE);
                String numString = read.getString("noticeNum","80");
                if (state == BatteryManager.BATTERY_STATUS_CHARGING && percent >= Integer.parseInt(numString)) {
                    NoticeCenter.sendNotice(R.mipmap.ic_launcher, "", "电量:" + percent + "%", "该拔掉电源了", context);
                }
                if (pow != null) {
                    pow.setText(percent + "%");
                }
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                NoticeCenter.sendNotice(R.mipmap.ic_launcher, "", "断开充电", "", context);
                break;
        }

    }
}
