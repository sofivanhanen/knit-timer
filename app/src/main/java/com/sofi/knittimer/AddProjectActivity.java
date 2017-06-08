package com.sofi.knittimer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddProjectActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText projectName;
    private Spinner spinner;

    private Dialogs dialogs;

    private LinearLayout timeSpentLayout;
    private TextView percentageDoneTv;

    private final static int ARRAY_INDEX_BRAND_NEW = 0;
    private final static int ARRAY_INDEX_STARTED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        projectName = (EditText) findViewById(R.id.et_set_project_name);

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        dialogs = new Dialogs(this);

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        timeSpentLayout = (LinearLayout) findViewById(R.id.layout_time_spent);
        timeSpentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogs.getNewEditTimeDialogFragment((TextView) findViewById(R.id.tv_hours),
                        (TextView) findViewById(R.id.tv_minutes),
                        (TextView) findViewById(R.id.tv_seconds)).show(getFragmentManager(), "edit time");
            }
        });

        percentageDoneTv = (TextView) findViewById(R.id.tv_percent);
        percentageDoneTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogs.getNewPauseProjectDialogFragment(percentageDoneTv)
                        .show(getFragmentManager(), "edit percentage");
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == ARRAY_INDEX_STARTED) {
            findViewById(R.id.layout_details).setVisibility(View.VISIBLE);
        } else if (position == ARRAY_INDEX_BRAND_NEW) {
            findViewById(R.id.layout_details).setVisibility(View.GONE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_project_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_menu_item_add:
                if (projectName.getText().toString().equals("")) {
                    // No project name given; show a toast to prompt the user
                    Toast.makeText(getApplicationContext(), "Remember to name your project!",
                            Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    switch (spinner.getSelectedItemPosition()) {
                        case ARRAY_INDEX_BRAND_NEW:
                            // With this intent, we're able to return data (the project name) to the MainActivity
                            Intent intent = new Intent(this, this.getClass());
                            intent.putExtra(MainActivity.PROJECT_NAME_KEY, projectName.getText().toString());
                            intent.putExtra(MainActivity.PROJECT_TIME_KEY, 0 + "");
                            intent.putExtra(MainActivity.PROJECT_PERCENT_KEY, 0 + "");
                            setResult(RESULT_OK, intent);
                            finish();
                            return true;
                        case ARRAY_INDEX_STARTED:
                            Intent sIntent = new Intent(this, this.getClass());
                            sIntent.putExtra(MainActivity.PROJECT_NAME_KEY, projectName.getText().toString());
                            sIntent.putExtra(MainActivity.PROJECT_TIME_KEY, getTimeSpentInMillis() + "");
                            sIntent.putExtra(MainActivity.PROJECT_PERCENT_KEY, percentageDoneTv.getTag().toString());
                            setResult(RESULT_OK, sIntent);
                            finish();
                            return true;
                    }
                }
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private long getTimeSpentInMillis() {
        long value = Long.parseLong(((TextView) timeSpentLayout.findViewById(R.id.tv_hours))
                .getText().toString()) * 1000 * 60 * 60;
        value += Long.parseLong(((TextView) timeSpentLayout.findViewById(R.id.tv_minutes))
                .getText().toString()) * 1000 * 60;
        value += Long.parseLong(((TextView) timeSpentLayout.findViewById(R.id.tv_seconds))
                .getText().toString()) * 1000;
        return value;
    }
}
