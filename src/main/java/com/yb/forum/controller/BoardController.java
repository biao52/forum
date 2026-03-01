package com.yb.forum.controller;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.exception.ApplicationException;
import com.yb.forum.model.Board;
import com.yb.forum.services.IBoardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author yangbiao
 */
@Slf4j
@Api(tags = "版块接口")
@RestController
@RequestMapping("/board")
public class BoardController {

    @Value("${bit-forum.index.board-num:9}")
    private Integer indexBoardNum;

    @Resource
    private IBoardService boardService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String BOARD_TOP_LIST_KEY = "board:topList:";
    private static final String BOARD_DETAIL_KEY = "board:detail:";
    private static final long CACHE_EXPIRE_TIME = 60; // 版块数据缓存时间较长（分钟）

    @ApiOperation("获取首页版块列表")
    @GetMapping("/topList")
    public AppResult<List<Board>> topList () {
        log.info("首页版块个数为：" + indexBoardNum);

        // 构造缓存key
        String cacheKey = BOARD_TOP_LIST_KEY + indexBoardNum;

        // 尝试从Redis获取缓存
        List<Board> boards = (List<Board>) redisTemplate.opsForValue().get(cacheKey);

        if (boards != null) {
            log.info("从Redis缓存获取首页版块列表");
            return AppResult.success(boards);
        }

        // 缓存未命中，从数据库查询
        log.info("从数据库查询首页版块列表");
        boards = boardService.selectByNum(indexBoardNum);

        if (boards == null) {
            boards = new ArrayList<>();
        }

        // 存入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, boards, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return AppResult.success(boards);
    }

    @ApiOperation("获取版块信息")
    @GetMapping("/getById")
    public AppResult<Board> getById (@ApiParam("版块Id") @RequestParam("id") @NonNull Long id) {

        // 构造缓存key
        String cacheKey = BOARD_DETAIL_KEY + id;

        // 尝试从Redis获取缓存
        Board board = (Board) redisTemplate.opsForValue().get(cacheKey);

        if (board != null) {
            log.info("从Redis缓存获取版块信息，id: {}", id);
            if (board.getDeleteState() == 1) {
                log.warn(ResultCode.FAILED_BOARD_NOT_EXISTS.toString());
                throw new ApplicationException(AppResult.failed(ResultCode.FAILED_BOARD_NOT_EXISTS));
            }
            return AppResult.success(board);
        }

        // 缓存未命中，从数据库查询
        log.info("从数据库查询版块信息，id: {}", id);
        board = boardService.selectById(id);

        // 对查询结果进行校验
        if (board == null || board.getDeleteState() == 1) {
            log.warn(ResultCode.FAILED_BOARD_NOT_EXISTS.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_BOARD_NOT_EXISTS));
        }

        // 存入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, board, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return AppResult.success(board);
    }
}