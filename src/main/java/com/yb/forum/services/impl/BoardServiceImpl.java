package com.yb.forum.services.impl;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.dao.BoardMapper;
import com.yb.forum.exception.ApplicationException;
import com.yb.forum.model.Board;
import com.yb.forum.services.IBoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author yangbiao
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BoardServiceImpl implements IBoardService {

    @Resource
    private BoardMapper boardMapper;

    @Override
    public List<Board> selectByNum(Integer num) {
        // 非空校验
        if (num <= 0) {
            // 打印日志
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        // 调用DAO查询数据库中的数据
        List<Board> result = boardMapper.selectByNum(num);
        // 返回结果
        return result;
    }

    @Override
    public Board selectById(Long id) {
        if (id == null || id <= 0) {
            // 打印日志
            log.warn(ResultCode.FAILED_BOARD_ARTICLE_COUNT.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_BOARD_ARTICLE_COUNT));
        }
        // 调用DAO查询数据
        Board board = boardMapper.selectByPrimaryKey(id);
        // 返回结果
        return board;
    }

    @Override
    public void addOneArticleCountById(Long id) {
        if (id == null || id <= 0) {
            // 打印日志
            log.warn(ResultCode.FAILED_BOARD_ARTICLE_COUNT.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_BOARD_ARTICLE_COUNT));
        }
        // 查询对应的版块
        Board board = boardMapper.selectByPrimaryKey(id);
        if (board == null) {
            // 打印日志
            log.warn(ResultCode.ERROR_IS_NULL.toString() + ", board id = " + id);
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.ERROR_IS_NULL));
        }
        // 更新帖子数量
        Board updateBoard = new Board();
        updateBoard.setId(board.getId());
        updateBoard.setArticleCount(board.getArticleCount() + 1);
        // 调用DAO，执行更新
        int row = boardMapper.updateByPrimaryKeySelective(updateBoard);
        // 判断受影响的行数
        if (row != 1) {
            log.warn(ResultCode.FAILED.toString() + ", 受影响的行数不等于 1 .");
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED));
        }
    }

    @Override
    public void subOneArticleCountById(Long id) {
        // 非空校验
        if (id == null || id <= 0) {
            // 打印日志
            log.warn(ResultCode.FAILED_BOARD_ARTICLE_COUNT.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_BOARD_ARTICLE_COUNT));
        }
        // 查询版块详情
        Board board = boardMapper.selectByPrimaryKey(id);
        if (board == null) {
            // 打印日志
            log.warn(ResultCode.FAILED_BOARD_NOT_EXISTS.toString() + ", board id = " + id);
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_BOARD_NOT_EXISTS));
        }
        // 构造更新对象
        Board updateBoard = new Board();
        updateBoard.setId(board.getId());
        updateBoard.setArticleCount(board.getArticleCount() - 1);
        // 判断减1之后是否小于0
        if (updateBoard.getArticleCount() < 0) {
            // 如果小于0那么设置为0
            updateBoard.setArticleCount(0);
        }

        // 调用DAO
        int row = boardMapper.updateByPrimaryKeySelective(updateBoard);
        if (row != 1) {
            log.warn(ResultCode.FAILED.toString() + ", 受影响的行数不等于 1 .");
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED));
        }
    }
}
