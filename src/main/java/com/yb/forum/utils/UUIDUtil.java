package com.yb.forum.utils;

import java.util.UUID;

/**
 * @Author 比特就业课
 */

public class UUIDUtil {

    /**
     * 生成一个标准的UUID
     *
     * @return
     */
    public static String UUID_36 () {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成一个32位的UUID
     * @return
     */
    public static String UUID_32 () {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
