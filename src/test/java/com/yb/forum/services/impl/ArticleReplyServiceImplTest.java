package com.yb.forum.services.impl;

import com.yb.forum.model.ArticleReply;
import com.yb.forum.services.IArticleReplyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class ArticleReplyServiceImplTest {

    @Resource
    private IArticleReplyService articleReplyService;
    @Resource
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    void create() {
        // 构造一个回复对象
        ArticleReply articleReply = new ArticleReply();
        articleReply.setArticleId(1l);
        articleReply.setPostUserId(1l);
        articleReply.setContent("单元测试回复");
        // 调用Service
        articleReplyService.create(articleReply);
        System.out.println("回复成功");
    }

    @Test
    void selectByArticleId() throws JsonProcessingException {
        List<ArticleReply> articleReplies = articleReplyService.selectByArticleId(1L);
        System.out.println(objectMapper.writeValueAsString(articleReplies));

    }
}