package com.sofi.knittimer.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Default User on 22.5.2017.
 */

public class ProjectProvider extends ContentProvider {

    // TODO: Implement rest of CRUD methods

    public static final int CODE_PROJECTS = 100;

    private DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mReadableDatabase;
    private SQLiteDatabase mWritableDatabase;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ProjectContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, ProjectContract.PATH_PROJECTS, CODE_PROJECTS);

        return matcher;
    }

    @Override
    synchronized public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    synchronized public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_PROJECTS:
                cursor = mReadableDatabase.query(ProjectContract.ProjectEntry.TABLE_PROJECTS,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    synchronized public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        long rowId = mWritableDatabase
                .insert(ProjectContract.ProjectEntry.TABLE_PROJECTS, "", values);

        if (rowId > 0) {
            Uri retUri = ContentUris.withAppendedId(ProjectContract.ProjectEntry.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(retUri, null);
            return retUri;
        }

        throw new SQLException("Failed to add record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
