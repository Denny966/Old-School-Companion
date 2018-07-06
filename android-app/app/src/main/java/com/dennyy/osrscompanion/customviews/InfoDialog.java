package com.dennyy.osrscompanion.customviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.WindowManager;

import com.dennyy.osrscompanion.R;

public class InfoDialog extends DialogFragment {
    public static final String ARG_TITLE = "InfoDialog.Title";
    public static final String ARG_MESSAGE = "InfoDialog.Message";
    public static final String ARG_SHOW_CANCEL = "InfoDialog.ShowCancel";

    public InfoDialog() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(ARG_TITLE);
        String message = args.getString(ARG_MESSAGE);
        boolean showCancelButton = args.getBoolean(ARG_SHOW_CANCEL, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok,null);
        if (showCancelButton)
            builder.setNegativeButton(android.R.string.no, null);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }
    }
}
