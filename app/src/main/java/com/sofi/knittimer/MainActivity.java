package com.sofi.knittimer;

import android.support.design.widget.FloatingActionButton;
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
                addProject();
            }
        });
    }

    public void addProject() {
        // Starts new activity in which user gives details of the project
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.closeDatabase();
    }
}
