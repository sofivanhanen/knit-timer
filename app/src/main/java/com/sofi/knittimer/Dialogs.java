package com.sofi.knittimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.sofi.knittimer.data.Project;

public class Dialogs {

    static final int CAPTURE_PICTURE_REQUEST = 80;
    static final int CHOOSE_FROM_GALLERY_REQUEST = 81;

    private ProjectAdapter projectAdapterContext;
    private AddProjectActivity addProjectActivityContext;

    Dialogs(ProjectAdapter context) {
        projectAdapterContext = context;
    }

    Dialogs(AddProjectActivity context) {
        addProjectActivityContext = context;
    }

    DeleteProjectDialogFragment getNewDeleteProjectDialogFragment(Project project, int index, ActionMode actionMode) {
        return new DeleteProjectDialogFragment(project, index, actionMode);
    }

    AddPictureDialogFragment getNewAddPictureDialogFragment() {
        if (addProjectActivityContext != null) {
            return new AddPictureDialogFragment(addProjectActivityContext);
        } else {
            Log.w("Dialogs", "called getNewAddPictureDialogFragment, context was null");
            return null;
        }
    }

    public DebuggingDialog getNewDebuggingDialog() {
        return new DebuggingDialog();
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
                            if (projectAdapterContext.activityContext.deleteProject(mProject) >= 1) {
                                projectAdapterContext.projects.remove(mIndex);
                                projectAdapterContext.notifyItemRemoved(mIndex);
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

    public class AddPictureDialogFragment extends DialogFragment {

        private AddProjectActivity context;

        public AddPictureDialogFragment(AddProjectActivity context) {
            this.context = context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_message_add_picture)
                    /*
                    .setNeutralButton(R.string.dialog_button_take_picture, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.startImplicitIntent(CAPTURE_PICTURE_REQUEST);
                        }
                    })*/.setPositiveButton(R.string.dialog_button_choose_from_gallery,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.startImplicitIntent(CHOOSE_FROM_GALLERY_REQUEST);
                        }
                    }).setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            return builder.create();
        }
    }

    public static class DebuggingDialog extends DialogFragment {

        public DebuggingDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Preferences reset")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            return builder.create();
        }
    }

}
