package com.dennyy.oldschoolcompanion.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.HiscoreType;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.interfaces.HiscoreTypeSelectedListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HiscoreTypeSelectorLayout extends LinearLayout implements View.OnClickListener {

    private HiscoreType hiscoreType;
    private HiscoreTypeSelectedListener listener;
    private HashMap<HiscoreType, Integer> indicators;

    public HiscoreTypeSelectorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HiscoreTypeSelectorLayout, 0, 0);
        try {
            hiscoreType = HiscoreType.fromValue(ta.getInt(R.styleable.HiscoreTypeSelectorLayout_selectedType, HiscoreType.NORMAL.getValue()));
        }
        finally {
            ta.recycle();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.hiscore_type_selector_layout, this);

        indicators = new HashMap<>();
        indicators.put(HiscoreType.NORMAL, R.id.hiscores_normal);
        indicators.put(HiscoreType.IRONMAN, R.id.hiscores_ironman);
        indicators.put(HiscoreType.HCIM, R.id.hiscores_hardcore_ironman);
        indicators.put(HiscoreType.UIM, R.id.hiscores_ultimate_ironman);
        indicators.put(HiscoreType.DMM, R.id.hiscores_dmm);
        indicators.put(HiscoreType.SDMM, R.id.hiscores_sdmm);
        for (Map.Entry<HiscoreType, Integer> entry : indicators.entrySet()) {
            findViewById(entry.getValue()).setOnClickListener(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int viewId = indicators.get(hiscoreType);
        LineIndicatorButton selected = findViewById(viewId);
        selected.setActive(true);
    }

    public HiscoreType getHiscoreType() {
        return hiscoreType;
    }

    public String getHiscoresUrl() {
        String url;
        switch (hiscoreType) {
            case UIM:
                url = Constants.RS_HISCORES_UIM_URL;
                break;
            case IRONMAN:
                url = Constants.RS_HISCORES_IRONMAN_URL;
                break;
            case HCIM:
                url = Constants.RS_HISCORES_HCIM_URL;
                break;
            case DMM:
                url = Constants.RS_HISCORES_DMM_URL;
                break;
            case SDMM:
                url = Constants.RS_HISCORES_SDMM_URL;
                break;
            default:
                url = Constants.RS_HISCORES_URL;
        }
        return url;
    }

    public void setOnTypeSelectedListener(HiscoreTypeSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (listener == null) {
            return;
        }
        int id = view.getId();
        HiscoreType newHiscoreType = getKeyByValue(indicators, id);
        if (hiscoreType == null) {
            return;
        }
        hiscoreType = newHiscoreType;
        updateIndicators();
        listener.onHiscoreTypeSelected(newHiscoreType);
    }

    public void setHiscoreType(HiscoreType hiscoreType) {
        this.hiscoreType = hiscoreType;
        updateIndicators();
    }

    private void updateIndicators() {
        for (Map.Entry<HiscoreType, Integer> entry : indicators.entrySet()) {
            ((LineIndicatorButton) findViewById(entry.getValue())).setActive(false);
        }
        ((LineIndicatorButton) findViewById(indicators.get(hiscoreType))).setActive(true);
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}