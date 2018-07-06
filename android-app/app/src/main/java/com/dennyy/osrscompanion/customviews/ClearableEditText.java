package com.dennyy.osrscompanion.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dennyy.osrscompanion.R;


public class ClearableEditText extends RelativeLayout implements TextWatcher, View.OnClickListener {
    boolean flagNoExtractUiOn;
    private Button clearButton;
    private EditText editText;
private String hint;
    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ClearableEditText, 0, 0);
        try {
            flagNoExtractUiOn = ta.getBoolean(R.styleable.ClearableEditText_flagNoExtractUiOn, false);
        hint = ta.getString(R.styleable.ClearableEditText_hint);
        }
        finally {
            ta.recycle();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.clearable_edittext, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        clearButton = (Button) findViewById(R.id.clearable_edittext_clear_button);
        clearButton.setOnClickListener(this);
        editText = (EditText) findViewById(R.id.clearable_edittext);
        editText.addTextChangedListener(this);
        editText.setHint(hint);

        if (flagNoExtractUiOn)
            ((EditText) findViewById(R.id.clearable_edittext)).setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
    }
    public EditText getEditText() {
        return editText;
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
        editText.setText("");
    }
}

