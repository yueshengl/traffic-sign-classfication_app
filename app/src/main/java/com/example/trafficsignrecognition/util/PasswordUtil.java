package com.example.trafficsignrecognition.util;

import java.util.regex.Pattern;

/**验证规则
 ^: 匹配字符串的开头
 (?=.*[a-z]): 正向预查，表示密码中至少包含一个小写字母
 (?=.*[A-Z]): 正向预查，表示密码中至少包含一个大写字母
 (?=.*\\d): 正向预查，表示密码中至少包含一个数字
 [a-zA-Z\\d]{6,}: 表示匹配长度至少为6位，只包含大小写字母和数字的字符串
 $: 匹配字符串的结尾
 **/
public class PasswordUtil {

    //判断密码是否合理
    public static boolean isPasswordValid(String password) {
        // 密码长度至少6位，包含大小写字母和数字
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$";
        return Pattern.matches(pattern, password);
    }

}


