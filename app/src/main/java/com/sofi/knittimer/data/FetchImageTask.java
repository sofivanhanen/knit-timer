package com.sofi.knittimer.data;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.sofi.knittimer.ProjectAdapter;
import com.sofi.knittimer.utils.ImageUtils;

public class FetchImageTask extends AsyncTask<Void, Void, Void> {

    private Project project;
    private ProjectAdapter adapterContext;

    private Bitmap bitmap;

    public FetchImageTask(Project project, ProjectAdapter adapterContext) {
        this.project = project;
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
        if (project.background == null && bitmap != null) {
            adapterContext.activityContext.changeBackgroundInMemoryCache("" + project.id, bitmap);
        }
        if (bitmap != null ||
                project.background != null) {
            project.background = bitmap;
            adapterContext.notifyBackgroundChanged(project.id);
        }
    }
}
