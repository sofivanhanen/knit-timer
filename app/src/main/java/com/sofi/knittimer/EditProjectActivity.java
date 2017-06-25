package com.sofi.knittimer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditProjectActivity extends AppCompatActivity {

    private EditText projectName;
    private LinearLayout timeSpentLayout;
    private TextView percentageDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
        makeLayout();
    }

    private void makeLayout() {
        findViewById(R.id.spinner).setVisibility(View.GONE);

        projectName = (EditText) findViewById(R.id.et_set_project_name);

        timeSpentLayout = (LinearLayout) findViewById(R.id.layout_time_spent);

        percentageDone = (TextView) findViewById(R.id.tv_percent);

        findViewById(R.id.layout_details).setVisibility(View.VISIBLE);
    }

    // TODO: Add functionality

}
