package com.sofi.knittimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ActionMode;

import com.sofi.knittimer.data.Project;

/**
 * Created by Default User on 1.6.2017.
 */

public class Dialogs {

    ProjectAdapter mContext;

    public Dialogs(ProjectAdapter context) {
        mContext = context;
    }

    public DeleteProjectDialogFragment getNewDeleteProjectDialogFragment(Project project, int index, ActionMode actionMode) {
        return new DeleteProjectDialogFragment(project, index, actionMode);
    }

    public class DeleteProjectDialogFragment extends DialogFragment {

        private Project mProject;
        private int mIndex;
        private ActionMode mActionMode;

        public DeleteProjectDialogFragment(Project project, int index, ActionMode mode) {
            mProject = project;
            mIndex = index;
            mActionMode = mode;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_message_delete)
                    .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mContext.activityContext.deleteProject(mProject) == 1) {
                                mContext.projects.remove(mIndex);
                                mContext.notifyItemRemoved(mIndex);
                            }
                            if (mActionMode != null) {
                                mActionMode.finish();
                                mActionMode = null;
                            }
                        }
                    }).setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            return builder.create();
        }
    }

}
