package com.sofi.knittimer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "knittingDb";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " +
                ProjectContract.ProjectEntry.TABLE_PROJECTS + "( "
                + ProjectContract.ProjectEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ProjectContract.ProjectEntry._NAME + " TEXT NOT NULL,"
                + ProjectContract.ProjectEntry._TIME_SPENT + " INTEGER NOT NULL,"
                + ProjectContract.ProjectEntry._PERCENT_DONE + " INTEGER NOT NULL" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProjectContract.ProjectEntry.TABLE_PROJECTS);
        onCreate(db);
        // TODO: Implement data migration so data won't be lost if/when the database is updated
    }
}
