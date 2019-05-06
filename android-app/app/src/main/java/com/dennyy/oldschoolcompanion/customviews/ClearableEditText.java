package com.dennyy.oldschoolcompanion.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.helpers.Utils;
import com.dennyy.oldschoolcompanion.interfaces.ClearableEditTextListener;


public class ClearableEditText extends RelativeLayout implements TextWatcher, View.OnClickListener {
    private final static String INSTANCE_STATE_KEY = "instance_state";
    private final static String TEXT_KEY = "text_key";

    boolean flagNoExtractUiOn;
    private Button clearButton;
    private EditText editText;
    private String hint;
    private ClearableEditTextListener listener;

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
        clearButton = findViewWithTag(getResources().getString(R.string.clearable_edittext_clear_button));
        clearButton.setOnClickListener(this);
        editText = findViewWithTag(getResources().getString(R.string.clearable_edittext));
        editText.addTextChangedListener(this);
        editText.setHint(hint);

        if (flagNoExtractUiOn)
            getEditText().setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
    }

    public EditText getEditText() {
        return editText;
    }

    public void setListener(ClearableEditTextListener listener) {
        this.listener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString();
        if (text.trim().length() == 0) {
            clearButton.setVisibility(GONE);
        }
        else {
            clearButton.setVisibility(VISIBLE);
        }
        if (listener != null) {
            listener.onClearableEditTextTextChanged(text, Utils.isNullOrEmpty(text));
        }
    }

    @Override
    public void onClick(View v) {
        editText.setText("");
        if (listener != null) {
            listener.onClearableEditTextClear();
        }
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE_KEY, super.onSaveInstanceState());
        bundle.putString(TEXT_KEY, editText.getText().toString());
        return bundle;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            editText.setText(bundle.getString(TEXT_KEY));
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE_KEY));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
        this.editText.setOnTouchListener(l);
    }
}

