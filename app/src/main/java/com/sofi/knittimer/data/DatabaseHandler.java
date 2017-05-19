package com.sofi.knittimer.data;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.sofi.knittimer.R;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "knittingDb";

    private static final String TABLE_PROJECTS = "projects";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_TIME_SPENT = "time_spent";
    private static final String KEY_PERCENT_DONE = "percentage_done";

    private SQLiteDatabase mDatabase;

    private ProgressBar mProgressBar;

    private void initDatabase() {
        // Initializes the private mDatabase if it hasn't yet been initialized
        if (mDatabase == null) {
            // TODO: Make the progressbar work (do this in background)
            mProgressBar.setVisibility(View.VISIBLE);
            mDatabase = this.getWritableDatabase();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void closeDatabaseConnection() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mProgressBar = (ProgressBar) ((Activity) context).findViewById(R.id.progress_bar);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PROJECTS + "( "
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_TIME_SPENT + " INTEGER," + KEY_PERCENT_DONE + " INTEGER" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        onCreate(db);
        // TODO: Implement data migration so data won't be lost if/when the database is updated
    }

    public void addProject(Project project) {
        initDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, project._name);
        values.put(KEY_TIME_SPENT, project._timeSpentInMillis);
        values.put(KEY_PERCENT_DONE, project._percentageDone);

        mDatabase.insert(TABLE_PROJECTS, null, values);
    }

    public Project getProject(int id) {
        initDatabase();

        Cursor cursor = mDatabase.query(TABLE_PROJECTS, new String[] {KEY_ID, KEY_NAME,
                KEY_TIME_SPENT, KEY_PERCENT_DONE}, KEY_ID + " = ?",
                new String[] {"" + id}, null, null, null);

        if (cursor == null || cursor.getCount() == 0) throw new IllegalStateException("getProject: Cursor was empty");

        cursor.moveToFirst();

        Project project = new Project(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                Integer.parseInt(cursor.getString(2)), Integer.parseInt(cursor.getString(3)));
        cursor.close();
        return project;
    }

    public List<Project> getAllProjects() {
        initDatabase();

        List<Project> projectList = new ArrayList<Project>();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_PROJECTS, null);

        if (cursor.moveToFirst()) {
            do {
                Project project = new Project(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1), Integer.parseInt(cursor.getString(2)),
                        Integer.parseInt(cursor.getString(3)));
                projectList.add(project);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return projectList;
    }

    public int getProjectsCount() {
        initDatabase();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_PROJECTS, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int updateProject(Project project) {
        initDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, project._name);
        values.put(KEY_TIME_SPENT, project._timeSpentInMillis);
        values.put(KEY_PERCENT_DONE, project._percentageDone);

        return mDatabase.update(TABLE_PROJECTS, values, KEY_ID + " =?",
                new String[] {String.valueOf(project._id)});
    }

    public void deleteProject(Project project) {
        initDatabase();

        mDatabase.delete(TABLE_PROJECTS, KEY_ID + " = ?",
                new String[] {String.valueOf(project._id)});
    }

}
