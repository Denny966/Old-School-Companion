package com.dennyy.osrscompanion.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.dennyy.osrscompanion.R;

public class ClearableAutoCompleteTextView extends RelativeLayout implements TextWatcher, View.OnClickListener {
    private Button clearButton;
    private AutoCompleteTextView textView;
    private String hint;

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ClearableAutoCompleteTextView, 0, 0);
        try {
            hint = ta.getString(R.styleable.ClearableAutoCompleteTextView_hint);
        }
        finally {
            ta.recycle();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.clearable_autocompletetextview, this);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        clearButton = (Button) findViewById(R.id.autocomplete_textview_clear_button);
        clearButton.setOnClickListener(this);
        textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_textview);
        textView.setHint(hint);
        textView.addTextChangedListener(this);
    }

    public AutoCompleteTextView getAutoCompleteTextView() {
        return textView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().trim().length() == 0) {
            clearButton.setVisibility(GONE);
        }
        else {
            clearButton.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        textView.setText("");
    }
}
