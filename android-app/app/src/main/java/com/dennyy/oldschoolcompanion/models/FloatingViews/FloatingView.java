package com.dennyy.oldschoolcompanion.models.FloatingViews;

public class FloatingView implements Comparable<FloatingView> {
    public final String id;
    public final String name;
    public final int drawableId;
    public final int layoutId;

    private int sortOrder;
    private boolean isSelected;

    public FloatingView(String id, String name, int drawableId, int layoutId) {
        this.id = id;
        this.name = name;
        this.drawableId = drawableId;
        this.layoutId = layoutId;
        this.sortOrder = Integer.MAX_VALUE;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int compareTo(FloatingView o) {

        if (this.sortOrder > o.getSortOrder()) {
            return 1;
        }
        else if (this.sortOrder < o.getSortOrder()) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
