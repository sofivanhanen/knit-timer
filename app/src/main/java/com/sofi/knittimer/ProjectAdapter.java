package com.sofi.knittimer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sofi.knittimer.data.DatabaseHandler;
import com.sofi.knittimer.data.Project;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private Context context;
    private DatabaseHandler mDatabaseHandler;
    private List<Project> projects;

    public void closeDatabase() {
        mDatabaseHandler.closeDatabaseConnection();
    }

    private void initListOfProjects() {
        projects = mDatabaseHandler.getAllProjects();
    }

    public ProjectAdapter(Context context, DatabaseHandler handler) {
        this.context = context;
        mDatabaseHandler = handler;
        initListOfProjects();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View projectView = inflater.inflate(R.layout.project_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(projectView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        initListOfProjects();

        Project project = projects.get(position);
        holder.projectName.setText(project._name);
        holder.details.setText(createDetailsString(project));
        holder.timeSpent.setText(createTimeString(project));
    }

    @Override
    public int getItemCount() {
        initListOfProjects();
        return projects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView projectName;
        public TextView details;
        public TextView timeSpent;

        public ViewHolder(View itemView) {
            super(itemView);

            projectName = (TextView) itemView.findViewById(R.id.tv_project_name);
            details = (TextView) itemView.findViewById(R.id.tv_details);
            timeSpent = (TextView) itemView.findViewById(R.id.tv_time_spent);
        }
    }

    private String createDetailsString(Project project) {
        int timeRemaining = project.timeLeftInMillis();
        int totalSeconds = timeRemaining / 1000;
        int totalMinutes = totalSeconds / 60;
        int littleMinutes = totalMinutes % 60; // total minutes - whole hours
        int totalHours = totalMinutes / 60;

        String details = project._percentageDone + "% done, " + totalHours;
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
        int timeSpent = project._timeSpentInMillis;
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
