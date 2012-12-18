package com.skyteam.easy.chat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatHistory {

    private static final String DATABASE_NAME = "chat_history";
    private static final int DATABASE_VERSION = 2;
    
    private static final String TABLE_NAME = "messages";
    
    // Speciální hodnota "_id", pro jednodušší použití SimpleCursorAdapteru
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USER = "user";
    public static final String COLUMN_MESSAGE = "message";
    
    public static final String[] columns = { COLUMN_ID, COLUMN_USER, 
                                             COLUMN_MESSAGE };
    
    private static final String ORDER_BY = COLUMN_ID + " DESC";
    
    private SQLiteOpenHelper mOpenHelper;
    
    static class DatabaseHelper extends SQLiteOpenHelper {
        
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" 
                            + COLUMN_ID + " INTEGER PRIMARY KEY," 
                            + COLUMN_USER + " TEXT NOT NULL," 
                            + COLUMN_MESSAGE + " TEXT NOT NULL" + ");");
        }

        /*
         * Ve skutečnosti je potřeba, abychom uživatelům nemazali data, vytvořit
         * pro každou změnu struktury databáze nějaký upgradovací nedestruktivní
         * SQL příkaz.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO upgrade without loose data
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);            
        }
        
    }
    
    public ChatHistory(Context context) {
        mOpenHelper = new DatabaseHelper(context);
    }
    
    public Cursor getMessages() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.query(TABLE_NAME, columns, null, null, null, null, ORDER_BY);
    }
    
    public Cursor getMessage(long id) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String[] selectionArgs = { String.valueOf(id) };
        return db.query(TABLE_NAME, columns, COLUMN_ID + "= ?", selectionArgs, 
                        null, null, ORDER_BY);
    }
    
    public boolean deleteMessage(long id) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String[] selectionArgs = { String.valueOf(id) };
        
        int deletedCount = db.delete(TABLE_NAME, COLUMN_ID + "= ?", selectionArgs);
        db.close();
        return deletedCount > 0;
    }
    
    public long insertMessage(String user, String message) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER, user);
        values.put(COLUMN_MESSAGE, message);
        
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }
    
    public void close() {
        mOpenHelper.close();
    }
    
}
