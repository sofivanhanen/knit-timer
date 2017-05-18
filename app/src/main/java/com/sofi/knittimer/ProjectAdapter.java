package com.sofi.knittimer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    // TODO: Get data from a cursor (database)
    // Example projects
    private String[] names = new String[]{"Pink Socks", "Scarf for Friend",
            "Cute Alpaca Mittens for Friend's Birthday", "My Huge Scarf",
            "Big Hat for Mom", "Fancy Shawl"};
    private Context context;

    public ProjectAdapter(Context context) {
        this.context = context;
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
        TextView textView = holder.projectName;
        textView.setText(names[position]);
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView projectName;

        public ViewHolder(View itemView) {
            super(itemView);

            projectName = (TextView) itemView.findViewById(R.id.tv_project_name);
        }
    }
}
