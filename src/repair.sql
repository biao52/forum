-- 修改回复表中的非空校验
ALTER TABLE `forum_db`.`t_article_reply`
CHANGE COLUMN `replyId` `replyId` BIGINT(20) NULL COMMENT '关联回复编号，支持楼中楼' ,
CHANGE COLUMN `replyUserId` `replyUserId` BIGINT(20) NULL COMMENT '楼主下的回复用户编号，支持楼中楼' ;