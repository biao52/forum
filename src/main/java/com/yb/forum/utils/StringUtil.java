package com.yb.forum.utils;

/**
 * @Author 比特就业课
 */

public class StringUtil {

    /**
     * 判断字符串是否为空
     *
     * @param value 要判断的字符串
     * @return true 空  <br/> false 非空
     */
    public static boolean isEmpty (String value) {
        return value == null || value.length() == 0;
    }
}
