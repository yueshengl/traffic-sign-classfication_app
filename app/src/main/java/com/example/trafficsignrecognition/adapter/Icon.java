package com.example.trafficsignrecognition.adapter;

public class Icon {
    private final int iconId;
    private final String iconName;
    private final int iconNum;

    public Icon(int iId, String iName, int iNum) {
        this.iconId = iId;
        this.iconName = iName;
        this.iconNum = iNum;
    }

    public int getIconId() {
        return iconId;
    }

    public String getIconName() {
        return iconName;
    }

    public int getIconNum() {
        return iconNum;
    }

}
