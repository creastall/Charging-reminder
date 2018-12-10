package com.example.dcn.battery3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener{
    BatteryReceiver mReceiver;
    PowerManager.WakeLock mWakeLock;
    TextView mNoticeText;
    private SeekBar seekBar;
    static MediaPlayer mMediaPlayer;
    static Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        TextView batteryInfo = (TextView) findViewById(R.id.BatteryInfoText);
        mNoticeText = (TextView) findViewById(R.id.noticeText);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        SharedPreferences read = getSharedPreferences("notice", MODE_PRIVATE);
        int currentNum = Integer.parseInt(read.getString("noticeNum","80"));
        mNoticeText.setText(currentNum +"");
        seekBar.setProgress(currentNum);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mReceiver = new BatteryReceiver(batteryInfo);
        registerReceiver(mReceiver, intentFilter);
        getLock(this);
        startAllServices();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mNoticeText.setText(progress+"");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
//        mNoticeText.setText("开始拖动");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
//        mNoticeText.setText("停止拖动");
        SharedPreferences.Editor editor = getSharedPreferences("notice", MODE_PRIVATE).edit();
        editor.putString("noticeNum",mNoticeText.getText().toString());
        editor.commit();
    }

    public static void StopRing(){
        if (mMediaPlayer != null ){
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
        if (vibrator != null){
            vibrator.cancel();
            vibrator = null;
        }
    }

    public static void PlayRing(final Context context) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()){
            return;
        }
        Log.e("ee", "正在响铃");
        // 使用来电铃声的铃声路径
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(context, uri);
            mMediaPlayer.setLooping(true); //循环播放
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        vibrator = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
        long[] patter = {1000, 1000, 2000, 2000};
        vibrator.vibrate(patter, 0);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseLock();
    }


    synchronized private void getLock(Context context) {
        if (mWakeLock == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, StepService.class.getName());
            mWakeLock.setReferenceCounted(true);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis((System.currentTimeMillis()));
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if (hour >= 23 || hour <= 6) {
                mWakeLock.acquire(5000);
            } else {
                mWakeLock.acquire(300000);
            }
        }
        Log.v(TAG, "get lock");
    }

    synchronized private void releaseLock() {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
                Log.v(TAG, "release lock");
            }

            mWakeLock = null;
        }
    }

    /**
     * 开启所有Service
     */
    private void startAllServices() {
        startService(new Intent(this, StepService.class));
        startService(new Intent(this, GuardService.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "startAllServices: ");
            //版本必须大于5.0
            startService(new Intent(this, JobWakeUpService.class));
        }
    }

    public void saveLog(String content,String filename){
        FileOutputStream fos = null;
        try{
            File exfile = new File(getExternalFilesDir(null).getPath(),filename);
            fos = new FileOutputStream(exfile);
            byte[] buffer = content.getBytes();
            fos.write(buffer);
            fos.close();
        }catch (Exception ex){
            ex.printStackTrace();
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
