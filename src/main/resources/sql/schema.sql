-- =============================================================================
-- 数据库结构初始化 (DDL)
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `user_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `student_id` VARCHAR(50) COMMENT '学号',
    `username` VARCHAR(100) COMMENT '用户姓名',
    `nickname` VARCHAR(100) COMMENT '用户昵称',
    `openid` VARCHAR(100) COMMENT '微信OpenID',
    `unionid` VARCHAR(100) COMMENT '微信UnionID',
    `wechat_nickname` VARCHAR(100) COMMENT '微信昵称',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `password` VARCHAR(255) COMMENT '密码',
    `avatar_file_id` BIGINT COMMENT '头像文件ID',
    `gender` INT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `birthday` DATE COMMENT '出生日期',
    `major` VARCHAR(100) COMMENT '专业',
    `grade` VARCHAR(50) COMMENT '年级',
    `tech_stack` TEXT COMMENT '技术栈',
    `personal_intro` TEXT COMMENT '个人简介',
    `award_experience` TEXT COMMENT '获奖经历',
    `role` VARCHAR(20) DEFAULT 'student' COMMENT '角色',
    `auth_status` INT DEFAULT 0 COMMENT '认证状态：0-待审核 1-已通过 2-已驳回',
    `audit_time` DATETIME COMMENT '审核时间',
    `auditor_user_id` INT COMMENT '审核人ID',
    `remark` TEXT COMMENT '审核备注',
    `is_talent_visible` BOOLEAN DEFAULT TRUE COMMENT '人才卡片是否可见',
    `talent_card_id` INT COMMENT '当前人才卡片ID',
    `status` BOOLEAN DEFAULT FALSE COMMENT '是否冻结',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `login_count` INT DEFAULT 0 COMMENT '登录次数',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 社区帖子表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `community_post` (
    `post_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
    `user_id` INT NOT NULL COMMENT '发布者ID',
    `section` INT NOT NULL COMMENT '板块：1-技术交流 2-灵感分享 3-组队经验',
    `title` VARCHAR(200) NOT NULL COMMENT '帖子标题',
    `content` TEXT NOT NULL COMMENT '帖子内容',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `is_top` BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    `is_essence` BOOLEAN DEFAULT FALSE COMMENT '是否精华',
    `status` INT DEFAULT 1 COMMENT '状态：1-正常 0-删除 2-违规下架',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_section` (`section`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_time` (`created_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

SET FOREIGN_KEY_CHECKS = 1;