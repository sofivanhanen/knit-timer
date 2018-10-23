package com.sofi.knittimer.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.sofi.knittimer.MainActivity;
import com.sofi.knittimer.R;

public final class DialogUtils {

    public static class PercentageSetterDialogFragment extends DialogFragment {

        public interface PercentageSetterDialogListener {
            void onPercentageSetterDialogPositiveClick(int projectId, int newPercentage);
        }

        PercentageSetterDialogListener mListener;

        // Helper for bundling information
        public static PercentageSetterDialogFragment newInstance(int id, int percentage) {
            PercentageSetterDialogFragment fragment = new PercentageSetterDialogFragment();
            Bundle args = new Bundle();
            args.putInt(MainActivity.PROJECT_ID_KEY, id);
            args.putInt(MainActivity.PROJECT_PERCENT_KEY, percentage);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            try {
                mListener = (PercentageSetterDialogListener) context;
            } catch (ClassCastException exception) {
                throw new ClassCastException(context.toString() + " must implement PercentageSetterDialogListener");
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_edit_percentage, null);

            // Setup percentage picker
            final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
            numberPicker.setMaxValue(100);
            numberPicker.setMinValue(0);
            numberPicker.setValue(getArguments().getInt(MainActivity.PROJECT_PERCENT_KEY));

            builder.setMessage(R.string.dialog_message_get_progress)
                    .setView(view)
                    .setPositiveButton(R.string.dialog_button_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            mListener.onPercentageSetterDialogPositiveClick(getArguments().getInt(MainActivity.PROJECT_ID_KEY), numberPicker.getValue());
                        }
                    }).setNegativeButton(R.string.dialog_button_cancel, null);
            return builder.create();
        }
    }

    public static class TimeSetterDialogFragment extends DialogFragment {

        public interface TimeSetterDialogListener {
            void onTimeSetterDialogPositiveClick(long time);
        }

        TimeSetterDialogListener mListener;

        // Helper for bundling information
        public static TimeSetterDialogFragment newInstance(int hours, int minutes, int seconds) {
            TimeSetterDialogFragment fragment = new TimeSetterDialogFragment();
            Bundle args = new Bundle();
            args.putInt(MainActivity.HOURS_KEY, hours);
            args.putInt(MainActivity.MINUTES_KEY, minutes);
            args.putInt(MainActivity.SECONDS_KEY, seconds);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            try {
                mListener = (TimeSetterDialogListener) context;
            } catch (ClassCastException exception) {
                throw new ClassCastException(context.toString() + " must implement TimeSetterDialogListener");
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_edit_time, null);

            // Setup number pickers
            final NumberPicker npHours = (NumberPicker) view.findViewById(R.id.np_hours);
            npHours.setMaxValue(999);
            npHours.setMinValue(0);
            npHours.setValue(getArguments().getInt(MainActivity.HOURS_KEY));

            final NumberPicker npMinutes = (NumberPicker) view.findViewById(R.id.np_minutes);
            npMinutes.setMaxValue(59);
            npMinutes.setMinValue(0);
            npMinutes.setValue(getArguments().getInt(MainActivity.MINUTES_KEY));

            final NumberPicker npSeconds = (NumberPicker) view.findViewById(R.id.np_seconds);
            npSeconds.setMaxValue(59);
            npSeconds.setMinValue(0);
            npSeconds.setValue(getArguments().getInt(MainActivity.SECONDS_KEY));

            builder.setMessage(R.string.dialog_message_edit_time)
                    .setView(view)
                    .setPositiveButton(R.string.dialog_button_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            long timeInMillis = (long) npHours.getValue() * 1000 * 60 * 60
                                    + (long) npMinutes.getValue() * 1000 * 60
                                    + (long) npSeconds.getValue() * 1000;
                            mListener.onTimeSetterDialogPositiveClick(timeInMillis);
                        }
                    }).setNegativeButton(R.string.dialog_button_cancel, null);
            return builder.create();
        }
    }

    public static class AddPictureDialogFragment extends DialogFragment {

        public interface AddPictureDialogListener {
            void onAddPictureDialogPositiveClick();
        }

        AddPictureDialogListener mListener;

        // Helper for bundling information
        public static AddPictureDialogFragment newInstance() {
            AddPictureDialogFragment fragment = new AddPictureDialogFragment();
            return fragment;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            try {
                mListener = (AddPictureDialogListener) context;
            } catch (ClassCastException exception) {
                throw new ClassCastException(context.toString() + " must implement PercentageSetterDialogListener");
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_message_add_picture)
                    /* // TODO Enable taking a picture
                    .setNeutralButton(R.string.dialog_button_take_picture, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.startImplicitIntent(CAPTURE_PICTURE_REQUEST);
                        }
                    })*/
                    .setPositiveButton(R.string.dialog_button_choose_from_gallery,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.onAddPictureDialogPositiveClick();
                        }
                    }).setNegativeButton(R.string.dialog_button_cancel, null);
            return builder.create();
        }
    }

}
