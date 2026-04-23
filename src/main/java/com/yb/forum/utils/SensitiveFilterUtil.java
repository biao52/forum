package com.yb.forum.utils;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 敏感词过滤工具类
 */
@Component
public class SensitiveFilterUtil {
    
    // 敏感词集合
    private Set<String> sensitiveWords = new HashSet<>();
    
    // 初始化敏感词列表
    @PostConstruct
    public void init() {
        // 添加常见敏感词
        sensitiveWords.add("政治敏感词1");
        sensitiveWords.add("政治敏感词2");
        sensitiveWords.add("色情敏感词1");
        sensitiveWords.add("色情敏感词2");
        sensitiveWords.add("暴力敏感词1");
        sensitiveWords.add("暴力敏感词2");
        sensitiveWords.add("赌博敏感词");
        sensitiveWords.add("毒品敏感词");
        sensitiveWords.add("违禁品敏感词");
        sensitiveWords.add("侮辱性敏感词1");
        sensitiveWords.add("侮辱性敏感词2");
    }
    
    /**
     * 检查内容是否包含敏感词
     * @param content 待检查内容
     * @return 是否包含敏感词
     */
    public boolean containsSensitiveWord(String content) {
        if (StringUtil.isEmpty(content)) {
            return false;
        }
        
        // 检查敏感词
        for (String sensitiveWord : sensitiveWords) {
            if (content.contains(sensitiveWord)) {
                return true;
            }
        }
        
        // 检查特殊字符和恶意内容
        String regex = "[\\s\\S]*([<>\"'&\\[\\]\\(\\)\\{\\}])[\\s\\S]*";
        if (Pattern.matches(regex, content)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 过滤敏感词（将敏感词替换为*）
     * @param content 待过滤内容
     * @return 过滤后的内容
     */
    public String filterSensitiveWord(String content) {
        if (StringUtil.isEmpty(content)) {
            return content;
        }
        
        String filteredContent = content;
        for (String sensitiveWord : sensitiveWords) {
            if (filteredContent.contains(sensitiveWord)) {
                // 手动构建替换字符串
                StringBuilder replacement = new StringBuilder();
                for (int i = 0; i < sensitiveWord.length(); i++) {
                    replacement.append("*");
                }
                filteredContent = filteredContent.replace(sensitiveWord, replacement.toString());
            }
        }
        
        return filteredContent;
    }
}