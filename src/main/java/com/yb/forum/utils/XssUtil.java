package com.yb.forum.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

/**
 * XSS过滤工具类
 * 用于防止跨站脚本攻击
 */
public class XssUtil {
    
    // 定义允许的HTML标签和属性
    private static final Whitelist whitelist = Whitelist.basicWithImages()
            .addAttributes("a", "href", "title")
            .addAttributes("img", "src", "alt", "title")
            .addAttributes("code", "class")
            .addAttributes("pre", "class");
    
    /**
     * 清理输入字符串，移除可能的XSS攻击代码
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String clean(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // 使用Jsoup进行XSS过滤
        Document doc = Jsoup.parse(input);
        String clean = Jsoup.clean(input, whitelist);
        
        return clean;
    }
    
    /**
     * 清理输入字符串，移除所有HTML标签
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String cleanAll(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // 移除所有HTML标签
        return Jsoup.clean(input, Whitelist.none());
    }
}