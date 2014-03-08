package com.jonah.srun3000;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jonah on 13-6-17.
 */
public class DatabaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "db";
    private static final int DATABASE_VERSION = 4;
    public static final String ACCOUNT_TABLE_NAME = "account";
    private static final String ACCOUNT_TABLE_CREATE =
            "CREATE TABLE " + ACCOUNT_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT, " +
                    "password TEXT, " +
                    "free INTEGER " +
                    ");";
//    private static final String


    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ACCOUNT_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE " + ACCOUNT_TABLE_NAME + " ADD free INTEGER;");
    }
}
