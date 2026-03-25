-- 论坛项目完整数据库脚本
-- 创建数据库
drop database if exists forum_db;
create database  forum_db character set utf8mb4 collate utf8mb4_general_ci;
-- 选择数据库
use forum_db;

-- 创建表
-- 用户表
drop table if exists t_user;
create table t_user (
                        id bigint primary key auto_increment comment '编号，主键自增',
                        username varchar(20) not null unique comment '用户名，唯一',
                        password varchar(32) not null comment '加密后的密码',
                        nickname varchar(50) not null comment '昵称',
                        phoneNum varchar(20) comment '手机号',
                        email varchar(50) comment '电子邮箱',
                        gender tinyint not null default 2 comment '性别 0女，1男，2保密',
                        salt varchar(32) not null comment '为密码加盐',
                        avatarUrl varchar(255) comment '用户头像路径',
                        articleCount int not null default 0 comment '发帖数量',
                        isAdmin tinyint not null default 0 comment '是否管理员 0否，1是',
                        remark varchar(1000) comment '备注，自我介绍',
                        state tinyint not null default 0 comment '状态 0正常，1禁言',
                        deleteState tinyint not null default 0 comment '是否删除，0否，1是',
                        createTime datetime not null comment '创建时间，精确到秒',
                        updateTime datetime not null comment '更新时间，精确到秒'
);

-- 版块表
drop table if exists t_board;
create table t_board (
                         id bigint primary key auto_increment comment '编号，主键自增',
                         name varchar(50) not null comment '版块名',
                         articleCount int not null default 0 comment '帖子数量',
                         sort int not null default 0 comment '排序优先级，升序',
                         state tinyint not null default 0 comment '状态 0正常，1禁用',
                         deleteState tinyint not null default 0 comment '是否删除，0否，1是',
                         createTime datetime not null comment '创建时间，精确到秒',
                         updateTime datetime not null comment '更新时间，精确到秒'
);

-- 帖子表
drop table if exists t_article;
create table t_article (
                           id bigint primary key auto_increment comment '编号，主键自增',
                           boardId bigint not null comment '编号，主键自增',
                           userId bigint not null comment '发帖人，关联用户编号',
                           title varchar(100) not null comment '帖子标题',
                           content text not null comment '帖子正文',
                           visitCount int not null default 0 comment '访问量',
                           replyCount int not null default 0 comment '回复数',
                           likeCount int not null default 0 comment '点赞数',
                           state tinyint not null default 0 comment '状态 0正常，1禁用',
                           deleteState tinyint not null default 0 comment '是否删除，0否，1是',
                           createTime datetime not null comment '创建时间，精确到秒',
                           updateTime datetime not null comment '更新时间，精确到秒'
);

-- 帖子回复表
drop table if exists t_article_reply;
create table t_article_reply (
                                 id bigint primary key auto_increment comment '编号，主键自增',
                                 articleId bigint not null comment '关联帖子编号',
                                 postUserId bigint not null comment '楼主用户，关联用户编号',
                                 replyId bigint comment '关联回复编号，支持楼中楼',
                                 replyUserId bigint comment '楼主下的回复用户编号，支持楼中楼',
                                 content varchar(500) not null comment '回贴内容',
                                 likeCount int not null comment '回贴内容',
                                 state tinyint not null default 0 comment '状态 0正常，1禁用',
                                 deleteState tinyint not null default 0 comment '是否删除，0否，1是',
                                 createTime datetime not null comment '创建时间，精确到秒',
                                 updateTime datetime not null comment '更新时间，精确到秒'
);

-- 站内信表
drop table if exists t_message;
create table t_message (
                           id bigint primary key auto_increment comment '编号，主键自增',
                           postUserId bigint not null comment '发送者，关联用户编号',
                           receiveUserId bigint not null comment '接收者，关联用户编号',
                           content varchar(255) not null comment '内容',
                           state tinyint not null default 0 comment '状态 0 正常，1 禁用',
                           deleteState tinyint not null default 0 comment '是否删除，0 否，1 是',
                           createTime datetime not null comment '创建时间，精确到秒',
                           updateTime datetime not null comment '更新时间，精确到秒'
);

