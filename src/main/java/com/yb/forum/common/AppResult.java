package com.yb.forum.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @Author 比特就业课
 */

public class AppResult<T> {

    // 状态码
    @JsonInclude(JsonInclude.Include.ALWAYS) // 不论任何情况都参与JSON序列化
    private int code;
    // 描述信息
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String message;
    // 具体的数据
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private T data;

    /**
     * 构造方法
     * @param code
     * @param message
     */
    public AppResult(int code, String message) {
        this(code, message, null);
    }

    public AppResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功
     * @return
     */
    public static AppResult success () {
        return new AppResult(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    public static AppResult success (String message) {
        return new AppResult(ResultCode.SUCCESS.getCode(), message);
    }

    public static <T> AppResult<T> success (T data) {
        return new AppResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> AppResult<T> success (String message, T data) {
        return new AppResult<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败
     * @return
     */
    public static AppResult failed () {
        return new AppResult(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage());
    }

    public static AppResult failed (String message) {
        return new AppResult(ResultCode.FAILED.getCode(), message);
    }

    public static AppResult failed (ResultCode resultCode) {
        return new AppResult(resultCode.getCode(), resultCode.getMessage());
    }

    public static AppResult failed (ResultCode resultCode, String message) {
        return new AppResult(resultCode.getCode(), message);
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}
