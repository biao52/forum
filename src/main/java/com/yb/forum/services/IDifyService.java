package com.yb.forum.services;

import com.yb.forum.model.DifyResponse;

public interface IDifyService {
    /**
     * 调用Dify智能体API
     * @param query 用户查询内容
     * @param userId 用户ID
     * @return Dify响应结果
     */
    DifyResponse callDify(String query, String userId);
}
