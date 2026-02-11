package com.yb.forum.services.impl;

import com.yb.forum.model.Article;
import com.yb.forum.services.IArticleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class ArticleServiceImplTest {

    @Resource
    private IArticleService articleService;
    @Resource
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    void create() {
        Article article = new Article();
        article.setUserId(2L); // bitboy
        article.setBoardId(1L); // java版块
        article.setTitle("单元测试");
        article.setContent("测试内容");
        articleService.create(article);
        System.out.println("发贴成功");
    }

    @Test
    void selectAll() throws JsonProcessingException {
        // 调用Service
        List<Article> articles = articleService.selectAll();
        // 转换成JSON字符串并且打印
        System.out.println(objectMapper.writeValueAsString(articles));
    }

    @Test
    void selectAllByBoardId() throws JsonProcessingException {
        List<Article> articles = articleService.selectAllByBoardId(3l);
        System.out.println(objectMapper.writeValueAsString(articles));
    }

    @Test
    void selectDetailById() throws JsonProcessingException {
        Article article = articleService.selectDetailById(1l);
        System.out.println(objectMapper.writeValueAsString(article));
    }

    @Test
    @Transactional
    void modify() {
        articleService.modify(1l, "单元测试111", "测试内容111");
        System.out.println("更新成功");
    }

    @Test
    void selectById() throws JsonProcessingException {
        Article article = articleService.selectById(1l);
        System.out.println(objectMapper.writeValueAsString(article));
    }

    @Test
    @Transactional
    void thumbsUpById() {
        articleService.thumbsUpById(1L);
        System.out.println("点赞成功");
    }

    @Test
    @Transactional
    void deleteById() {
        articleService.deleteById(11l);
        System.out.println("删除成功");
    }

    @Test
    @Transactional
    void addOneReplyCountById() {
        articleService.addOneReplyCountById(1l);
        System.out.println("更新成功");
    }

    @Test
    void selectByUserId() throws JsonProcessingException {
        List<Article> articles = articleService.selectByUserId(1l);
        System.out.println(objectMapper.writeValueAsString(articles));
    }
}