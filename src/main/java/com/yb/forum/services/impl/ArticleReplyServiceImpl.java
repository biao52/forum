package com.yb.forum.services.impl;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.dao.ArticleReplyMapper;
import com.yb.forum.exception.ApplicationException;
import com.yb.forum.model.ArticleReply;
import com.yb.forum.services.IArticleReplyService;
import com.yb.forum.services.IArticleService;
import com.yb.forum.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author yangbiao
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ArticleReplyServiceImpl implements IArticleReplyService {

    @Resource
    private ArticleReplyMapper articleReplyMapper;

    @Resource
    private IArticleService articleService;

    @Override
    public void create(ArticleReply articleReply) {
        // 非空校验
        if (articleReply == null || articleReply.getArticleId() == null
                || articleReply.getPostUserId() == null
                || StringUtil.isEmpty(articleReply.getContent())) {
            // 打印日志
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        // 设置默认值
        articleReply.setReplyId(null);
        articleReply.setReplyUserId(null);
        articleReply.setLikeCount(0);
        articleReply.setState((byte) 0);
        articleReply.setDeleteState((byte) 0);
        Date date = new Date();
        articleReply.setCreateTime(date);
        articleReply.setUpdateTime(date);
        // 写入数据库
        int row = articleReplyMapper.insertSelective(articleReply);
        if (row != 1) {
            // 打印日志
            log.warn(ResultCode.ERROR_SERVICES.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.ERROR_SERVICES));
        }

        // 更新帖子表中的回复数
        articleService.addOneReplyCountById(articleReply.getArticleId());
        // 打印日志
        log.info("回复成功, article id = " + articleReply.getArticleId() + ", user id = " +
                articleReply.getPostUserId());

    }

    @Override
    public List<ArticleReply> selectByArticleId(Long articleId) {
        // 非空校验
        if (articleId == null || articleId <= 0) {
            // 打印日志
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        // 调用DAO
        List<ArticleReply> result = articleReplyMapper.selectByArticleId(articleId);
        // 返回结果
        return result;
    }
    
    @Override
    public void deleteById(Long id) {
        // 非空校验
        if (id == null || id <= 0) {
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        
        // 查询回复信息
        ArticleReply reply = articleReplyMapper.selectByPrimaryKey(id);
        if (reply == null) {
            log.warn(ResultCode.FAILED_REPLY_NOT_EXISTS.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_REPLY_NOT_EXISTS));
        }
        
        // 逻辑删除
        reply.setDeleteState((byte) 1);
        reply.setUpdateTime(new Date());
        int row = articleReplyMapper.updateByPrimaryKeySelective(reply);
        if (row != 1) {
            log.warn(ResultCode.ERROR_SERVICES.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.ERROR_SERVICES));
        }
        
        // 更新帖子表中的回复数（减1）
        articleService.reduceOneReplyCountById(reply.getArticleId());
        log.info("删除回复成功, reply id = " + id + ", article id = " + reply.getArticleId());
    }
    
    @Override
    public ArticleReply selectById(Long id) {
        // 非空校验
        if (id == null || id <= 0) {
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        return articleReplyMapper.selectByPrimaryKey(id);
    }
}