-- Dify 对话记录表
DROP TABLE IF EXISTS t_dify_conversation;
CREATE TABLE t_dify_conversation (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '编号，主键自增',
                                     userId BIGINT NOT NULL COMMENT '用户编号，关联用户表',
                                     conversationId VARCHAR(100) COMMENT 'Dify 对话 ID，用于关联同一会话',
                                     query TEXT NOT NULL COMMENT '用户问题',
                                     answer TEXT COMMENT 'AI 回答',
                                     messageId VARCHAR(100) COMMENT 'Dify 消息 ID',
                                     createTime DATETIME NOT NULL COMMENT '创建时间，精确到秒',
                                     updateTime DATETIME NOT NULL COMMENT '更新时间，精确到秒',
                                     INDEX idx_user_id (userId),
                                     INDEX idx_conversation_id (conversationId)
);

-- 写入版块信息数据
INSERT INTO `t_board` (`id`, `name`, `articleCount`, `sort`, `state`, `deleteState`, `createTime`, `updateTime`) VALUES
    (1, 'Java', 0, 1, 0, 0, '2023-06-25 10:25:55', '2023-06-25 10:25:55');
INSERT INTO `t_board` (`id`, `name`, `articleCount`, `sort`, `state`, `deleteState`, `createTime`, `updateTime`) VALUES
    (2, 'C++', 0, 2, 0, 0, '2023-06-25 10:25:56', '2023-06-25 10:25:56');
INSERT INTO `t_board` (`id`, `name`, `articleCount`, `sort`, `state`, `deleteState`, `createTime`, `updateTime`) VALUES
    (3, '前端技术', 0, 3, 0, 0, '2023-06-25 10:25:56', '2023-06-25 10:25:56');
INSERT INTO `t_board` (`id`, `name`, `articleCount`, `sort`, `state`, `deleteState`, `createTime`, `updateTime`) VALUES
    (4, 'MySQL', 0, 4, 0, 0, '2023-06-25 19:05:22', '2023-06-25 19:05:22');
INSERT INTO `t_board` (`id`, `name`, `articleCount`, `sort`, `state`, `deleteState`, `createTime`, `updateTime`) VALUES (5, '面试宝典', 0, 5, 0, 0, '2024-06-25 19:05:22', '2024-06-25 19:05:22');
INSERT INTO `t_board` (`id`, `name`, `articleCount`, `sort`, `state`, `deleteState`, `createTime`, `updateTime`) VALUES (6, '经验分享', 0, 6, 0, 0, '2024-06-25 19:05:22', '2024-06-25 19:05:22');
INSERT INTO `t_board` (`id`, `name`, `articleCount`, `sort`, `state`, `deleteState`, `createTime`, `updateTime`) VALUES
    (7, '招聘信息', 0, 7, 0, 0, '2023-06-25 19:05:22', '2023-06-25 19:05:22');
INSERT INTO `t_board` (`id`, `name`, `articleCount`, `sort`, `state`, `deleteState`, `createTime`, `updateTime`) VALUES (8, '福利待遇', 0, 8, 0, 0, '2024-06-25 19:05:22', '2024-06-25 19:05:22');
INSERT INTO `t_board` (`id`, `name`, `articleCount`, `sort`, `state`, `deleteState`, `createTime`, `updateTime`) VALUES (9, '灌水区', 0, 9, 0, 0, '2024-06-25 19:05:22', '2024-06-25 19:05:22');

-- 修改回复表中的非空校验
ALTER TABLE `forum_db`.`t_article_reply`
    CHANGE COLUMN `replyId` `replyId` BIGINT(20) NULL COMMENT '关联回复编号，支持楼中楼' ,
    CHANGE COLUMN `replyUserId` `replyUserId` BIGINT(20) NULL COMMENT '楼主下的回复用户编号，支持楼中楼' ;

-- ----------------------------
-- Records of t_article
-- ----------------------------
INSERT INTO `t_article` VALUES (1, 4, 1, 'MySQL板块测试', '范德萨范德萨分爱上幅度萨芬 士大夫的撒范德萨', 0, 0, 3, 0, 0, '2025-10-22 21:43:21', '2026-03-03 21:25:36');
INSERT INTO `t_article` VALUES (2, 2, 1, 'C++辅导', '范德萨范德萨范德萨范德萨都是范德萨范德萨', 0, 0, 9, 0, 0, '2025-10-22 21:43:54', '2026-03-03 21:25:36');
INSERT INTO `t_article` VALUES (7, 1, 1, '测试噢噢噢噢', '测试噢噢噢噢', 0, 0, 2, 0, 0, '2025-11-29 00:44:54', '2026-03-03 21:17:13');
INSERT INTO `t_article` VALUES (8, 2, 5, '前后短测试', '前后短测试', 0, 0, 0, 0, 0, '2025-11-29 01:50:11', '2025-11-29 01:50:11');
INSERT INTO `t_article` VALUES (9, 1, 6, '这是一个mysql的版块内容', '的犯得上发生范德萨\n测试页面发帖子', 0, 0, 0, 0, 0, '2025-11-29 14:47:16', '2025-11-29 14:47:16');
INSERT INTO `t_article` VALUES (10, 1, 6, '这是一个mysql的版块内容', '阿凡达范德萨', 1, 0, 0, 0, 0, '2025-11-29 14:48:41', '2025-11-29 14:48:41');
INSERT INTO `t_article` VALUES (11, 1, 6, 'fsasdfdsafasd', '范德萨发大水范德萨', 0, 0, 4, 0, 0, '2025-11-29 14:49:26', '2026-03-03 21:24:53');


