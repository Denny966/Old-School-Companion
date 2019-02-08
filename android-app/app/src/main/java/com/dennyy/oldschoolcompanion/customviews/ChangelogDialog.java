package com.dennyy.oldschoolcompanion.customviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import com.dennyy.oldschoolcompanion.BuildConfig;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.adapters.ChangelogAdapter;
import com.dennyy.oldschoolcompanion.models.Changelog.Changelogs;


public class ChangelogDialog extends DialogFragment {

    public static final String ARG_CHANGELOGS = BuildConfig.APPLICATION_ID + ".changelog_dialog_changelogs";

    public ChangelogDialog() {

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        Changelogs changelogs = (Changelogs) args.getSerializable(ARG_CHANGELOGS);
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.changelog_layout, null);
        ListView listView = view.findViewById(R.id.changelog_listview);
        listView.setAdapter(new ChangelogAdapter(getActivity(), changelogs));
        return new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(getResources().getString(R.string.whats_new))
                .setView(listView)
                .setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            lp.width = (int) (metrics.widthPixels * 0.9);
            lp.height = (int) (metrics.heightPixels * 0.9);
            dialog.getWindow().setAttributes(lp);
        }
    }
}
