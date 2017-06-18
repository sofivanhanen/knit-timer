package com.sofi.knittimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
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
import com.sofi.knittimer.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public MainActivity activityContext;
    public List<Project> projects;

    public int currentRunningProjectId;
    private ActionMode mActionMode;
    private int selectedItemIndex;
    private View selectedView;

    public Dialogs dialogs;

    public ProjectAdapter(MainActivity context, int currentRunningProjectId) {
        activityContext = context;
        projects = new ArrayList<Project>();
        dialogs = new Dialogs(this);
        IntentFilter filter = new IntentFilter(TimerService.BROADCAST_ACTION_UPDATE);
        filter.addAction(TimerService.BROADCAST_ACTION_FINISH);
        LocalBroadcastManager.getInstance(context).registerReceiver(new TimerBroadcastReceiver(),
                filter);
        this.currentRunningProjectId = currentRunningProjectId;
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
        for (Project project : projects) {
            if (project.id == currentRunningProjectId) {
                project.serviceRunning = true;
            }
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

        if (project.serviceRunning) {
            holder.button.setActivated(true);
        } else {
            holder.button.setActivated(false);
        }

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!project.serviceRunning && currentRunningProjectId == 0) {
                    Intent intent = new Intent(activityContext, TimerService.class);
                    project.serviceRunning = true;
                    currentRunningProjectId = project.id;
                    intent.putExtra(TimerService.EXTRA_KEY_ID, project.id);
                    intent.putExtra(TimerService.EXTRA_KEY_TOTAL_TIME,
                            project.timeSpentInMillis);
                    activityContext.startService(intent);
                    v.setActivated(true);
                } else if (project.serviceRunning && currentRunningProjectId == project.id) {
                    activityContext.stopService(new Intent(activityContext, TimerService.class));
                    project.serviceRunning = false;
                    currentRunningProjectId = 0;
                    v.setActivated(false);
                    dialogs.getNewPauseProjectDialogFragment(project, position)
                            .show(activityContext.getFragmentManager(), "pause");
                }
                ProjectAdapter.this.notifyItemChanged(position);
            }
        });

        holder.background.setMaxHeight(holder.textLayout.getHeight());

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


    private String createDetailsString(Project project) {
        if (project.serviceRunning) {
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
}
