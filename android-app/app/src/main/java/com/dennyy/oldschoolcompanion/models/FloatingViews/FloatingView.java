package com.dennyy.oldschoolcompanion.models.FloatingViews;

public class FloatingView implements Comparable<FloatingView> {
    public final String id;
    public final String name;
    public final int drawableId;
    public final int layoutId;
    public final boolean isCustomView;

    private int sortOrder;
    private boolean isSelected;
    private String url;

    public FloatingView(String id, String name, int drawableId, int layoutId, boolean isCustomView) {
        this.id = id;
        this.name = name;
        this.drawableId = drawableId;
        this.layoutId = layoutId;
        this.isCustomView = isCustomView;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
            return this.isCustomView == o.isCustomView ? 0 : this.isCustomView ? 1 : -1;
        }
    }
}
