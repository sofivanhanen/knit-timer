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
            void onPauseDialogPositiveClick(int projectId, int newPercentage);
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
            View view = inflater.inflate(R.layout.dialog_pause, null);

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
                            mListener.onPauseDialogPositiveClick(getArguments().getInt(MainActivity.PROJECT_ID_KEY), numberPicker.getValue());
                        }
                    }).setNegativeButton(R.string.dialog_button_cancel, null);
            return builder.create();
        }
    }

}
