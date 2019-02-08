package com.dennyy.oldschoolcompanion.customviews;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.helpers.Utils;

public class FloatingViewSeekBarPreference extends SeekBarPreference {

    private final String preview = "floating_view_preview";
    private LinearLayout base;

    public FloatingViewSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        base = (LinearLayout) super.onCreateDialogView();
        ImageView imageView = new ImageView(context);
        imageView.setTag(preview);
        imageView.setBackground(context.getResources().getDrawable(R.drawable.floating_view_size_preview));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        base.addView(imageView, 1, params);
        return base;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seek) {
        super.onStopTrackingTouch(seek);
        int pixels = (int) Utils.convertDpToPixel(10 + (seek.getProgress() * 5), context);
        updatePreview(pixels);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        int pixels = (int) Utils.convertDpToPixel(10 + (PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.PREF_SIZE, 8) * 5), context);
        updatePreview(pixels);
    }

    private void updatePreview(int pixels) {
        if (base == null) {
            return;
        }
        ImageView imageView = base.findViewWithTag(preview);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        params.width = pixels;
        params.height = pixels;
        imageView.requestLayout();
    }
}