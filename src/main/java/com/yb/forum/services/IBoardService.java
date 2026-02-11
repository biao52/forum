package com.yb.forum.services;

import com.yb.forum.model.Board;

import java.util.List;

public interface IBoardService {

    /**
     * 查询num条记录
     * @param num 要查询的条数
     * @return
     */
    List<Board> selectByNum (Integer num);

    /**
     * 根据版块Id查询版块信息
     * @param id 版块Id
     * @return
     */
    Board selectById (Long id);

    /**
     * 版块中的帖子数量 +1
     * @param id 版块Id
     */
    void addOneArticleCountById (Long id);

    /**
     * 版块中的帖子数量 -1
     * @param id 版块Id
     */
    void subOneArticleCountById (Long id);
}
