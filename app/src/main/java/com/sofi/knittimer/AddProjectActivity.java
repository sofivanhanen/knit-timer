package com.sofi.knittimer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sofi.knittimer.utils.ImageUtils;

import java.io.File;
import java.io.IOException;

public class AddProjectActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    protected EditText projectName;
    protected ImageView pictureBackground;
    protected TextView pictureButton;
    protected int interruptedIntentRequestCode = 0;
    protected Spinner spinner;

    protected Dialogs dialogs;

    protected LinearLayout timeSpentLayout;
    protected TextView percentageDoneTv;

    public static final int PERMISSION_REQUEST_CODE = 999;

    private final static int ARRAY_INDEX_BRAND_NEW = 0;
    private final static int ARRAY_INDEX_STARTED = 1;

    protected boolean hasBackground;

    // TODO: Make taking picture for background work and make the option visible (in Dialogs)

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

        hasBackground = false;
    }

    protected void setOnClickListeners() {
        pictureBackground = (ImageView) findViewById(R.id.iv_picture);
        pictureButton = (TextView) findViewById(R.id.tv_picture);
        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogs.getNewAddPictureDialogFragment().show(getFragmentManager(), "add picture");
            }
        });

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
        getMenuInflater().inflate(R.menu.add_project_menu, menu);
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
                    Intent intent = new Intent(this, this.getClass());
                    intent.putExtra(MainActivity.PROJECT_NAME_KEY, projectName.getText().toString());
                    intent.putExtra(MainActivity.PROJECT_HAS_IMAGE_KEY, hasBackground);

                    switch (spinner.getSelectedItemPosition()) {
                        case ARRAY_INDEX_BRAND_NEW:
                            intent.putExtra(MainActivity.PROJECT_TIME_KEY, 0);
                            intent.putExtra(MainActivity.PROJECT_PERCENT_KEY, 0);
                            setResult(RESULT_OK, intent);
                            finish();
                            return true;
                        case ARRAY_INDEX_STARTED:
                            intent.putExtra(MainActivity.PROJECT_TIME_KEY, getTimeSpentInMillis());
                            intent.putExtra(MainActivity.PROJECT_PERCENT_KEY,
                                    Integer.parseInt(percentageDoneTv.getTag().toString()));
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

    public void startImplicitIntent(int requestCode) {
        if (!havePermissionForStorage()) {
            interruptedIntentRequestCode = requestCode;
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            return;
        } else {
            if (requestCode == Dialogs.CAPTURE_PICTURE_REQUEST) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = ImageUtils.createImageFile(this, "temp");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (photoFile != null) {
                        Uri photoUri = FileProvider.getUriForFile(this, "com.sofi.knittimer.fileprovider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(intent, Dialogs.CAPTURE_PICTURE_REQUEST);
                    }
                }
            } else if (requestCode == Dialogs.CHOOSE_FROM_GALLERY_REQUEST) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent.createChooser(intent, "Select File"),
                        Dialogs.CHOOSE_FROM_GALLERY_REQUEST);
            }
            interruptedIntentRequestCode = 0;
        }
    }

    protected boolean havePermissionForStorage() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (interruptedIntentRequestCode != 0) {
                        startImplicitIntent(interruptedIntentRequestCode);
                    }
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = null;
            if (requestCode == Dialogs.CAPTURE_PICTURE_REQUEST) {
                bitmap = ImageUtils.loadImageFromStorage("temp", this);
            } else if (requestCode == Dialogs.CHOOSE_FROM_GALLERY_REQUEST) {
                try {
                    bitmap = ImageUtils.resizeBitmap(MediaStore.Images.Media.getBitmap
                            (getApplicationContext().getContentResolver(), data.getData()));
                    ImageUtils.saveToExternalStorage(bitmap, "temp", AddProjectActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                return;
            }
            if (bitmap != null) {
                hasBackground = true;
                pictureBackground.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
            }
        }
    }

    protected long getTimeSpentInMillis() {
        long value = Long.parseLong(((TextView) timeSpentLayout.findViewById(R.id.tv_hours))
                .getText().toString()) * 1000 * 60 * 60;
        value += Long.parseLong(((TextView) timeSpentLayout.findViewById(R.id.tv_minutes))
                .getText().toString()) * 1000 * 60;
        value += Long.parseLong(((TextView) timeSpentLayout.findViewById(R.id.tv_seconds))
                .getText().toString()) * 1000;
        return value;
    }

    protected void changeTimeSpent(long timeInMillis) {
        long hours = timeInMillis / (1000 * 60 * 60);
        if (hours < 10) {
            ((TextView) timeSpentLayout.findViewById(R.id.tv_hours)).setText("0" + hours);
        } else {
            ((TextView) timeSpentLayout.findViewById(R.id.tv_hours)).setText(hours + "");
        }
        long minutes = (timeInMillis / (1000 * 60)) % 60;
        if (minutes < 10) {
            ((TextView) timeSpentLayout.findViewById(R.id.tv_minutes)).setText("0" + minutes);
        } else {
            ((TextView) timeSpentLayout.findViewById(R.id.tv_minutes)).setText(minutes + "");
        }
        long seconds = (timeInMillis / 1000) % 60;
        if (seconds < 10) {
            ((TextView) timeSpentLayout.findViewById(R.id.tv_seconds)).setText("0" + seconds);
        } else {
            ((TextView) timeSpentLayout.findViewById(R.id.tv_seconds)).setText(seconds + "");
        }
    }
}
