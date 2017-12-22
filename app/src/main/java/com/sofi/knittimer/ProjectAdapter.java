package com.sofi.knittimer;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sofi.knittimer.data.FetchImageTask;
import com.sofi.knittimer.data.Project;
import com.sofi.knittimer.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public MainActivity activityContext;
    public List<Project> projects;
    public Handler timingHandler;
    public TimingRunnable timingRunnable;
    public SharedPreferences preferences;

    private ActionMode mActionMode;
    private int selectedItemIndex;
    private View selectedView;

    public Dialogs dialogs;

    private Typeface dsGabrieleFont;

    public ProjectAdapter(MainActivity context) {
        activityContext = context;
        preferences = activityContext.getPreferences(Context.MODE_PRIVATE);
        projects = new ArrayList<Project>();
        dialogs = new Dialogs(this);
        timingHandler = new Handler();
        timingRunnable = new TimingRunnable(timingHandler, this);
        createFonts();
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == null) {
            projects = new ArrayList<Project>();
            this.notifyDataSetChanged();
            return;
        }
        projects = new ArrayList<Project>();
        if (newCursor.moveToFirst()) {
            do {
                projects.add(new Project(newCursor.getInt(0), newCursor.getString(1),
                        newCursor.getLong(2), newCursor.getInt(3)));
            } while (newCursor.moveToNext());
        }
        Project runningProject = getProjectById(preferences.getInt(activityContext.getResources()
                .getString(R.string.shared_preferences_current_id_key), -1));
        if (runningProject != null) {
            timingHandler.removeCallbacks(timingRunnable);
            runningProject.timerRunning = true;
            timingRunnable.begin();
        } else {
            resetPreferences();
        }
        this.notifyDataSetChanged();
    }

    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activityContext);

        View projectView = inflater.inflate(R.layout.project_list_item, parent, false);
        projectView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mActionMode != null) {
                    return false;
                }
                selectedItemIndex = activityContext.getRecyclerView().getChildLayoutPosition(v);
                mActionMode = activityContext.startActionMode(new mActionModeCallback());
                v.setSelected(true);
                selectedView = v;
                return true;
            }
        });

        ProjectViewHolder viewHolder = new ProjectViewHolder(projectView);
        return viewHolder;
    }

    private class mActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.main_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.main_context_menu_item_delete:
                    Dialogs.DeleteProjectDialogFragment fragment =
                            dialogs.getNewDeleteProjectDialogFragment
                                    (projects.get(selectedItemIndex), selectedItemIndex, mode);
                    fragment.show(activityContext.getFragmentManager(), "delete");
                    return true;
                case R.id.main_context_menu_item_edit:
                    Project selectedProject = projects.get(selectedItemIndex);
                    Intent intent = new Intent(activityContext, EditProjectActivity.class);
                    intent.putExtra(MainActivity.PROJECT_NAME_KEY, selectedProject.name);
                    intent.putExtra(MainActivity.PROJECT_ID_KEY, selectedProject.id);
                    intent.putExtra(MainActivity.PROJECT_TIME_KEY, selectedProject.timeSpentInMillis);
                    intent.putExtra(MainActivity.PROJECT_PERCENT_KEY, selectedProject.percentageDone);
                    activityContext.startActivityForResult(intent, MainActivity.EDIT_PROJECT_REQUEST);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (selectedView != null) {
                selectedView.setSelected(false);
                selectedView = null;
            }
            mActionMode = null;
        }
    }

    @Override
    public void onBindViewHolder(ProjectViewHolder holder, final int position) {

        if (projects == null) {
            return;
        }

        final Project project = projects.get(position);

        if (project.timerRunning) {
            holder.button.setActivated(true);
        } else {
            holder.button.setActivated(false);
        }

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentlyRunningId = preferences.getInt(activityContext.getResources()
                        .getString(R.string.shared_preferences_current_id_key), -1);
                if (currentlyRunningId == -1) { // if no project is currently running (play)
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt(activityContext.getResources().getString
                            (R.string.shared_preferences_current_id_key), project.id);
                    editor.putLong(activityContext.getResources().getString
                            (R.string.shared_preferences_begin_time_key), System.currentTimeMillis());
                    editor.apply();
                    timingRunnable.begin();
                    v.setActivated(true);
                    project.timerRunning = true;
                    ProjectAdapter.this.notifyItemChanged(position);
                    ((NotificationManager)activityContext.getSystemService(Context.NOTIFICATION_SERVICE))
                            .notify(NotificationUtils.NOTIFICATION_ID_TIMER_RUNNING,
                                    NotificationUtils.getTimerRunningNotification(activityContext));
                } else if (currentlyRunningId == project.id) { // if this project is currently running (pause)
                    timingHandler.removeCallbacks(timingRunnable);
                    resetPreferences();
                    v.setActivated(false);
                    project.timerRunning = false;
                    dialogs.getNewPauseProjectDialogFragment(project, position)
                            .show(activityContext.getFragmentManager(), "pause");
                    ProjectAdapter.this.notifyItemChanged(position);
                    ((NotificationManager)activityContext.getSystemService(Context.NOTIFICATION_SERVICE))
                            .cancel(NotificationUtils.NOTIFICATION_ID_TIMER_RUNNING);
                }
            }
        });

        // TODO: use cache
        if (!project.wasChecked) {
            holder.background.setImageResource(R.color.colorPrimaryDark);
            FetchImageTask task = new FetchImageTask(project, holder.background, this);
            task.execute();
            project.wasChecked = true;
        } else {
            holder.background.setImageDrawable(new BitmapDrawable(
                    activityContext.getResources(), project.background));
        }

        holder.projectName.setText(project.name);
        holder.projectName.setTypeface(dsGabrieleFont);
        holder.details.setText(createDetailsString(project));
        holder.details.setTypeface(dsGabrieleFont);
        holder.timeSpent.setText(createTimeString(project));
        holder.timeSpent.setTypeface(dsGabrieleFont);
    }

    @Override
    public int getItemCount() {
        if (projects == null) {
            return 0;
        }
        return projects.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {

        public ImageView background;
        public RelativeLayout textLayout;
        public TextView projectName;
        public TextView details;
        public TextView timeSpent;
        public ImageView button;

        public ProjectViewHolder(View itemView) {
            super(itemView);

            background = (ImageView) itemView.findViewById(R.id.iv_picture);
            textLayout = (RelativeLayout) itemView.findViewById(R.id.layout_texts);
            projectName = (TextView) itemView.findViewById(R.id.tv_project_name);
            details = (TextView) itemView.findViewById(R.id.tv_details);
            timeSpent = (TextView) itemView.findViewById(R.id.tv_time_spent);
            button = (ImageView) itemView.findViewById(R.id.iv_play);
        }
    }

    private Project getProjectById(int id) {
        for (Project project : projects) {
            if (project.id == id) {
                return project;
            }
        }
        return null; // project not found
    }

    private int getIndexOfProject(Project project) {
        if (project == null) {
            return -1;
        }
        int index = -1;
        for (Project newProject : projects) {
            index++;
            if (newProject == project) {
                return index;
            }
        }
        return -1; // project not found
    }

    public int updateTime(int projectId, long timeDifference) {
        Project project = getProjectById(projectId);
        int index = getIndexOfProject(project);
        if (project != null && index != -1) {
            project.timeSpentInMillis += timeDifference;
            ProjectAdapter.this.notifyItemChanged(index);
            return 1;
        }
        return 0;
    }

    public void resetPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(activityContext.getResources().getString
                (R.string.shared_preferences_current_id_key), -1);
        editor.putLong(activityContext.getResources().getString
                (R.string.shared_preferences_begin_time_key), -1);
        editor.apply();
        dialogs.getNewDebuggingDialog().show(activityContext.getFragmentManager(), "resetf");
    }

    private String createDetailsString(Project project) {
        if (project.timerRunning) {
            return "Working...";
        }

        if (project.percentageDone == 100) {
            return "100% done. Project finished! Yay!!";
        }

        long timeRemaining = project.timeLeftInMillis();

        if (timeRemaining == 0) { // Project hasn't been started (% is still set to 0)
            return "0% done. Let's get to work!";
        }

        long totalSeconds = timeRemaining / 1000;
        long totalMinutes = totalSeconds / 60;
        long littleMinutes = totalMinutes % 60; // total minutes - whole hours
        long totalHours = totalMinutes / 60;

        String details = project.percentageDone + "% done, " + totalHours;
        if (totalHours == 1) {
            details += " hour and ";
        } else {
            details += " hours and ";
        }
        details += littleMinutes;
        if (littleMinutes == 1) {
            details += " minute left";
        } else {
            details += " minutes left";
        }
        return details;
    }

    private String createTimeString(Project project) {
        long timeSpent = project.timeSpentInMillis;
        long totalSeconds = timeSpent / 1000;
        long littleSeconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long littleMinutes = totalMinutes % 60;
        long totalHours = totalMinutes / 60;

        String returnString;
        if (totalHours < 10) {
            returnString = "0" + totalHours + ":";
        } else {
            returnString = "" + totalHours + ":";
        }

        if (littleMinutes < 10) {
            returnString += "0" + littleMinutes + ":";
        } else {
            returnString += littleMinutes + ":";
        }

        if (littleSeconds < 10) {
            returnString += "0" + littleSeconds;
        } else {
            returnString += littleSeconds;
        }
        return returnString;
    }

    private void createFonts() {
        // TODO: Determine all fonts in xml (styles.xml and font folder)
        // When we update compileSdkVersion and TargetSdkVersion,
        // we can get a new support library with which we can define fonts in xml
        // in android v 14 and above.
        AssetManager am = activityContext.getAssets();
        dsGabrieleFont = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "DSGabriele.ttf"));
    }
}
