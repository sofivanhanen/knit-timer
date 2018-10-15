package com.sofi.knittimer.data;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.sofi.knittimer.ProjectAdapter;
import com.sofi.knittimer.utils.ImageUtils;

public class FetchImageTask extends AsyncTask<Void, Void, Void> {

    private Project project;
    private int position;
    private ProjectAdapter adapterContext;

    private Bitmap bitmap;

    public FetchImageTask(Project project, int position, ProjectAdapter adapterContext) {
        this.project = project;
        this.position = position;
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
        if (bitmap != null) {
            project.background = bitmap;
            adapterContext.notifyItemChanged(position);
        }
    }
}
