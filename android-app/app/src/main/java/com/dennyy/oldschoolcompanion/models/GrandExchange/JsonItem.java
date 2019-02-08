package com.dennyy.oldschoolcompanion.models.GrandExchange;


import java.io.Serializable;

public class JsonItem implements Serializable {
    public String id;
    public String name;
    public String store;
    public boolean isMembers;
    public int limit;

    public int getId() {
        return Integer.parseInt(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
