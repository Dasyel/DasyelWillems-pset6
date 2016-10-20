package com.dasyel.dasyelwillems_pset6.models;

/**
 * A Friend represents another account which can get suggestions
 */
public class Friend {
    private String name;
    private String uid;

    public Friend(){}

    public Friend(String name, String uid){
        this.name = name;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }
}
