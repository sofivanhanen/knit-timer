package com.sofi.knittimer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public MainActivity activityContext;
    public List<Project> projects;
    public Handler timingHandler;
    public TimingRunnable timingRunnable;
    private SharedPreferences preferences;

    private ActionMode mActionMode;
    private int selectedItemIndex;
    private View selectedView;

    public Dialogs dialogs;

    public ProjectAdapter(MainActivity context) {
        activityContext = context;
        projects = new ArrayList<Project>();
        dialogs = new Dialogs(this);
        timingHandler = new Handler();
        timingRunnable = new TimingRunnable(timingHandler, this);
        preferences = activityContext.getPreferences(Context.MODE_PRIVATE);
        /*
        IntentFilter filter = new IntentFilter(TimerService.BROADCAST_ACTION_UPDATE);
        filter.addAction(TimerService.BROADCAST_ACTION_FINISH);
        LocalBroadcastManager.getInstance(context).registerReceiver(new TimerBroadcastReceiver(),
                filter);
        this.currentRunningProjectId = currentRunningProjectId;
        */
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
            runningProject.timerRunning = true;
            timingRunnable.begin();
        } else {
            // TODO: change values in SharedPreferences to default
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
                /*
                if (!project.timerRunning && currentRunningProjectId == 0) {
                    Intent intent = new Intent(activityContext, TimerService.class);
                    project.timerRunning = true;
                    currentRunningProjectId = project.id;
                    intent.putExtra(TimerService.EXTRA_KEY_ID, project.id);
                    intent.putExtra(TimerService.EXTRA_KEY_TOTAL_TIME,
                            project.timeSpentInMillis);
                    activityContext.startService(intent);
                    v.setActivated(true);
                } else if (project.timerRunning && currentRunningProjectId == project.id) {
                    activityContext.stopService(new Intent(activityContext, TimerService.class));
                    project.timerRunning = false;
                    currentRunningProjectId = 0;
                    v.setActivated(false);
                    dialogs.getNewPauseProjectDialogFragment(project, position)
                            .show(activityContext.getFragmentManager(), "pause");
                }
                ProjectAdapter.this.notifyItemChanged(position);
                */
                SharedPreferences preferences = activityContext.getPreferences(Context.MODE_PRIVATE);
                int currentlyRunningId = preferences.getInt(activityContext.getResources()
                        .getString(R.string.shared_preferences_current_id_key), -1);
                SharedPreferences.Editor editor = preferences.edit();
                if (currentlyRunningId == -1) { // if no project is currently running (play)
                    editor.putInt(activityContext.getResources().getString
                            (R.string.shared_preferences_current_id_key), project.id);
                    editor.putLong(activityContext.getResources().getString
                            (R.string.shared_preferences_begin_time_key), System.currentTimeMillis());
                    editor.apply();
                    timingRunnable.begin();
                    v.setActivated(true);
                    project.timerRunning = true;
                    ProjectAdapter.this.notifyItemChanged(position);
                } else if (currentlyRunningId == project.id) { // if this project is currently running (pause)
                    timingHandler.removeCallbacks(timingRunnable);
                    editor.putInt(activityContext.getResources().getString
                            (R.string.shared_preferences_current_id_key), -1);
                    editor.putLong(activityContext.getResources().getString
                            (R.string.shared_preferences_begin_time_key), -1);
                    editor.apply();
                    v.setActivated(false);
                    project.timerRunning = false;
                    dialogs.getNewPauseProjectDialogFragment(project, position)
                            .show(activityContext.getFragmentManager(), "pause");
                    ProjectAdapter.this.notifyItemChanged(position);
                }
            }
        });

        holder.background.setMaxHeight(holder.textLayout.getHeight());

        // TODO: use cache
        if (!project.wasChecked) {
            holder.background.setImageResource(R.color.colorPrimaryDark);
            FetchImageTask task = new FetchImageTask(project, holder.background, this);
            task.execute();
            project.wasChecked = true;
        } else {
            if (project.background != null) {
                holder.background.setImageDrawable(new BitmapDrawable(
                        activityContext.getResources(), project.background));
            } else {
                holder.background.setImageResource(R.color.colorPrimaryDark);
            }
        }

        holder.projectName.setText(project.name);
        holder.details.setText(createDetailsString(project));
        holder.timeSpent.setText(createTimeString(project));
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

    /*
    public class TimerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TimerService.BROADCAST_ACTION_UPDATE) ||
                    intent.getAction().equals(TimerService.BROADCAST_ACTION_FINISH)) {

                int id = intent.getIntExtra(TimerService.EXTRA_KEY_ID, -1);
                Project mProject = null;
                int index = -1;
                for (Project project : projects) {
                    index++;
                    if (project.id == id) {
                        mProject = project;
                        break;
                    }
                }

                if (mProject == null) {
                    // Project not found - was probably deleted
                    context.stopService(new Intent(activityContext, TimerService.class));
                    currentRunningProjectId = 0;
                    return;
                }

                mProject.timeSpentInMillis =
                        intent.getLongExtra(TimerService.EXTRA_KEY_TOTAL_TIME, 0);
                notifyItemChanged(index);
                activityContext.updateProject(mProject);
                // TODO: Make this update happen in onPause and when pause is pressed
            }
        }
    }
    */

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

    public void updateTime(int projectId, long timeDifference) {
        Project project = getProjectById(projectId);
        int index = getIndexOfProject(project);
        if (project != null && index != -1) {
            project.timeSpentInMillis += timeDifference;
            ProjectAdapter.this.notifyItemChanged(index);
        } else {
        }
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

    public class TimingRunnable implements Runnable { // TODO: make this into its own file

        private Handler myHandler;
        private ProjectAdapter adapter;
        private int currentlyRunningId;
        private long timeAtBeginning; // TODO: use timeAtBeginning (possibly not in runnable)
        private long timeAtLastUpdate;

        public TimingRunnable(Handler myHandler, ProjectAdapter adapter) {
            this.myHandler = myHandler;
            this.adapter = adapter;
        }

        public void begin() {
            SharedPreferences preferences = activityContext.getPreferences(Context.MODE_PRIVATE);
            currentlyRunningId = preferences.getInt(activityContext.getResources()
                    .getString(R.string.shared_preferences_current_id_key), -1);
            timeAtBeginning = preferences.getLong(activityContext.getResources()
                    .getString(R.string.shared_preferences_begin_time_key), -1);
            timeAtLastUpdate = -1;
            myHandler.post(this);
        }

        @Override
        public void run() {
            if (timeAtLastUpdate == -1) { // first run - check that it really should be running
                if (currentlyRunningId == -1 || timeAtBeginning == -1) {
                    // if values in SharedPreferences show that timer should not be running, we stop the timer
                    return;
                } else {
                    timeAtLastUpdate = System.currentTimeMillis();
                    myHandler.postDelayed(this, 1000);
                    return;
                }
            }
            long currentTime = System.currentTimeMillis();
            adapter.updateTime(currentlyRunningId, currentTime - timeAtLastUpdate);
            timeAtLastUpdate = currentTime;
            myHandler.postDelayed(this, 1000);
        }
    }
}
