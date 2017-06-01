package com.sofi.knittimer;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sofi.knittimer.data.Project;
import com.sofi.knittimer.data.ProjectContract;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    public MainActivity mContext;
    private List<Project> projects;

    private ActionMode mActionMode;
    private int selectedItemIndex;

    private boolean serviceIsRunning;

    final String TAG_CLICKED = "clicked";
    final String TAG_NOT_CLICKED = "not clicked";

    public Intent timerServiceIntent;

    public ProjectAdapter(MainActivity context) {
        mContext = context;
        projects = new ArrayList<Project>();
        serviceIsRunning = false;
        IntentFilter filter = new IntentFilter(TimerService.BROADCAST_ACTION_UPDATE);
        filter.addAction(TimerService.BROADCAST_ACTION_FINISH);
        LocalBroadcastManager.getInstance(context).registerReceiver(new TimerBroadcastReceiver(), filter);
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == null) {
            projects = new ArrayList<Project>();
            this.notifyDataSetChanged();
            return;
        }
        List<Project> oldProjects = projects;
        projects = new ArrayList<Project>();
        if (newCursor.moveToFirst()) {
            do {
                projects.add(new Project(newCursor.getInt(0), newCursor.getString(1),
                        newCursor.getInt(2), newCursor.getInt(3)));
            } while (newCursor.moveToNext());
        }
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View projectView = inflater.inflate(R.layout.project_list_item, parent, false);
        projectView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mActionMode != null) {
                    return false;
                }
                selectedItemIndex = mContext.getRecyclerView().getChildLayoutPosition(v);
                mActionMode = mContext.startActionMode(new mActionModeCallback());
                v.setSelected(true);
                return true;
            }
        });

        ViewHolder viewHolder = new ViewHolder(projectView);
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
                    if (mContext.deleteProject(projects.get(selectedItemIndex)) == 1) {
                        mode.finish();
                        projects.remove(selectedItemIndex);
                        notifyItemRemoved(selectedItemIndex);
                        return true;
                    } else {
                        Log.e("onActionItemClicked", "More or less than 1 item deleted!!!");
                    }
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (projects == null) {
            return;
        }

        final Project project = projects.get(position);
        holder.projectName.setText(project.name);
        holder.details.setText(createDetailsString(project));
        holder.timeSpent.setText(createTimeString(project));
        final Intent mTimerIntent = new Intent(mContext, TimerService.class);
        timerServiceIntent = mTimerIntent;

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals(TAG_NOT_CLICKED) && !serviceIsRunning) {
                    mTimerIntent.putExtra(TimerService.EXTRA_KEY_ID, project.id);
                    mTimerIntent.putExtra(TimerService.EXTRA_KEY_TIME_LEFT, project.timeSpentInMillis);
                    mContext.startService(mTimerIntent);
                    ((ImageView) v).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_circle));
                    v.setTag(TAG_CLICKED);
                    serviceIsRunning = true;
                } else if (v.getTag().equals(TAG_CLICKED) && serviceIsRunning) {
                    mContext.stopService(mTimerIntent);
                    ((ImageView) v).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_circle));
                    v.setTag(TAG_NOT_CLICKED);
                    serviceIsRunning = false;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (projects == null) {
            throw new NullPointerException("GetItemCount got has null list");
        }
        return projects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView projectName;
        public TextView details;
        public TextView timeSpent;
        public ImageView button;

        public ViewHolder(View itemView) {
            super(itemView);

            projectName = (TextView) itemView.findViewById(R.id.tv_project_name);
            details = (TextView) itemView.findViewById(R.id.tv_details);
            timeSpent = (TextView) itemView.findViewById(R.id.tv_time_spent);
            button = (ImageView) itemView.findViewById(R.id.iv_play);
        }
    }

    public class TimerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TimerService.BROADCAST_ACTION_UPDATE) || intent.getAction().equals(TimerService.BROADCAST_ACTION_FINISH)) {
                int id = intent.getIntExtra(TimerService.EXTRA_KEY_ID, 0);
                Project mProject = null;
                for (Project project : projects) {
                    if (project.id == id) {
                        mProject = project;
                    }
                }

                if (mProject == null) { // Couldn't find project with given id - project was probably deleted
                    context.stopService(timerServiceIntent);
                    serviceIsRunning = false;
                    return;
                }

                mProject.timeSpentInMillis = intent.getIntExtra(TimerService.EXTRA_KEY_TIME_LEFT, 0);
                mContext.updateProject(mProject);
                notifyDataSetChanged();
            }
        }
    }

    ;

    private String createDetailsString(Project project) {
        int timeRemaining = project.timeLeftInMillis();

        if (timeRemaining == 0) { // Project hasn't been started (% is still set to 0)
            return "0% done. Let's get to work!";
        }

        int totalSeconds = timeRemaining / 1000;
        int totalMinutes = totalSeconds / 60;
        int littleMinutes = totalMinutes % 60; // total minutes - whole hours
        int totalHours = totalMinutes / 60;

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
        int timeSpent = project.timeSpentInMillis;
        int totalSeconds = timeSpent / 1000;
        int littleSeconds = totalSeconds % 60;
        int totalMinutes = totalSeconds / 60;
        int littleMinutes = totalMinutes % 60;
        int totalHours = totalMinutes / 60;

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
