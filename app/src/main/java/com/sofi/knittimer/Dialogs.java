package com.sofi.knittimer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.sofi.knittimer.data.Project;

/**
 * Created by Default User on 1.6.2017.
 */

public class Dialogs {

    public static final int CAPTURE_PICTURE_REQUEST = 80;
    public static final int CHOOSE_FROM_GALLERY_REQUEST = 81;

    ProjectAdapter projectAdapterContext;
    AddProjectActivity addProjectActivityContext;

    public Dialogs(ProjectAdapter context) {
        projectAdapterContext = context;
    }

    public Dialogs(AddProjectActivity context) {
        addProjectActivityContext = context;
    }

    public DeleteProjectDialogFragment getNewDeleteProjectDialogFragment(Project project, int index, ActionMode actionMode) {
        return new DeleteProjectDialogFragment(project, index, actionMode);
    }

    public AddPictureDialogFragment getNewAddPictureDialogFragment(Bitmap bitmap) {
        if (addProjectActivityContext != null) {
            return new AddPictureDialogFragment(addProjectActivityContext, bitmap);
        } else {
            Log.w("Dialogs", "called getNewAddPictureDialogFragment, context was null");
            return null;
        }
    }

    public PauseProjectDialogFragment getNewPauseProjectDialogFragment(Project project, int index) {
        return new PauseProjectDialogFragment(project, index);
    }

    public PauseProjectDialogFragment getNewPauseProjectDialogFragment(TextView textView) {
        return new PauseProjectDialogFragment(textView);
    }

    public EditTimeDialogFragment getNewEditTimeDialogFragment(TextView hours, TextView minutes, TextView seconds) {
        return new EditTimeDialogFragment(hours, minutes, seconds);
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

        private Bitmap bitmap;
        private AddProjectActivity context;

        public AddPictureDialogFragment(AddProjectActivity context, Bitmap bitmap) {
            this.context = context;
            this.bitmap = bitmap;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_message_add_picture)
                    .setPositiveButton(R.string.dialog_button_take_picture, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.startImplicitIntent(CAPTURE_PICTURE_REQUEST);
                        }
                    }).setNeutralButton(R.string.dialog_button_choose_from_gallery,
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

    public class PauseProjectDialogFragment extends DialogFragment {

        private Project mProject;
        private int mIndex;

        private TextView mPercentTv;

        public PauseProjectDialogFragment(Project project, int index) {
            mProject = project;
            mIndex = index;
        }

        public PauseProjectDialogFragment(TextView percentTv) {
            mPercentTv = percentTv;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view = null;
            if (projectAdapterContext != null) {
                view = projectAdapterContext.activityContext.getLayoutInflater().inflate(R.layout.dialog_pause, null);
            } else if (addProjectActivityContext != null) {
                view = addProjectActivityContext.getLayoutInflater().inflate(R.layout.dialog_pause, null);
            }
            final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
            numberPicker.setMaxValue(100);
            numberPicker.setMinValue(0);
            if (mProject != null) {
                numberPicker.setValue(mProject.percentageDone);
            } else if (mPercentTv != null) {
                numberPicker.setValue(Integer.parseInt(mPercentTv.getTag() + ""));
            }
            builder.setMessage(R.string.dialog_message_get_progress)
                    .setView(view)
                    .setPositiveButton(R.string.dialog_button_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mProject != null) {
                                mProject.percentageDone = numberPicker.getValue();
                                if (projectAdapterContext.activityContext.updateProject(mProject) >= 1) {
                                    projectAdapterContext.notifyItemChanged(mIndex);
                                }
                            } else if (mPercentTv != null) {
                                mPercentTv.setText(numberPicker.getValue() + "%");
                                mPercentTv.setTag(numberPicker.getValue());
                            }
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
                            if (npHours.getValue() < 10) {
                                hours.setText("0" + npHours.getValue());
                            } else {
                                hours.setText(npHours.getValue() + "");
                            }
                            if (npMinutes.getValue() < 10) {
                                minutes.setText("0" + npMinutes.getValue());
                            } else {
                                minutes.setText(npMinutes.getValue() + "");
                            }
                            if (npSeconds.getValue() < 10) {
                                seconds.setText("0" + npSeconds.getValue());
                            } else {
                                seconds.setText(npSeconds.getValue() + "");
                            }
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

}
