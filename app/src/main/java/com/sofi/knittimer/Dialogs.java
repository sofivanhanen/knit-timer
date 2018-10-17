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

    EditTimeDialogFragment getNewEditTimeDialogFragment(TextView hours, TextView minutes, TextView seconds) {
        return new EditTimeDialogFragment(hours, minutes, seconds);
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

    public class EditTimeDialogFragment extends DialogFragment {

        private TextView hours;
        private TextView minutes;
        private TextView seconds;

        public EditTimeDialogFragment(TextView hours, TextView minutes, TextView seconds) {
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view = addProjectActivityContext.getLayoutInflater().inflate(R.layout.dialog_edit_time, null);

            final NumberPicker npHours = (NumberPicker) view.findViewById(R.id.np_hours);
            npHours.setMaxValue(999);
            npHours.setMinValue(0);
            npHours.setValue(Integer.parseInt(hours.getText() + ""));

            final NumberPicker npMinutes = (NumberPicker) view.findViewById(R.id.np_minutes);
            npMinutes.setMaxValue(59);
            npMinutes.setMinValue(0);
            npMinutes.setValue(Integer.parseInt(minutes.getText() + ""));

            final NumberPicker npSeconds = (NumberPicker) view.findViewById(R.id.np_seconds);
            npSeconds.setMaxValue(59);
            npSeconds.setMinValue(0);
            npSeconds.setValue(Integer.parseInt(seconds.getText() + ""));

            builder.setMessage(R.string.dialog_message_edit_time)
                    .setView(view)
                    .setPositiveButton(R.string.dialog_button_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            long timeInMillis = (long) npHours.getValue() * 1000 * 60 * 60
                                    + (long) npMinutes.getValue() * 1000 * 60
                                    + (long) npSeconds.getValue() * 1000;
                            addProjectActivityContext.changeTimeSpent(timeInMillis);
                        }
                    }).setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            Dialog dialog = builder.create();
            return dialog;
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
