package com.dennyy.oldschoolcompanion.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dennyy.oldschoolcompanion.R;

public class NpcPropertyLayout extends LinearLayout {
    private TextView propertyTextView;
    private TextView valueTextView;
    private String text;

    public NpcPropertyLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.NpcPropertyLayout, 0, 0);
        try {
            text = ta.getString(R.styleable.NpcPropertyLayout_propertyName);
        }
        finally {
            ta.recycle();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.npc_property_layout, this);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        propertyTextView = findViewById(R.id.npc_property);
        valueTextView = findViewById(R.id.npc_property_value);
        propertyTextView.setText(text);
    }

    public void setText(String text) {
        propertyTextView.setText(text);
    }

    public void setValue(String value) {
        valueTextView.setText(value);
    }
}
