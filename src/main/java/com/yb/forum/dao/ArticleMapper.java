package com.yb.forum.dao;

import com.yb.forum.model.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//@Mapper
public interface ArticleMapper {
    int insert(Article row);

    int insertSelective(Article row);

    Article selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Article row);

    int updateByPrimaryKeyWithBLOBs(Article row);

    int updateByPrimaryKey(Article row);

    /**
     * 查询所有帖子列表
     * @return
     */
    List<Article> selectAll ();

    /**
     * 根据版块Id查询所有帖子列表
     * @param boardId 版块Id
     * @return
     */
    List<Article> selectAllByBoardId (@Param("boardId") Long boardId, @Param("keyword") String keyword);

    /**
     * 根据帖子Id查询详情
     * @param id 帖子Id
     * @return 帖子详情
     */
    Article selectDetailById (@Param("id") Long id);

    /**
     * 根据用户Id查询帖子列表
     * @param userId 用户Id
     * @return 帖子列表
     */
    List<Article> selectByUserId (@Param("userId") Long userId);

    /**
     * 🔍 根据关键字搜索文章（标题或内容）
     * @param keyword 搜索关键字
     * @return 文章列表
     */
    List<Article> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 🔍 根据版块Id + 关键字搜索文章
     * @param boardId 版块Id
     * @param keyword 搜索关键字
     * @return 文章列表
     */
    List<Article> selectByBoardIdAndKeyword(@Param("boardId") Long boardId,
                                            @Param("keyword") String keyword);

    int selectTotalCount();
    int selectTodayCount();
    List<Article> selectAllByBoardIdWithDetail(
            @Param("boardId") Long boardId,
            @Param("keyword") String keyword
    );

}