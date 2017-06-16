package com.sofi.knittimer.data;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.sofi.knittimer.ProjectAdapter;
import com.sofi.knittimer.R;
import com.sofi.knittimer.utils.ImageUtils;

public class FetchImageTask extends AsyncTask<Void, Void, Void> {

    private Project project;
    private ImageView background;
    private ProjectAdapter adapterContext;

    private Bitmap bitmap;

    public FetchImageTask(Project project, ImageView background, ProjectAdapter adapterContext) {
        this.project = project;
        this.background = background;
        this.adapterContext = adapterContext;
        bitmap = null;
    }

    @Override
    protected Void doInBackground(Void... params) {
        bitmap = ImageUtils.loadImageFromStorage("proj" + project.id, adapterContext.activityContext);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (background != null && bitmap != null) {
            background.setImageDrawable(new BitmapDrawable(adapterContext.activityContext.getResources(), bitmap));
            project.background = bitmap;
        } else if (background != null) {
            background.setImageResource(R.color.colorPrimaryDark);
        }
    }
}
