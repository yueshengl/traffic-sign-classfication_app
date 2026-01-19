package com.example.trafficsignrecognition.util;

public class TextSizeUtil {

    public static int getCommonFontSize(int level)
    {
        return ConstantUtil.TEXT_SIZE + (level-2)*3;
    }

    public static int getAlertSize(int level)
    {
        return ConstantUtil.ALERT_SIZE + (level-2);
    }
}
