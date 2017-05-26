package com.sofi.knittimer;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.sofi.knittimer.data.Project;
import com.sofi.knittimer.data.ProjectContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private ProjectAdapter mAdapter;
    private ProgressBar mProgressBar;

    public static final int ADD_PROJECT_REQUEST = 0;

    public static final int ID_PROJECTS_LOADER = 73;

    public static final String PROJECT_NAME_KEY = "project name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mAdapter = new ProjectAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        getSupportLoaderManager().initLoader(ID_PROJECTS_LOADER, null, this);
    }

    private void startAddProjectActivity() {
        startActivityForResult(new Intent(this, AddProjectActivity.class), ADD_PROJECT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_PROJECT_REQUEST:
                switch (resultCode) {
                    case RESULT_OK:
                        ContentValues values = new ContentValues();
                        values.put(ProjectContract.ProjectEntry._NAME, data.getStringExtra(PROJECT_NAME_KEY));
                        values.put(ProjectContract.ProjectEntry._TIME_SPENT, 0);
                        values.put(ProjectContract.ProjectEntry._PERCENT_DONE, 0);
                        getContentResolver().insert(ProjectContract.ProjectEntry.CONTENT_URI, values);
                        return;
                    default:
                        throw new UnsupportedOperationException("Activity returned an unsupported result code");
                }
            default:
                throw new UnsupportedOperationException("Unknown request code");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_PROJECTS_LOADER:
                Uri projectsUri = ProjectContract.ProjectEntry.CONTENT_URI;
                String sortOrder = ProjectContract.ProjectEntry._PERCENT_DONE + " ASC";
                return new CursorLoader(this, projectsUri, null, null, null, sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_PROJECTS_LOADER:
                mAdapter.swapCursor(data);
                mProgressBar.setVisibility(View.INVISIBLE);
                return;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch(loader.getId()) {
            case ID_PROJECTS_LOADER:
                // mAdapter creates a list of items from the cursor as soon as it gets it.
                // Therefore, we don't need to call swapCursor(Null) as mAdaptor is not using it anymore.
                return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        menu.getItem(0).getIcon().mutate().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_item_add:
                startAddProjectActivity();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public int deleteProject(Project project) {
        return getContentResolver().delete(ProjectContract.ProjectEntry.CONTENT_URI
                .buildUpon().appendPath(project.id + "").build(),
                ProjectContract.ProjectEntry._ID + " = ?", new String[]{project.id + ""});
    }
}
