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
import android.widget.Spinner;
import android.widget.Toast;

public class AddProjectActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText projectName;
    private Spinner spinner;

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

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // TODO: Once there are multiple choices, make it so that selecting the "already started"
        // project type starts a dialog where the user chooses how far the project is using NumberPickers
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
                        case 0: // Spinner set to 'fresh'. TODO: soft-code this.
                            // With this intent, we're able to return data (the project name) to the MainActivity
                            Intent intent = new Intent(this, this.getClass());
                            intent.putExtra(MainActivity.PROJECT_NAME_KEY, projectName.getText().toString());
                            setResult(RESULT_OK, intent);
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
}