-- ----------------------------
-- Records of t_dify_conversation
-- ----------------------------
INSERT INTO `t_dify_conversation` VALUES (1, 5, NULL, '你是谁啊', '我是论坛的智能助手，专门为论坛用户提供帮助。我的职责包括回答使用问题、提供技术支持、解答疑问，同时保持友好、专业和耐心的态度。如果您有任何问题，欢迎随时向我咨询！', NULL, '2026-03-10 22:15:30', '2026-03-10 22:15:30');
INSERT INTO `t_dify_conversation` VALUES (2, 5, NULL, '你能做什么??', '\n你好！我能在以下方面帮助你：1. 解答论坛使用相关问题 2. 提供技术支持与操作指导 3. 解答各类疑问。若遇到无法处理的问题，我会礼貌告知。请随时告诉我你需要什么帮助！', NULL, '2026-03-10 22:20:20', '2026-03-10 22:20:20');
INSERT INTO `t_dify_conversation` VALUES (3, 5, NULL, '你是谁啊', '\n您好！我是论坛的智能助手，专门为论坛用户提供帮助。我的职责包括回答使用问题、提供技术支持、解答疑问等。如果您有任何问题或需要帮助，欢迎随时向我提问！', NULL, '2026-03-10 22:31:16', '2026-03-10 22:31:16');
INSERT INTO `t_dify_conversation` VALUES (4, 5, NULL, '你是谁啊', '你好呀！我是论坛的智能助手，专门为各位用户服务～我的职责包括：1. 回答论坛使用相关问题 2. 提供技术支持指导 3. 解答各种疑问 4. 保持友好专业的态度~ 如果遇到任何问题都可以问我，我会尽力帮助你！', NULL, '2026-03-10 22:31:26', '2026-03-10 22:31:26');
INSERT INTO `t_dify_conversation` VALUES (5, 5, NULL, '你是谁啊', '我是论坛的智能助手，专门为您提供帮助！我的职责包括解答使用问题、提供技术支持、处理疑问等。有什么需要协助的吗？', NULL, '2026-03-10 22:56:44', '2026-03-10 22:56:44');

