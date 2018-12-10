package com.example.dcn.battery3;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NoticeCenter {
    public static void sendNotice(int icon, CharSequence tickerText, CharSequence contentTitle, CharSequence contentText, Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context,MainActivity.class), 0);
        Notification notify = new Notification.Builder(context)
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(pendingIntent).setNumber(1).build();
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        nm.notify(0, notify);
    }
}
