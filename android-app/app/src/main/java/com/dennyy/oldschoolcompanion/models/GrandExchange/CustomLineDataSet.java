package com.dennyy.oldschoolcompanion.models.GrandExchange;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;


public class CustomLineDataSet extends LineDataSet {
    public CustomLineDataSet(List<Entry> yVals, String label, int color) {
        super(yVals, label);
        setAxisDependency(YAxis.AxisDependency.LEFT);
        setColor(color);
        setDrawCircles(false);
        setCircleRadius(2f);
        setCircleColor(color);
        setLineWidth(2f);
        setDrawCircleHole(false);
        setDrawValues(false);
        setDrawHighlightIndicators(false);
    }
}
