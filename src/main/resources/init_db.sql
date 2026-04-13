DROP DATABASE IF EXISTS team_matching;
CREATE DATABASE IF NOT EXISTS team_matching
CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE team_matching;


-- 1.1 技能标签表（不依赖其他表）
DROP TABLE IF EXISTS skill_tag;
CREATE TABLE skill_tag (
                           tag_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID，主键',
                           tag_name VARCHAR(64) UNIQUE NOT NULL COMMENT '标签名（如Python、Java）',
                           category VARCHAR(32) COMMENT '分类（编程语言、框架、工具等）',
                           sort_order INT DEFAULT 0 COMMENT '排序权重',

                           created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能标签表';

-- =====================================================
-- 2. 创建文件资源表（被多个表引用）
-- =====================================================
DROP TABLE IF EXISTS file_resource;
CREATE TABLE file_resource (
                               file_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID，主键',
                               file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
                               file_key VARCHAR(255) NOT NULL COMMENT '存储key（OSS路径/对象存储路径）',
                               file_url VARCHAR(512) NOT NULL COMMENT '访问URL（CDN/OSS访问地址）',
                               file_size BIGINT COMMENT '文件大小（字节）',
                               file_type VARCHAR(64) COMMENT 'MIME类型（如：image/jpeg, application/pdf）',
                               file_extension VARCHAR(16) COMMENT '文件扩展名（如：jpg, pdf）',
                               md5_hash VARCHAR(64) COMMENT '文件MD5值（用于去重校验）',

    -- 业务关联
                               user_id INT NOT NULL COMMENT '上传用户ID，关联user表',
                               target_type TINYINT NOT NULL COMMENT '关联类型：1-用户简历 2-技能认证证书 3-帖子图片 4-评论图片 5-项目申请附件 6-人才卡片附件 7-用户头像 8-认证证明材料 9-团队帖子附件',
                               target_id INT COMMENT '关联业务ID（如skill_cert_id, post_id等）',

    -- 状态控制
                               is_temp BOOLEAN DEFAULT TRUE COMMENT '是否临时文件（用于清理未关联的临时文件）',
                               is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除标记（软删除）',

    -- 时间字段
                               created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                               update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               deleted_time TIMESTAMP NULL COMMENT '删除时间',

    -- 索引（暂时不添加外键约束，等user表创建后再添加）
                               INDEX idx_user_id (user_id),
                               INDEX idx_target (target_type, target_id),
                               INDEX idx_md5_hash (md5_hash),
                               INDEX idx_created_time (created_time),
                               INDEX idx_is_temp (is_temp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件资源表（统一管理所有上传文件）';

-- =====================================================
-- 3. 创建用户表（引用file_resource）
-- =====================================================
DROP TABLE IF EXISTS user;
CREATE TABLE user (
                      user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID，主键',
                      student_id VARCHAR(32) UNIQUE NULL COMMENT '学号（支持字母+数字）',
                      username VARCHAR(32) NOT NULL COMMENT '用户姓名，真实姓名',
                      nickname VARCHAR(32) COMMENT '用户昵称，可重复',
                      openid VARCHAR(64) UNIQUE COMMENT '微信OpenID（用于一键登录）',
                      unionid VARCHAR(64) COMMENT '微信UnionID（多平台关联）',
                      wechat_nickname VARCHAR(64) COMMENT '微信原始昵称',
                      phone VARCHAR(20) COMMENT '手机号，加密存储',
                      email VARCHAR(64) UNIQUE NOT NULL COMMENT '邮箱地址',
                      password VARCHAR(64) NOT NULL COMMENT '密码，加密存储（BCrypt）',
                      avatar_file_id BIGINT COMMENT '头像文件ID，关联file_resource表',
                      gender TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
                      birthday DATE COMMENT '出生日期',
                      school VARCHAR(128) COMMENT '学校名称',
                      major VARCHAR(64) COMMENT '专业',
                      grade VARCHAR(16) COMMENT '年级（如：2021级）',
                      tech_stack TEXT COMMENT '技术栈（如"Python,Java,机器学习"）',
                      personal_intro TEXT COMMENT '个人简介',
                      award_experience TEXT COMMENT '获奖经历',

                      role VARCHAR(16) DEFAULT 'student' COMMENT '角色：admin/student',
                      auth_status TINYINT DEFAULT 0 COMMENT '认证状态：0-待审核 1-已通过 2-已驳回',
                      audit_time TIMESTAMP NULL COMMENT '审核时间',
                      auditor_user_id INT COMMENT '审核人ID，关联user表',
                      remark VARCHAR(255) COMMENT '审核备注/驳回原因',

                      is_talent_visible BOOLEAN DEFAULT FALSE COMMENT '人才卡片是否可见（快捷开关）',
                      talent_card_id INT COMMENT '当前使用的人才卡片ID',

                      message_notify BOOLEAN DEFAULT TRUE COMMENT '新消息通知',
                      project_update_notify BOOLEAN DEFAULT TRUE COMMENT '项目状态更新通知',
                      invitation_notify BOOLEAN DEFAULT TRUE COMMENT '组队邀请通知',
                      system_notify BOOLEAN DEFAULT TRUE COMMENT '系统通知',

                      status BOOLEAN DEFAULT FALSE COMMENT '是否冻结：TRUE-冻结 FALSE-正常',
                      last_login_time TIMESTAMP NULL COMMENT '最后登录时间',
                      login_count INT DEFAULT 0 COMMENT '登录次数',

                      created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                      update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                      INDEX idx_auth_status (auth_status),
                      INDEX idx_role (role),
                      INDEX idx_created_time (created_time),
                      INDEX idx_is_talent_visible (is_talent_visible)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 4. 为file_resource添加外键约束（此时user表已存在）
-- =====================================================
ALTER TABLE file_resource ADD CONSTRAINT fk_file_resource_user
    FOREIGN KEY (user_id) REFERENCES user(user_id);

-- =====================================================
-- 5. 为用户表添加外键约束
-- =====================================================
ALTER TABLE user ADD CONSTRAINT fk_user_avatar
    FOREIGN KEY (avatar_file_id) REFERENCES file_resource(file_id);

ALTER TABLE user ADD CONSTRAINT fk_user_auditor
    FOREIGN KEY (auditor_user_id) REFERENCES user(user_id);

-- =====================================================
-- 6. 创建项目表（引用user）
-- =====================================================
DROP TABLE IF EXISTS project;
CREATE TABLE project (
                         project_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID，主键',
                         name VARCHAR(128) NOT NULL COMMENT '项目名称',
                         belong_track VARCHAR(128) COMMENT '所属赛道（大创、挑战杯等）',
                         level TINYINT COMMENT '级别：1-校级 2-省级 3-国家级',
                         project_type VARCHAR(64) COMMENT '类型：创新训练/创业实践',
                         project_intro TEXT COMMENT '项目详细介绍',
                         project_progress TEXT COMMENT '项目进展说明',
                         project_features TEXT COMMENT '项目特点/亮点',
                         tags VARCHAR(255) COMMENT '项目标签（逗号分隔）',
                         attachment_file_id BIGINT NULL COMMENT '项目说明等关联附件，关联file_resource表',
                         allow_cross_major_application BOOLEAN DEFAULT TRUE COMMENT '是否允许跨专业申请',

                         publisher_user_id INT NOT NULL COMMENT '发布人ID，关联user表',
                         is_anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名发布',
                         contact_info TEXT COMMENT '匿名时显示的临时联系方式',

                         release_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                         deadline_recruit DATETIME NULL COMMENT '招募截止时间',

                         view_count INT DEFAULT 0 COMMENT '浏览次数',
                         favorite_count INT DEFAULT 0 COMMENT '收藏次数',
                         apply_count INT DEFAULT 0 COMMENT '申请人数',

                         audit_status TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-通过 2-驳回',
                         audit_time TIMESTAMP NULL COMMENT '审核时间',
                         auditor_user_id INT COMMENT '审核人ID，关联user表',
                         remark VARCHAR(255) COMMENT '审核备注/驳回原因',

                         status TINYINT DEFAULT 2 COMMENT '项目状态：0-草拟 1-实施 2-招募中 3-完成 4-终止',
                         update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                         FOREIGN KEY (publisher_user_id) REFERENCES user(user_id),
                         FOREIGN KEY (auditor_user_id) REFERENCES user(user_id),

                         INDEX idx_publisher (publisher_user_id),
                         INDEX idx_status (status),
                         INDEX idx_audit_status (audit_status),
                         INDEX idx_release_time (release_time),
                         INDEX idx_deadline (deadline_recruit),
                         INDEX idx_belong_track (belong_track)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- =====================================================
-- 7. 创建项目角色要求表（引用project）
-- =====================================================
DROP TABLE IF EXISTS project_role_requirements;
CREATE TABLE project_role_requirements (
                                           requirement_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色要求ID，主键',
                                           project_id INT NOT NULL COMMENT '项目ID，关联project表',
                                           role VARCHAR(64) NOT NULL COMMENT '所需角色名（如：后端开发）',
                                           member_quota INT DEFAULT 1 COMMENT '招募人数',
                                           current_applicants INT DEFAULT 0 COMMENT '当前已申请人数',
                                           current_members INT DEFAULT 0 COMMENT '当前已加入人数',
                                           recruit_requirements TEXT COMMENT '具体招募要求（技能要求）',

                                           created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                           FOREIGN KEY (project_id) REFERENCES project(project_id),
                                           UNIQUE KEY unique_project_role (project_id, role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目角色要求表';

-- =====================================================
-- 8. 创建入队申请表（引用user, project, project_role_requirements, file_resource）
-- =====================================================
DROP TABLE IF EXISTS team_application;
CREATE TABLE team_application (
                                  application_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID，主键',
                                  applicant_user_id INT NOT NULL COMMENT '申请人ID，关联user表',
                                  project_id INT NOT NULL COMMENT '项目ID，关联project表',
                                  requirement_id INT COMMENT '申请的角色要求ID',
                                  role VARCHAR(64) COMMENT '申请的岗位角色',

                                  apply_reason TEXT COMMENT '申请原因/自我介绍',
                                  custom_resume_file_id BIGINT COMMENT '投递专用简历文件ID，关联file_resource表',
                                  application_attachment_file_id BIGINT COMMENT '其他附件文件ID，关联file_resource表',

                                  result TINYINT DEFAULT 0 COMMENT '结果：0-待审核 1-通过 2-驳回',
                                  audit_time TIMESTAMP NULL COMMENT '审核时间',
                                  auditor_user_id INT COMMENT '审核人ID（队长），关联user表',
                                  remark VARCHAR(255) COMMENT '审核备注/驳回原因',

                                  apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                                  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                  FOREIGN KEY (applicant_user_id) REFERENCES user(user_id),
                                  FOREIGN KEY (project_id) REFERENCES project(project_id),
                                  FOREIGN KEY (requirement_id) REFERENCES project_role_requirements(requirement_id),
                                  FOREIGN KEY (auditor_user_id) REFERENCES user(user_id),
                                  FOREIGN KEY (custom_resume_file_id) REFERENCES file_resource(file_id),
                                  FOREIGN KEY (application_attachment_file_id) REFERENCES file_resource(file_id),

                                  UNIQUE KEY unique_application (applicant_user_id, project_id),
                                  INDEX idx_applicant (applicant_user_id),
                                  INDEX idx_project (project_id),
                                  INDEX idx_result (result),
                                  INDEX idx_apply_time (apply_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入队申请表';

-- =====================================================
-- 9. 创建团队成员表（引用project, user）
-- =====================================================
DROP TABLE IF EXISTS team_member;
CREATE TABLE team_member (
                             team_member_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '成员关联ID，主键',
                             project_id INT NOT NULL COMMENT '项目ID，关联project表',
                             user_id INT NOT NULL COMMENT '用户ID，关联user表',
                             role VARCHAR(64) COMMENT '在团队中的角色',
                             join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                             status TINYINT DEFAULT 0 COMMENT '状态：0-在队 1-已退出 2-被移除',
                             update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                             FOREIGN KEY (project_id) REFERENCES project(project_id),
                             FOREIGN KEY (user_id) REFERENCES user(user_id),

                             UNIQUE KEY unique_project_user (project_id, user_id),
                             INDEX idx_user_id (user_id),
                             INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';

-- =====================================================
-- 10. 创建团队帖子表（引用project, user）
-- =====================================================
DROP TABLE IF EXISTS team_post;
CREATE TABLE team_post (
                           post_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID，主键',
                           project_id INT NOT NULL COMMENT '项目ID，关联project表',
                           user_id INT NOT NULL COMMENT '发布者ID，关联user表',
                           title VARCHAR(128) NOT NULL COMMENT '帖子标题',
                           content TEXT NOT NULL COMMENT '帖子内容',
                           status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-删除',

                           created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                           update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                           FOREIGN KEY (project_id) REFERENCES project(project_id),
                           FOREIGN KEY (user_id) REFERENCES user(user_id),

                           INDEX idx_project_created (project_id, created_time),
                           INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队帖子表';

-- =====================================================
-- 11. 创建团队帖子附件关联表（引用team_post, file_resource）
-- =====================================================
DROP TABLE IF EXISTS team_post_attachment;
CREATE TABLE team_post_attachment (
                                      relation_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID，主键',
                                      post_id INT NOT NULL COMMENT '帖子ID，关联team_post表',
                                      file_id BIGINT NOT NULL COMMENT '文件ID，关联file_resource表',
                                      sort_order INT DEFAULT 0 COMMENT '附件排序',
                                      created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                                      FOREIGN KEY (post_id) REFERENCES team_post(post_id) ON DELETE CASCADE,
                                      FOREIGN KEY (file_id) REFERENCES file_resource(file_id),

                                      UNIQUE KEY unique_post_file (post_id, file_id),
                                      INDEX idx_post_id (post_id),
                                      INDEX idx_file_id (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队帖子附件关联表';

-- =====================================================
-- 12. 创建团队任务表（引用project, user）
-- =====================================================
DROP TABLE IF EXISTS team_task;
CREATE TABLE team_task (
                           task_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID，主键',
                           project_id INT NOT NULL COMMENT '项目ID，关联project表',
                           title VARCHAR(128) NOT NULL COMMENT '任务标题',
                           description TEXT COMMENT '任务描述',
                           assignee_id INT NOT NULL COMMENT '负责人ID，关联user表',
                           creator_id INT NOT NULL COMMENT '创建者ID，关联user表',
                           deadline DATE COMMENT '截止日期',
                           status TINYINT DEFAULT 0 COMMENT '状态：0-待办 1-进行中 2-已完成',

                           created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                           FOREIGN KEY (project_id) REFERENCES project(project_id),
                           FOREIGN KEY (assignee_id) REFERENCES user(user_id),
                           FOREIGN KEY (creator_id) REFERENCES user(user_id),

                           INDEX idx_project_status (project_id, status),
                           INDEX idx_assignee_id (assignee_id),
                           INDEX idx_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队任务表';

-- =====================================================
-- 13. 创建人才卡片表（引用user, file_resource）
-- =====================================================
DROP TABLE IF EXISTS talent_card;
CREATE TABLE talent_card (
                             card_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '卡片ID，主键',
                             user_id INT NOT NULL COMMENT '用户ID，关联user表',

    -- 卡片状态
                             status TINYINT DEFAULT 0 COMMENT '状态：0-关闭（求组队关） 1-开启（求组队开） 2-已组队完成',
                             is_visible BOOLEAN DEFAULT TRUE COMMENT '是否可见（F-15隐私设置）',

    -- 展示信息（可从user表同步，但允许独立修改）
                             display_name VARCHAR(32) COMMENT '展示姓名（可匿名）',
                             major VARCHAR(64) COMMENT '专业',
                             grade VARCHAR(16) COMMENT '年级',

    -- 人才卡片专属字段
                             card_title VARCHAR(128) COMMENT '卡片标题（如：寻找大创队友）',
                             target_direction VARCHAR(255) COMMENT '期望方向（如：后端开发/算法/产品）',
                             expected_competition VARCHAR(128) COMMENT '期望参赛（大创/挑战杯/互联网+）',
                             expected_role VARCHAR(64) COMMENT '期望角色（队员/队长）',
                             self_statement TEXT COMMENT '自我陈述（补充说明）',

    -- 技能标签（冗余存储，便于查询）
                             skill_tags VARCHAR(255) COMMENT '技能标签（逗号分隔，如：Python,Java,机器学习）',

    -- 附件资料（改为文件ID关联）
                             resume_file_id BIGINT COMMENT '简历附件文件ID，关联file_resource表',
                             portfolio_file_id BIGINT COMMENT '作品集文件ID，关联file_resource表',
                             github_url VARCHAR(128) COMMENT 'GitHub地址',

    -- 统计数据
                             view_count INT DEFAULT 0 COMMENT '浏览次数',
                             invite_count INT DEFAULT 0 COMMENT '被邀请次数',

    -- 时间字段
                             created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             last_visible_time TIMESTAMP NULL COMMENT '最后可见时间（用于排序）',

                             FOREIGN KEY (user_id) REFERENCES user(user_id),
                             FOREIGN KEY (resume_file_id) REFERENCES file_resource(file_id),
                             FOREIGN KEY (portfolio_file_id) REFERENCES file_resource(file_id),
                             UNIQUE KEY unique_user_card (user_id),
                             INDEX idx_status (status),
                             INDEX idx_expected_competition (expected_competition),
                             INDEX idx_last_visible (last_visible_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人才卡片表';

-- =====================================================
-- 14. 创建队长邀请记录表（引用user, project, talent_card）
-- =====================================================
DROP TABLE IF EXISTS talent_invitation;
CREATE TABLE talent_invitation (
                                   invitation_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '邀请ID',
                                   captain_id INT NOT NULL COMMENT '队长ID（邀请方）',
                                   talent_id INT NOT NULL COMMENT '人才ID（被邀请方）',
                                   project_id INT COMMENT '关联项目ID',
                                   talent_card_id INT NOT NULL COMMENT '被邀请的人才卡片ID',

    -- 邀请内容
                                   invitation_message TEXT COMMENT '邀请附言',
                                   project_name VARCHAR(128) COMMENT '项目名称（冗余）',
                                   project_role VARCHAR(64) COMMENT '邀请担任的角色',

    -- 状态
                                   status TINYINT DEFAULT 0 COMMENT '状态：0-待回复 1-已接受 2-已拒绝 3-已过期',
                                   read_status BOOLEAN DEFAULT FALSE COMMENT '是否已读',

    -- 时间
                                   send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                                   read_time TIMESTAMP NULL COMMENT '阅读时间',
                                   response_time TIMESTAMP NULL COMMENT '回复时间',

                                   FOREIGN KEY (captain_id) REFERENCES user(user_id),
                                   FOREIGN KEY (talent_id) REFERENCES user(user_id),
                                   FOREIGN KEY (project_id) REFERENCES project(project_id),
                                   FOREIGN KEY (talent_card_id) REFERENCES talent_card(card_id),

                                   INDEX idx_captain (captain_id),
                                   INDEX idx_talent (talent_id),
                                   INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队长邀请记录表';

-- =====================================================
-- 15. 创建人才浏览记录表（引用user, talent_card）
-- =====================================================
DROP TABLE IF EXISTS talent_view_history;
CREATE TABLE talent_view_history (
                                     view_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '浏览记录ID',
                                     viewer_id INT NOT NULL COMMENT '浏览者ID（通常是队长）',
                                     talent_id INT NOT NULL COMMENT '被浏览的人才ID',
                                     talent_card_id INT NOT NULL COMMENT '被浏览的卡片ID',

                                     view_duration INT DEFAULT 0 COMMENT '浏览时长（秒）',
                                     source VARCHAR(32) COMMENT '来源：搜索/推荐/关注列表',

                                     created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',

                                     FOREIGN KEY (viewer_id) REFERENCES user(user_id),
                                     FOREIGN KEY (talent_id) REFERENCES user(user_id),
                                     FOREIGN KEY (talent_card_id) REFERENCES talent_card(card_id),

                                     INDEX idx_viewer (viewer_id),
                                     INDEX idx_talent (talent_id),
                                     INDEX idx_created (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人才浏览记录表';

-- =====================================================
-- 16. 创建用户-技能标签关联表（引用user, skill_tag）
-- =====================================================
DROP TABLE IF EXISTS user_skill_relation;
CREATE TABLE user_skill_relation (
                                     relation_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID，主键',
                                     user_id INT NOT NULL COMMENT '用户ID，关联user表',
                                     tag_id INT NOT NULL COMMENT '标签ID，关联skill_tag表',
                                     proficiency TINYINT DEFAULT 1 COMMENT '熟练度：1-了解 2-熟悉 3-精通',

                                     created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                     FOREIGN KEY (user_id) REFERENCES user(user_id),
                                     FOREIGN KEY (tag_id) REFERENCES skill_tag(tag_id),

                                     UNIQUE KEY unique_user_skill (user_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-技能标签关联表';

-- =====================================================
-- 17. 创建会话表（引用user, project）
-- =====================================================
DROP TABLE IF EXISTS chat_session;
CREATE TABLE chat_session (
                              session_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID，主键',
                              user1_id INT NOT NULL COMMENT '参与者1，关联user表',
                              user2_id INT NOT NULL COMMENT '参与者2，关联user表',
                              project_id INT COMMENT '关联项目ID（如果是项目相关聊天）',

                              last_message TEXT COMMENT '最后一条消息内容',
                              last_msg_time TIMESTAMP NULL COMMENT '最后消息时间',
                              user1_unread INT DEFAULT 0 COMMENT '用户1未读数',
                              user2_unread INT DEFAULT 0 COMMENT '用户2未读数',
                              recruit_status VARCHAR(32) DEFAULT 'communicating' COMMENT '招募沟通状态：communicating/offer/reject',

                              status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-删除',
                              update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                              FOREIGN KEY (user1_id) REFERENCES user(user_id),
                              FOREIGN KEY (user2_id) REFERENCES user(user_id),
                              FOREIGN KEY (project_id) REFERENCES project(project_id),

                              UNIQUE KEY unique_chat (user1_id, user2_id, project_id),
                              INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- =====================================================
-- 18. 创建消息表（引用chat_session, user）
-- =====================================================
DROP TABLE IF EXISTS message;
CREATE TABLE message (
                         message_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID，主键',
                         session_id INT NOT NULL COMMENT '会话ID，关联chat_session表',
                         sender_id INT NOT NULL COMMENT '发送方ID，关联user表',
                         receiver_id INT NOT NULL COMMENT '接收方ID，关联user表',

                         content TEXT NOT NULL COMMENT '消息内容',
                         msg_type TINYINT DEFAULT 1 COMMENT '类型：1-文字 2-图片 3-系统通知 4-投递卡片 5-邀请卡片',
                         status TINYINT DEFAULT 0 COMMENT '状态：0-未读 1-已读 2-撤回',

                         send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',

                         FOREIGN KEY (session_id) REFERENCES chat_session(session_id),
                         FOREIGN KEY (sender_id) REFERENCES user(user_id),
                         FOREIGN KEY (receiver_id) REFERENCES user(user_id),

                         INDEX idx_session_id (session_id),
                         INDEX idx_sender_receiver (sender_id, receiver_id),
                         INDEX idx_send_time (send_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- =====================================================
-- 19. 创建联系方式交换记录表（引用user, project）
-- =====================================================
DROP TABLE IF EXISTS contact_exchange;
CREATE TABLE contact_exchange (
                                  exchange_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '交换ID，主键',
                                  requester_id INT NOT NULL COMMENT '发起方ID，关联user表',
                                  receiver_id INT NOT NULL COMMENT '接收方ID，关联user表',
                                  project_id INT COMMENT '关联项目ID',

                                  status TINYINT DEFAULT 0 COMMENT '状态：0-待确认 1-已同意 2-已拒绝',
                                  request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
                                  response_time TIMESTAMP NULL COMMENT '响应时间',

                                  FOREIGN KEY (requester_id) REFERENCES user(user_id),
                                  FOREIGN KEY (receiver_id) REFERENCES user(user_id),
                                  FOREIGN KEY (project_id) REFERENCES project(project_id),

                                  UNIQUE KEY unique_exchange (requester_id, receiver_id, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联系方式交换记录表';

-- =====================================================
-- 20. 创建社区帖子表（引用user）
-- =====================================================
DROP TABLE IF EXISTS community_post;
CREATE TABLE community_post (
                                post_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID，主键',
                                user_id INT NOT NULL COMMENT '发布者ID，关联user表',
                                section TINYINT NOT NULL COMMENT '板块：1-技术交流 2-灵感分享 3-组队经验',

                                title VARCHAR(128) NOT NULL COMMENT '帖子标题',
                                content TEXT NOT NULL COMMENT '帖子内容',

                                view_count INT DEFAULT 0 COMMENT '浏览次数',
                                like_count INT DEFAULT 0 COMMENT '点赞数',
                                comment_count INT DEFAULT 0 COMMENT '评论数',

                                is_top BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
                                is_essence BOOLEAN DEFAULT FALSE COMMENT '是否精华',
                                status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-删除 2-违规下架',

                                created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                FOREIGN KEY (user_id) REFERENCES user(user_id),

                                INDEX idx_section (section),
                                INDEX idx_user_id (user_id),
                                INDEX idx_created_time (created_time),
                                INDEX idx_is_top (is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

-- =====================================================
-- 21. 创建帖子图片关联表（引用community_post, file_resource）
-- =====================================================
DROP TABLE IF EXISTS post_image_relation;
CREATE TABLE post_image_relation (
                                     relation_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
                                     post_id INT NOT NULL COMMENT '帖子ID',
                                     file_id BIGINT NOT NULL COMMENT '文件ID',
                                     sort_order INT DEFAULT 0 COMMENT '排序顺序',
                                     created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                     FOREIGN KEY (post_id) REFERENCES community_post(post_id) ON DELETE CASCADE,
                                     FOREIGN KEY (file_id) REFERENCES file_resource(file_id),
                                     UNIQUE KEY unique_post_file (post_id, file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子图片关联表';

-- =====================================================
-- 22. 创建评论表（引用community_post, user, comment）
-- =====================================================
DROP TABLE IF EXISTS comment;
CREATE TABLE comment (
                         comment_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID，主键',
                         post_id INT NOT NULL COMMENT '帖子ID，关联community_post表',
                         user_id INT NOT NULL COMMENT '评论者ID，关联user表',
                         parent_id INT COMMENT '父评论ID（支持楼中楼）',

                         content TEXT NOT NULL COMMENT '评论内容',
                         like_count INT DEFAULT 0 COMMENT '点赞数',
                         status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-删除',

                         created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
                         update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                         FOREIGN KEY (post_id) REFERENCES community_post(post_id),
                         FOREIGN KEY (user_id) REFERENCES user(user_id),
                         FOREIGN KEY (parent_id) REFERENCES comment(comment_id),

                         INDEX idx_post_id (post_id),
                         INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- =====================================================
-- 23. 创建点赞表（引用user）
-- =====================================================
DROP TABLE IF EXISTS like_record;
CREATE TABLE like_record (
                             like_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID，主键',
                             user_id INT NOT NULL COMMENT '点赞用户ID，关联user表',
                             target_type TINYINT NOT NULL COMMENT '目标类型：1-帖子 2-评论',
                             target_id INT NOT NULL COMMENT '目标ID',

                             created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',

                             FOREIGN KEY (user_id) REFERENCES user(user_id),

                             UNIQUE KEY unique_like (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- =====================================================
-- 24. 创建收藏表（引用user）
-- =====================================================
DROP TABLE IF EXISTS favorite;
CREATE TABLE favorite (
                          favorite_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID，主键',
                          user_id INT NOT NULL COMMENT '用户ID，关联user表',
                          target_type TINYINT NOT NULL COMMENT '类型：1-项目 2-帖子 3-人才卡片',
                          target_id INT NOT NULL COMMENT '目标ID',

                          created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',

                          FOREIGN KEY (user_id) REFERENCES user(user_id),

                          UNIQUE KEY unique_favorite (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- =====================================================
-- 25. 创建关注表（引用user）
-- =====================================================
DROP TABLE IF EXISTS follow;
CREATE TABLE follow (
                        follow_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关注ID，主键',
                        follower_id INT NOT NULL COMMENT '关注者ID，关联user表',
                        following_id INT NOT NULL COMMENT '被关注者ID，关联user表',

                        created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',

                        FOREIGN KEY (follower_id) REFERENCES user(user_id),
                        FOREIGN KEY (following_id) REFERENCES user(user_id),

                        UNIQUE KEY unique_follow (follower_id, following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- =====================================================
-- 23. 创建技能认证表（引用user, file_resource）
-- =====================================================
DROP TABLE IF EXISTS skill_certification;
CREATE TABLE skill_certification (
                                     cert_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '认证ID，主键',
                                     user_id INT NOT NULL COMMENT '用户ID，关联user表',
                                     skill_name VARCHAR(64) NOT NULL COMMENT '技能名称',
                                     cert_name VARCHAR(128) NOT NULL COMMENT '证书名称',
                                     cert_file_id BIGINT COMMENT '证书文件ID，关联file_resource表',
                                     issue_date DATE COMMENT '发证日期',
                                     issuer VARCHAR(128) COMMENT '发证机构',
                                     cert_number VARCHAR(64) COMMENT '证书编号',
                                     description TEXT COMMENT '证书描述/补充说明',

                                     status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-删除',
                                     audit_status TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已驳回',
                                     audit_time TIMESTAMP NULL COMMENT '审核时间',
                                     auditor_user_id INT COMMENT '审核人ID',
                                     remark VARCHAR(255) COMMENT '审核备注/驳回原因',

                                     created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                     FOREIGN KEY (user_id) REFERENCES user(user_id),
                                     FOREIGN KEY (cert_file_id) REFERENCES file_resource(file_id),
                                     FOREIGN KEY (auditor_user_id) REFERENCES user(user_id),

                                     INDEX idx_user_id (user_id),
                                     INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能认证表';

-- =====================================================
-- 24. 创建身份认证材料表（引用user, file_resource）
-- =====================================================
DROP TABLE IF EXISTS auth_material;
CREATE TABLE auth_material (
                               material_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '材料ID',
                               user_id INT NOT NULL COMMENT '用户ID',
                               material_type TINYINT NOT NULL COMMENT '材料类型：1-学生证 2-身份证 3-校园卡 4-学信网证明',
                               file_id BIGINT NOT NULL COMMENT '文件ID，关联file_resource表',
                               description VARCHAR(255) COMMENT '材料描述',
                               audit_status TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已驳回',
                               audit_time TIMESTAMP NULL COMMENT '审核时间',
                               auditor_user_id INT COMMENT '审核人ID',
                               remark VARCHAR(255) COMMENT '审核备注',

                               created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               FOREIGN KEY (user_id) REFERENCES user(user_id),
                               FOREIGN KEY (file_id) REFERENCES file_resource(file_id),
                               FOREIGN KEY (auditor_user_id) REFERENCES user(user_id),

                               INDEX idx_user_id (user_id),
                               INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身份认证材料表';