-- ----------------------------
-- Records of t_message
-- ----------------------------
INSERT INTO `t_message` VALUES (1, 1, 2, '飞机迪斯科飞机是打卡练腹肌打算看了', 0, 0, '2026-03-03 19:40:18', '2026-03-03 19:40:18');
INSERT INTO `t_message` VALUES (2, 1, 2, '飞机迪斯科飞机是打卡练腹肌打算看了', 0, 0, '2026-03-03 19:40:26', '2026-03-03 19:40:26');
INSERT INTO `t_message` VALUES (3, 1, 2, '飞机迪斯科飞机是打卡练腹肌打算看了', 0, 0, '2026-03-03 19:41:13', '2026-03-03 19:41:13');
INSERT INTO `t_message` VALUES (4, 1, 2, '你好，最近怎么样？', 0, 0, '2026-03-03 20:43:29', '2026-03-03 20:43:29');
INSERT INTO `t_message` VALUES (5, 1, 2, '测试内容', 0, 0, '2026-03-03 20:43:29', '2026-03-03 20:43:29');
INSERT INTO `t_message` VALUES (6, 1, 2, '测试', 0, 0, '2026-03-03 20:43:30', '2026-03-03 20:43:30');
INSERT INTO `t_message` VALUES (7, 1, 2, 'hello\'; DROP TABLE messages; --', 0, 0, '2026-03-03 20:43:31', '2026-03-03 20:43:31');
INSERT INTO `t_message` VALUES (8, 1, 2, '<script>alert(\'xss\')</script>', 0, 0, '2026-03-03 20:43:31', '2026-03-03 20:43:31');
INSERT INTO `t_message` VALUES (9, 1, 2, '测试', 0, 0, '2026-03-03 20:43:31', '2026-03-03 20:43:31');
INSERT INTO `t_message` VALUES (10, 1, 2, '测试', 0, 0, '2026-03-03 20:43:31', '2026-03-03 20:43:31');
INSERT INTO `t_message` VALUES (11, 1, 2, '你好，最近怎么样？', 0, 0, '2026-03-03 20:44:26', '2026-03-03 20:44:26');
INSERT INTO `t_message` VALUES (12, 1, 3, '你是谁呀哈哈哈', 0, 0, '2026-03-03 20:44:26', '2026-03-03 20:44:26');
INSERT INTO `t_message` VALUES (13, 1, 2, '测试内容', 0, 0, '2026-03-03 20:44:27', '2026-03-03 20:44:27');
INSERT INTO `t_message` VALUES (14, 1, 2, '<script>alert(\'xss\')</script>', 0, 0, '2026-03-03 20:44:27', '2026-03-03 20:44:27');
INSERT INTO `t_message` VALUES (15, 1, 2, 'hello\'; DROP TABLE messages; --', 0, 0, '2026-03-03 20:44:27', '2026-03-03 20:44:27');
INSERT INTO `t_message` VALUES (16, 1, 2, '测试', 0, 0, '2026-03-03 20:44:28', '2026-03-03 20:44:28');
INSERT INTO `t_message` VALUES (17, 1, 2, '测试', 0, 0, '2026-03-03 20:44:28', '2026-03-03 20:44:28');
INSERT INTO `t_message` VALUES (18, 1, 2, 'hello\'; DROP TABLE messages; --', 0, 0, '2026-03-03 20:46:51', '2026-03-03 20:46:51');
INSERT INTO `t_message` VALUES (19, 1, 2, '你好，最近怎么样？', 0, 0, '2026-03-03 20:47:49', '2026-03-03 20:47:49');
INSERT INTO `t_message` VALUES (20, 1, 3, '你是谁呀哈哈哈', 0, 0, '2026-03-03 20:47:50', '2026-03-03 20:47:50');
INSERT INTO `t_message` VALUES (21, 1, 2, '<script>alert(\'xss\')</script>', 0, 0, '2026-03-03 20:47:50', '2026-03-03 20:47:50');
INSERT INTO `t_message` VALUES (22, 1, 2, 'hello\'; DROP TABLE messages; --', 0, 0, '2026-03-03 20:47:50', '2026-03-03 20:47:50');
INSERT INTO `t_message` VALUES (23, 5, 2, '你好，最近怎么样？', 0, 0, '2026-03-03 21:13:33', '2026-03-03 21:13:33');


-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'Jack', '8b3c7521fe916e7c053c474f74074d1c', 'Jack', NULL, NULL, 2, 'e88a0c42c6b34b658a3fd7b84b637763', NULL, 3, 0, NULL, 0, 0, '2025-10-22 21:42:26', '2025-10-22 21:42:26');
INSERT INTO `t_user` VALUES (2, 'bitboy', '123456', '特', NULL, NULL, 2, '123', 'avatar.png', 0, 1, NULL, 0, 0, '2022-12-13 22:30:10', '2022-12-13 22:30:13');
INSERT INTO `t_user` VALUES (3, 'yangbiao', '0731bc467c6347c32de1780c24bbb691', 'darkbee', NULL, NULL, 2, '3cd1b1e9a7d84e43acc40a620e2490ad', NULL, 0, 0, NULL, 0, 0, '2025-11-18 21:54:50', '2025-11-18 21:54:50');
INSERT INTO `t_user` VALUES (4, 'hahaah', 'e85f3035c02ba7bdad315d02c1a8eca3', 'fdsjafdsajklfs', NULL, NULL, 2, 'ffaabfb559cf44ccabbd52d097228dfb', NULL, 0, 0, NULL, 0, 0, '2025-11-18 22:36:52', '2025-11-18 22:36:52');
INSERT INTO `t_user` VALUES (5, '1', '22924fa6926f14db68fbd2475432601e', '1', NULL, NULL, 2, '6b6adb56bf3c4568b56f85902f09166c', NULL, 1, 0, NULL, 0, 0, '2025-11-23 14:31:37', '2025-11-23 14:31:37');
INSERT INTO `t_user` VALUES (6, '2', 'd51f71b5840d0a29d817545e7b0043a2', '2', NULL, NULL, 2, '3bb881b386d94b96aa968112768e5409', NULL, 3, 0, NULL, 0, 0, '2025-11-28 23:33:10', '2025-11-28 23:33:10');
