package com.example.trafficsignrecognition.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public class UsernameUtil {

    //自动生成名字（中文）
    public static String getRandomJianHan(int len) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String str = null;
            int heightPose, lowPos; // 定义高低位
            Random random = new Random();
            heightPose = (176 + Math.abs(random.nextInt(39))); // 获取高位值
            lowPos = (161 + Math.abs(random.nextInt(93))); // 获取低位值
            byte[] b = new byte[2];
            b[0] = (Integer.valueOf(heightPose).byteValue());
            b[1] = (Integer.valueOf(lowPos).byteValue());
            try {
                str = new String(b, "GBK"); // 转成中文
            } catch (UnsupportedEncodingException ex) {
                Log.d("UsernameUtil","getRandomJianHan Error",ex);
            }
            ret.append(str);
        }
        return ret.toString();
    }

    //生成随机用户名，数字和字母组成,
    public static String getStringRandom(int length) {

        StringBuilder val = new StringBuilder();
        Random random = new Random();

        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {

            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val.append((char) (random.nextInt(26) + temp));
            } else {
                val.append(random.nextInt(10));
            }
        }
        return val.toString();
    }
}
