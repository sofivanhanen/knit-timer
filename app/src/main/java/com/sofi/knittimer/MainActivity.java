package com.sofi.knittimer;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.sofi.knittimer.data.Project;
import com.sofi.knittimer.data.ProjectContract;
import com.sofi.knittimer.utils.DataUtils;
import com.sofi.knittimer.utils.DialogUtils;
import com.sofi.knittimer.utils.ImageUtils;
import com.sofi.knittimer.utils.NotificationUtils;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        DialogUtils.PercentageSetterDialogFragment.PercentageSetterDialogListener,
        DialogUtils.DeleteProjectDialogFragment.DeleteProjectDialogListener {

    // TODO: Save data so that data persists through deleting and reinstalling
    // TODO: Add broadcast receiver for ACTION_SHUTDOWN and update running project when phone turns off

    private RecyclerView mRecyclerView;
    private ProjectAdapter mAdapter;

    public static final int ADD_PROJECT_REQUEST = 24;
    public static final int EDIT_PROJECT_REQUEST = 42;
    public static final int CAPTURE_PICTURE_REQUEST = 80;
    public static final int CHOOSE_FROM_GALLERY_REQUEST = 81;

    public static final int ID_PROJECTS_LOADER = 73;

    public static final String PROJECT_NAME_KEY = "project name";
    public static final String PROJECT_ID_KEY = "project id";
    public static final String PROJECT_TIME_KEY = "project time";
    public static final String PROJECT_PERCENT_KEY = "project percent";
    public static final String PROJECT_HAS_IMAGE_KEY = "project has image in temp";

    public static final String HOURS_KEY = "hours";
    public static final String MINUTES_KEY = "minutes";
    public static final String SECONDS_KEY = "seconds";

    private boolean bitmapIsWaiting;
    private int waitingBitmapProjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mAdapter = new ProjectAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        getSupportLoaderManager().initLoader(ID_PROJECTS_LOADER, null, this);
        bitmapIsWaiting = false;
        waitingBitmapProjectId = -1;

        createNotificationChannel();
    }

    private void startAddProjectActivity() {
        startActivityForResult(new Intent(this, AddProjectActivity.class), ADD_PROJECT_REQUEST);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NotificationUtils.NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_PROJECT_REQUEST:
                switch (resultCode) {
                    case RESULT_OK:
                        bitmapIsWaiting = data.getBooleanExtra(PROJECT_HAS_IMAGE_KEY, false);
                        ContentValues contentValues = DataUtils.intentToContentValues(data);
                        Uri uri = insertProject(contentValues);
                        if (uri == null) {
                            throw new UnsupportedOperationException("Insert failed!");
                        }
                        getSupportLoaderManager().restartLoader(ID_PROJECTS_LOADER, null, this);
                        Toast.makeText(getApplicationContext(), "Project added!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    default:
                        return;
                }
            case EDIT_PROJECT_REQUEST:
                switch (resultCode) {
                    case RESULT_OK:

                        bitmapIsWaiting = data.getBooleanExtra(PROJECT_HAS_IMAGE_KEY, false);
                        waitingBitmapProjectId = data.getIntExtra(PROJECT_ID_KEY, -1);
                        ContentValues contentValues = DataUtils.intentToContentValues(data);
                        int amount = updateProject(contentValues);
                        if (amount != 1) {
                            throw new UnsupportedOperationException("update failed!");
                        }
                        getSupportLoaderManager().restartLoader(ID_PROJECTS_LOADER, null, this);
                        Toast.makeText(getApplicationContext(), "Project edited!",
                                Toast.LENGTH_SHORT).show();
                        mAdapter.destroyActionMode();
                        return;
                    default:
                        return;
                }
            default:
                throw new UnsupportedOperationException("Unknown request code");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(ID_PROJECTS_LOADER, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_PROJECTS_LOADER:
                // Sorts projects so that finished projects are last, 0% projects first.
                // If there are projects with same #% done, sorts those by name.
                Uri projectsUri = ProjectContract.ProjectEntry.CONTENT_URI;
                String sortOrder = "CAST (" + ProjectContract.ProjectEntry._PERCENT_DONE
                        + " AS int) ASC, CAST (" + ProjectContract.ProjectEntry._NAME
                        + " AS text) COLLATE NOCASE ASC ";
                return new CursorLoader(this, projectsUri, null, null, null, sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_PROJECTS_LOADER:

                if (bitmapIsWaiting && data.moveToFirst()) {
                    int mostRecentId = -1;
                    if (waitingBitmapProjectId != -1) {
                        mostRecentId = waitingBitmapProjectId;
                        waitingBitmapProjectId = -1;
                    } else {
                        do {
                            if (mostRecentId < data.getInt(0)) {
                                mostRecentId = data.getInt(0);
                            }
                        } while (data.moveToNext());
                    }
                    // TODO: Make this asynchronous
                    ImageUtils.saveToExternalStorage(ImageUtils.loadImageFromStorage("temp", this), "proj" + mostRecentId, MainActivity.this);
                    bitmapIsWaiting = false;
                    data.moveToFirst();
                }
                mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_PROJECTS_LOADER:
                // mAdapter creates a list of items from the cursor as soon as it gets it.
                // Therefore, we don't need to call swapCursor(null) as mAdaptor is not using it anymore.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_item_add:
                startAddProjectActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }
    // TODO: Put these data handling methods into a utilities class
    public int deleteProject(int projectId) {
        return getContentResolver().delete(ProjectContract.ProjectEntry.CONTENT_URI
                        .buildUpon().appendPath(projectId + "").build(),
                ProjectContract.ProjectEntry._ID + " = ?", new String[]{projectId + ""});
    }

    public int updateProject(Project project) {
        ContentValues values = new ContentValues();
        values.put(ProjectContract.ProjectEntry._NAME, project.name);
        values.put(ProjectContract.ProjectEntry._PERCENT_DONE, project.percentageDone);
        values.put(ProjectContract.ProjectEntry._TIME_SPENT, project.timeSpentInMillis);
        return getContentResolver().update(ProjectContract.ProjectEntry.CONTENT_URI.buildUpon()
                        .appendPath(project.id + "").build(), values,
                ProjectContract.ProjectEntry._ID + " = ?",
                new String[]{project.id + ""});
    }

    public int updateProject(ContentValues contentValues) {
        int id = contentValues.getAsInteger(ProjectContract.ProjectEntry._ID);
        return getContentResolver().update(ProjectContract.ProjectEntry.CONTENT_URI.buildUpon()
                        .appendPath(id + "").build(), contentValues,
                ProjectContract.ProjectEntry._ID + " = ?", new String[]{id + ""});
    }

    public Uri insertProject(ContentValues contentValues) {
        return getContentResolver().insert(ProjectContract.ProjectEntry.CONTENT_URI, contentValues);
    }

    @Override
    public void onPercentageSetterDialogPositiveClick(int projectId, int newPercentage) {
        Project project = mAdapter.getProjectById(projectId);
        project.percentageDone = newPercentage;
        updateProject(project);
        mAdapter.notifyItemChanged(mAdapter.getIndexOfProject(project));
    }

    @Override
    public void onDeleteProjectDialogPositiveClick(int projectId) {
        if (deleteProject(projectId) >= 1) {
            mAdapter.projectDeleted(projectId);
            mAdapter.destroyActionMode(); // TODO ActionMode should be handled by Activity, not adapter.
        }
    }
}
