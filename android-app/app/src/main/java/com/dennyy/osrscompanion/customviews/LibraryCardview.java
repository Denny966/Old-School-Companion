package com.dennyy.osrscompanion.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;


public class LibraryCardview extends LinearLayout {

    private String author;
    private String url;
    private String title;

    public LibraryCardview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.LibraryCardview, 0, 0);
        try {
            title = ta.getString(R.styleable.LibraryCardview_title);
            author = ta.getString(R.styleable.LibraryCardview_author);
            url = ta.getString(R.styleable.LibraryCardview_url);
        }
        finally {
            ta.recycle();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.library_cardview_layout, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ((TextView) findViewById(R.id.library_cardview_title)).setText(title);
        ((TextView) findViewById(R.id.library_cardview_author)).setText(getResources().getString(R.string.by, author));
    }

    public String getUrl() {
        return url;
    }
}
