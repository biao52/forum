package com.yb.forum.utils;

import java.util.regex.Pattern;

/**
 * 参数校验工具类
 * 用于注册、修改等场景的参数验证
 * 
 * @author yangbiao
 */
public class ValidationUtil {

    /**
     * 用户名正则：
     * - 长度 4-20 位
     * - 只能包含字母、数字、下划线、中划线
     * - 不能以数字开头
     * - 不能包含连续的特殊字符
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{3,19}$");

    /**
     * 昵称正则：
     * - 长度 2-20 位
     * - 可以包含中文、字母、数字
     * - 不能包含特殊字符
     */
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9]{2,20}$");

    /**
     * 密码强度要求：
     * - 长度至少 6 位
     * - 包含字母和数字
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$");

    /**
     * 最大长度限制
     */
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MAX_NICKNAME_LENGTH = 20;
    private static final int MAX_PASSWORD_LENGTH = 32;

    /**
     * 校验用户名
     * 规则：
     * 1. 不能为空
     * 2. 长度 4-20 位
     * 3. 只能包含字母、数字、下划线、中划线
     * 4. 不能以数字开头
     * 
     * @param username 用户名
     * @return 校验结果，null 表示成功，否则返回错误信息
     */
    public static String validateUsername(String username) {
        if (StringUtil.isEmpty(username)) {
            return "用户名不能为空";
        }

        // 去除首尾空格
        username = username.trim();

        // 检查长度
        if (username.length() < 4) {
            return "用户名长度至少为 4 位";
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            return "用户名长度不能超过 20 位";
        }

        // 检查是否包含非法字符
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "用户名只能包含字母、数字、下划线，且不能以数字开头";
        }

        return null;
    }

    /**
     * 校验昵称
     * 规则：
     * 1. 不能为空
     * 2. 长度 2-20 位
     * 3. 可以包含中文、字母、数字
     * 
     * @param nickname 昵称
     * @return 校验结果，null 表示成功，否则返回错误信息
     */
    public static String validateNickname(String nickname) {
        if (StringUtil.isEmpty(nickname)) {
            return "昵称不能为空";
        }

        // 去除首尾空格
        nickname = nickname.trim();

        // 检查长度
        if (nickname.length() < 2) {
            return "昵称长度至少为 2 位";
        }
        if (nickname.length() > MAX_NICKNAME_LENGTH) {
            return "昵称长度不能超过 20 位";
        }

        // 检查是否包含非法字符
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            return "昵称只能包含中文、字母、数字";
        }

        return null;
    }

    /**
     * 校验密码
     * 规则：
     * 1. 不能为空
     * 2. 长度至少 6 位
     * 3. 必须包含数字、大写字母、小写字母
     * 
     * @param password 密码
     * @return 校验结果，null 表示成功，否则返回错误信息
     */
    public static String validatePassword(String password) {
        if (StringUtil.isEmpty(password)) {
            return "密码不能为空";
        }

        // 检查长度
        if (password.length() < 6) {
            return "密码长度至少为 6 位";
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return "密码长度不能超过 32 位";
        }

        // 检查密码强度：必须包含数字、大写字母、小写字母
        boolean hasDigit = false;
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            }
        }
        
        // 强度检查：必须同时包含数字、大写字母、小写字母
        if (!(hasDigit && hasUpperCase && hasLowerCase)) {
            return "密码强度不足";
        }

        return null;
    }

    /**
     * 综合校验注册参数
     * 
     * @param username 用户名
     * @param nickname 昵称
     * @param password 密码
     * @param passwordRepeat 确认密码
     * @return 校验结果，null 表示成功，否则返回错误信息
     */
    public static String validateRegisterParams(String username, String nickname, 
                                                String password, String passwordRepeat) {
        // 校验用户名
        String usernameError = validateUsername(username);
        if (usernameError != null) {
            return usernameError;
        }

        // 校验昵称
        String nicknameError = validateNickname(nickname);
        if (nicknameError != null) {
            return nicknameError;
        }

        // 校验密码
        String passwordError = validatePassword(password);
        if (passwordError != null) {
            return passwordError;
        }

        // 校验两次密码是否一致
        if (!password.equals(passwordRepeat)) {
            return "两次输入的密码不一致";
        }

        return null;
    }

    /**
     * 检查字符串是否为空字符串（只包含空格）
     * 
     * @param value 要检查的字符串
     * @return true 表示为空或只包含空格
     */
    public static boolean isBlank(String value) {
        if (value == null) {
            return true;
        }
        // 检查每个字符是否都是空白字符
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否包含非法字符（用于额外的安全检查）
     * 
     * @param value 要检查的字符串
     * @return true 表示包含非法字符
     */
    public static boolean containsIllegalCharacters(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        
        // 检查是否包含 SQL 注入常见字符
        String[] illegalPatterns = {"'", "\"", ";", "--", "/*", "*/", "xp_", "exec", "drop", "delete", "update", "insert"};
        String lowerValue = value.toLowerCase();
        
        for (String pattern : illegalPatterns) {
            if (lowerValue.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
}
