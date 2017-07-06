package com.sofi.knittimer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sofi.knittimer.utils.ImageUtils;

public class EditProjectActivity extends AddProjectActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        makeLayout();
    }

    private void makeLayout() {
        findViewById(R.id.spinner).setVisibility(View.GONE);

        projectName.setText(intent.getStringExtra(MainActivity.PROJECT_NAME_KEY));
        timeSpentLayout = (LinearLayout) findViewById(R.id.layout_time_spent);
        changeTimeSpent(intent.getLongExtra(MainActivity.PROJECT_TIME_KEY, 0));
        percentageDoneTv.setText(intent.getIntExtra(MainActivity.PROJECT_PERCENT_KEY, 0) + "%");
        percentageDoneTv.setTag(intent.getIntExtra(MainActivity.PROJECT_PERCENT_KEY, 0));
        findViewById(R.id.layout_details).setVisibility(View.VISIBLE);

        Bitmap bitmap = ImageUtils.loadImageFromStorage("proj" +
                intent.getIntExtra(MainActivity.PROJECT_ID_KEY, 0), this);
        if (bitmap != null) {
            pictureBackground.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_project_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_menu_item_edit:
                if (projectName.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Remember to name your project!",
                            Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Intent newIntent = new Intent();
                    newIntent.putExtra(MainActivity.PROJECT_NAME_KEY,
                            projectName.getText().toString());
                    newIntent.putExtra(MainActivity.PROJECT_ID_KEY,
                            intent.getIntExtra(MainActivity.PROJECT_ID_KEY, 0));
                    newIntent.putExtra(MainActivity.PROJECT_HAS_IMAGE_KEY, hasBackground);
                    newIntent.putExtra(MainActivity.PROJECT_TIME_KEY, getTimeSpentInMillis());
                    newIntent.putExtra(MainActivity.PROJECT_PERCENT_KEY,
                            Integer.parseInt(percentageDoneTv.getTag().toString()));
                    setResult(RESULT_OK, newIntent);
                    finish();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
