package com.yb.forum.dao;

import com.yb.forum.model.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

//@Mapper
public interface UserMapper {
    int insert(User row);

    int insertSelective(User row);

    User selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(User row);

    int updateByPrimaryKey(User row);

    User selectByUserName (@Param("username") String username);
    
    User selectByNickname (@Param("nickname") String nickname);

    int selectTotalCount();

    @MapKey("dateStr")  // 这里的 "dateStr" 必须和你 XML 中 SELECT 出来的别名完全一致
    List<Map<String, Object>> selectUserGrowthLast7Days();

    List<User> selectAllWithFilter(@Param("username") String username);
}
