package com.sofi.knittimer;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sofi.knittimer.data.DatabaseHandler;
import com.sofi.knittimer.data.Project;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ProjectAdapter mAdapter;
    private DatabaseHandler mDatabaseHandler;

    private FloatingActionButton fab;

    public static final int ADD_PROJECT_REQUEST = 0;

    public static final String PROJECT_NAME_KEY = "project name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mDatabaseHandler = new DatabaseHandler(this);
        mAdapter = new ProjectAdapter(this, mDatabaseHandler);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddProjectActivity();
            }
        });
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
                        addProject(data.getStringExtra(PROJECT_NAME_KEY));
                        updateList();
                }
        }
    }

    private void updateList() {
        mAdapter.notifyDataSetChanged();
    }

    // For fresh projects
    public void addProject(String projectName) {
        mDatabaseHandler.addProject(new Project(mDatabaseHandler.getProjectsCount(), projectName, 0, 0));
        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.closeDatabase();
    }
}
