package com.sofi.knittimer;

import android.animation.LayoutTransition;
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
import android.widget.Toast;

import com.sofi.knittimer.data.FetchImageTask;
import com.sofi.knittimer.data.Project;
import com.sofi.knittimer.utils.NotificationUtils;
import com.sofi.knittimer.utils.StringUtils;

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

    public ProjectAdapter(MainActivity context) {
        activityContext = context;
        preferences = activityContext.getPreferences(Context.MODE_PRIVATE);
        projects = new ArrayList<Project>();
        dialogs = new Dialogs(this);
        timingHandler = new Handler();
        timingRunnable = new TimingRunnable(timingHandler, this);
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
                Project project = new Project(newCursor.getInt(0), newCursor.getString(1),
                        newCursor.getLong(2), newCursor.getInt(3));
                projects.add(project);
                // This task will get the background image of this project.
                // If the image exists, it will save it into project.background
                // and call notifyItemChanged().
                // TODO: use LruCache
                FetchImageTask task = new FetchImageTask(project, newCursor.getPosition(), this);
                task.execute();
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
                if (projects.get(selectedItemIndex).timerRunning) {
                    Toast.makeText(activityContext, "Can't edit an active project!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                mActionMode = activityContext.startActionMode(new mActionModeCallback());
                v.setSelected(true);
                selectedView = v;
                return true;
            }
        });

        ProjectViewHolder viewHolder = new ProjectViewHolder(projectView);
        return viewHolder;
    }

    public void destroyActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
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
    public void onBindViewHolder(final ProjectViewHolder holder, final int position) {

        if (projects == null) {
            return;
        }

        final Project project = projects.get(position);

        if (project.timerRunning) {
            holder.button.setActivated(true);
            holder.textLayout.setVisibility(View.GONE);
            holder.textLayoutActivated.setVisibility(View.VISIBLE);
        } else {
            holder.button.setActivated(false);
            holder.textLayoutActivated.setVisibility(View.GONE);
            holder.textLayout.setVisibility(View.VISIBLE);
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
                    holder.textLayout.setVisibility(View.GONE);
                    holder.textLayoutActivated.setVisibility(View.VISIBLE);
                    ProjectAdapter.this.notifyItemChanged(position);
                    ((NotificationManager)activityContext.getSystemService(Context.NOTIFICATION_SERVICE))
                            .notify(NotificationUtils.NOTIFICATION_ID_TIMER_RUNNING,
                                    NotificationUtils.getTimerRunningNotification(activityContext));
                } else if (currentlyRunningId == project.id) { // if this project is currently running (pause)
                    timingHandler.removeCallbacks(timingRunnable);
                    resetPreferences();
                    v.setActivated(false);
                    project.timerRunning = false;
                    holder.textLayoutActivated.setVisibility(View.GONE);
                    holder.textLayout.setVisibility(View.VISIBLE);
                    dialogs.getNewPauseProjectDialogFragment(project, position)
                            .show(activityContext.getFragmentManager(), "pause");
                    ProjectAdapter.this.notifyItemChanged(position);
                    ((NotificationManager)activityContext.getSystemService(Context.NOTIFICATION_SERVICE))
                            .cancel(NotificationUtils.NOTIFICATION_ID_TIMER_RUNNING);
                }
            }
        });

        if (project.background != null) {
            holder.background.setImageDrawable(new BitmapDrawable(
                    activityContext.getResources(), project.background));
        } else {
            // Need to reset the image so recyclerView doesn't recycle an old image here.
            holder.background.setImageResource(R.mipmap.project_background_lower_quality);
        }

        holder.projectName.setText(project.name);
        holder.projectNameBig.setText(project.name);
        holder.details.setText(StringUtils.createDetailsString(project));
        String timeString = StringUtils.createTimeString(project);
        holder.timeSpent.setText(timeString);
        holder.timeSpentBig.setText(timeString);
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

        public RelativeLayout textLayoutActivated;
        public TextView projectNameBig;
        public TextView timeSpentBig;

        public ProjectViewHolder(View itemView) {
            super(itemView);

            background = (ImageView) itemView.findViewById(R.id.iv_picture);
            textLayout = (RelativeLayout) itemView.findViewById(R.id.layout_texts);
            projectName = (TextView) itemView.findViewById(R.id.tv_project_name);
            details = (TextView) itemView.findViewById(R.id.tv_details);
            timeSpent = (TextView) itemView.findViewById(R.id.tv_time_spent);
            button = (ImageView) itemView.findViewById(R.id.iv_play);

            textLayoutActivated = (RelativeLayout) itemView.findViewById(R.id.layout_texts_activated);
            projectNameBig = (TextView) itemView.findViewById(R.id.tv_project_name_activated);
            timeSpentBig = (TextView) itemView.findViewById(R.id.tv_time_spent_activated);

            ((ViewGroup)itemView.findViewById(R.id.root_project_list_item_top)).getLayoutTransition()
                    .enableTransitionType(LayoutTransition.CHANGING);
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
}
