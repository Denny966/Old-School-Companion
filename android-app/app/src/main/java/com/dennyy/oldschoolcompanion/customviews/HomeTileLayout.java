package com.dennyy.oldschoolcompanion.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dennyy.oldschoolcompanion.R;

public class HomeTileLayout extends RelativeLayout {

    private String tileText;
    private Drawable tileDrawable;

    public HomeTileLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HomeTileLayout, 0, 0);
        try {
            tileText = ta.getString(R.styleable.HomeTileLayout_text);
            tileDrawable = ta.getDrawable(R.styleable.HomeTileLayout_drawable);
        }
        finally {
            ta.recycle();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.home_tile_layout, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        TextView tileTextView = findViewById(R.id.home_tile_textview);
        ImageView tileImageView = findViewById(R.id.home_tile_drawable);

        tileTextView.setText(tileText);
        tileImageView.setBackground(tileDrawable);
    }


}
