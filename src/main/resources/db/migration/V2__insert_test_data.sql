-- =====================================================
-- TeamMatching 联调测试数据
-- 请先执行 init_db.sql 以重建空数据库，再执行本文件
-- 说明：
-- 1) 本文件仅包含 INSERT 语句，不包含 CREATE/ALTER。
-- 2) 所有测试账号统一登录口令为：123456
-- 3) 当前 password 字段写入的是 123456 的同一个 BCrypt 哈希值，便于走登录校验。
-- 4) 若登录失败，请检查项目登录实现是否要求 BCrypt/PasswordEncoder。
-- =====================================================

-- 技能标签：用于 /common/skill-tags 与技能展示联调
INSERT INTO skill_tag (tag_id, tag_name, category, sort_order) VALUES
                                                                   (1, 'Java', '编程语言', 10),
                                                                   (2, 'Spring Boot', '后端框架', 9),
                                                                   (3, 'MySQL', '数据库', 8),
                                                                   (4, 'Vue', '前端框架', 7),
                                                                   (5, 'Python', '编程语言', 6);

-- 用户：核心登录账号与个人资料展示数据（统一密码 123456）
-- 测试账号：
-- 101 / tm_user101@example.com / 123456
-- 102 / tm_user102@example.com / 123456
-- 103 / tm_user103@example.com / 123456
-- 104 / tm_user104@example.com / 123456
-- 105 / tm_user105@example.com / 123456
INSERT INTO user (
    user_id, student_id, username, nickname, openid, phone, email, password,
    major, grade, tech_stack, personal_intro, role, auth_status,
    is_talent_visible, talent_card_id, status
) VALUES
      (101, 'TM2026001', '张三', '老三', 'wx_tm_u101', '13800000001', 'tm_user101@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '软件工程', '2022级', 'Java,Spring Boot,MySQL', '创新项目队长，负责后端与协调', 'student', 1, 0, NULL, 0),
      (102, 'TM2026002', '李四', '老四', 'wx_tm_u102', '13800000002', 'tm_user102@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '计算机科学与技术', '2023级', 'Java,Spring Boot,Redis', '后端开发方向，正在寻找组队', 'student', 1, 1, 301, 0),
      (103, 'TM2026003', '王五', '老五', 'wx_tm_u103', '13800000003', 'tm_user103@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '数据科学', '2022级', 'Python,机器学习,数据挖掘', '偏算法方向，关注竞赛落地效果', 'student', 1, 0, 302, 0),
      (104, 'TM2026004', 'David Product', 'david', 'wx_tm_u104', '13800000004', 'tm_user104@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '信息管理', '2024级', '产品,Vue,SQL', '偏产品方向的组队成员', 'student', 1, 0, 303, 0),
      (105, 'TM2026005', 'Eva Design', 'eva', 'wx_tm_u105', '13800000005', 'tm_user105@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '数字媒体', '2023级', 'UI,UX,Figma', '负责设计与交互体验', 'student', 1, 0, NULL, 0);

-- 项目：用于人才邀请与联系方式交换（project_id 外键）
INSERT INTO project (
    project_id, name, belong_track, level, project_type, project_intro, tags,
    publisher_user_id, is_anonymous, status, audit_status
) VALUES
      (201, '校园智能助手', '挑战杯', 2, '创新训练',
       '面向校内师生的智能问答与事务助手', 'AI,助手,校园',
       101, 0, 2, 1),
      (202, '智能排课规划器', '互联网+', 1, '创新训练',
       '用于课程安排与学习负载平衡的工具平台', '教育,排课,Web',
       104, 0, 2, 1);

-- 人才卡：覆盖开启/关闭/已组队三种状态，供列表与详情联调
INSERT INTO talent_card (
    card_id, user_id, status, is_visible, display_name, major, grade,
    card_title, target_direction, expected_competition, expected_role,
    self_statement, skill_tags, github_url, view_count, invite_count, last_visible_time
) VALUES
      (301, 102, 1, 1, '李四', '计算机科学与技术', '2023级',
       '校园助手项目寻找队友', '后端开发', '挑战杯', '队员',
       '可承担后端接口开发与数据库设计', 'Java,Spring Boot,MySQL',
       'https://github.com/tm-bob', 36, 3, '2026-04-02 10:00:00'),
      (302, 103, 0, 0, '王五', '数据科学', '2022级',
       '算法方向成员近期可沟通', '算法', '挑战杯', '队员',
       '近期备考，暂时关闭展示', 'Python,机器学习',
       'https://github.com/tm-carol', 18, 1, '2026-03-28 09:30:00'),
      (303, 104, 2, 0, 'david', '信息管理', '2024级',
       '产品岗位已完成匹配', '产品管理', '互联网+', '队长',
       '团队已组满，卡片仅保留历史展示', '产品,Vue,SQL',
       'https://github.com/tm-david', 22, 2, '2026-03-25 14:00:00');

-- 用户技能关联：补充用户技能画像（可用于推荐与展示）
INSERT INTO user_skill_relation (relation_id, user_id, tag_id, proficiency) VALUES
                                                                                (1, 101, 1, 3),
                                                                                (2, 101, 3, 2),
                                                                                (3, 102, 1, 3),
                                                                                (4, 102, 2, 3),
                                                                                (5, 103, 5, 3),
                                                                                (6, 104, 4, 2);

-- 关注关系：用于人才列表 isFollowing 展示
INSERT INTO follow (follow_id, follower_id, following_id) VALUES
                                                              (1, 101, 102),
                                                              (2, 104, 102);

-- 聊天会话：联系方式交换请求接口依赖 session_id
INSERT INTO chat_session (
    session_id, user1_id, user2_id, project_id, last_message, last_msg_time,
    user1_unread, user2_unread, recruit_status, status
) VALUES
      (401, 101, 102, 201, '先聊一下后端分工。', '2026-04-02 11:00:00', 0, 1, 'communicating', 1),
      (402, 101, 103, 201, '你的算法背景很匹配当前需求。', '2026-04-02 12:15:00', 0, 0, 'offer', 1),
      (403, 104, 105, 202, '排课页面需要你协助 UI 设计。', '2026-04-01 20:40:00', 0, 0, 'communicating', 1);

-- 联系方式交换：覆盖待确认/已同意/已拒绝三种状态
INSERT INTO contact_exchange (
    exchange_id, requester_id, receiver_id, project_id, status, request_time, response_time
) VALUES
      (501, 101, 102, 201, 0, '2026-04-02 11:10:00', NULL),
      (502, 102, 101, 201, 1, '2026-04-01 09:00:00', '2026-04-01 09:30:00'),
      (503, 104, 105, 202, 2, '2026-04-01 21:00:00', '2026-04-01 21:20:00');

-- 社区帖子：覆盖列表页与详情页展示
INSERT INTO community_post (
    post_id, user_id, section, title, content,
    view_count, like_count, comment_count, is_top, is_essence, status
) VALUES
      (601, 101, 3, '组建挑战杯团队流程',
       '分享从找队友到角色分工的完整实践流程。',
       120, 15, 2, 1, 0, 1),
      (602, 103, 1, '高效特征工程经验整理',
       '给学生项目准备的一份简明特征工程检查清单。',
       88, 9, 1, 0, 1, 1);

-- 评论：至少 3 条，含 1 条楼中楼回复
INSERT INTO comment (
    comment_id, post_id, user_id, parent_id, content, like_count, status, created_time
) VALUES
      (701, 601, 102, NULL, '总结得很好（尤其是角色拆分部分）', 3, 1, '2026-04-02 13:00:00'),
      (702, 601, 101, 701, '下一篇补上可复用模板', 1, 1, '2026-04-02 13:15:00'),
      (703, 602, 104, NULL, '内容很实用，但是能否补一个小型示例数据集？', 2, 1, '2026-04-02 16:20:00');

-- 人才邀请：补充人才模块“发送邀请”链路联调数据
INSERT INTO talent_invitation (
    invitation_id, captain_id, talent_id, project_id, talent_card_id,
    invitation_message, project_name, project_role, status, read_status, send_time
) VALUES
      (801, 101, 102, 201, 301, '求后端', '校园智能助手', '后端开发', 0, 0, '2026-04-02 10:30:00'),
      (802, 101, 103, 201, 302, '关于排序模块算法，有空详聊。', '校园智能助手', '算法工程', 1, 1, '2026-03-30 18:00:00');

-- =====================================================
-- 追加联调扩展数据（仅新增，不修改已有数据）
-- =====================================================

INSERT INTO user (
    user_id, student_id, username, nickname, openid, phone, email, password,
    major, grade, tech_stack, personal_intro, role, auth_status,
    is_talent_visible, talent_card_id, status
) VALUES
      (106, 'TM2026106', '赵六', '阿六', 'wx_tm_u106', '13800000006', 'tm_user106@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '人工智能', '2022级', 'Java,Spring Boot,Redis', '偏后端与系统架构方向，习惯写技术文档。', 'student', 1, 1, 304, 0),
      (107, 'TM2026107', '孙七', '小七', 'wx_tm_u107', '13800000007', 'tm_user107@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '软件工程', '2023级', 'Vue,TypeScript,Node.js', '前端方向，关注交互体验与可维护性。', 'student', 1, 0, 305, 0),
      (108, 'TM2026108', '周八', '周周', 'wx_tm_u108', '13800000008', 'tm_user108@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '计算机科学与技术', '2024级', 'Java,MySQL,Linux', '希望在真实项目里提升工程实践能力。', 'student', 1, 0, NULL, 0),
      (109, 'TM2026109', '吴九', '九哥', 'wx_tm_u109', '13800000009', 'tm_user109@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '数据科学', '2022级', 'Python,机器学习,PyTorch', '算法方向，擅长实验设计与模型调优。', 'student', 1, 1, 306, 0),
      (110, 'TM2026110', '郑十', '阿十', 'wx_tm_u110', '13800000010', 'tm_user110@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '物联网工程', '2021级', '嵌入式,C语言,数据可视化', '做过传感器采集与看板展示相关项目。', 'student', 1, 0, NULL, 0),
      (111, 'TM2026111', '冯一鸣', '一鸣', 'wx_tm_u111', '13800000011', 'tm_user111@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '信息管理', '2023级', '产品设计,Axure,SQL', '偏产品与需求分析，注重业务闭环。', 'student', 1, 0, 307, 0),
      (112, 'TM2026112', '陈晨', '晨晨', 'wx_tm_u112', '13800000012', 'tm_user112@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '网络工程', '2022级', 'Java,Spring Cloud,Docker', '后端开发，熟悉微服务部署。', 'student', 1, 0, NULL, 0),
      (113, 'TM2026113', '褚晴', '晴晴', 'wx_tm_u113', '13800000013', 'tm_user113@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '数字媒体', '2024级', 'UI,UX,Figma', '负责视觉与交互设计，擅长移动端界面。', 'student', 1, 1, 308, 0),
      (114, 'TM2026114', '卫宁', '小宁', 'wx_tm_u114', '13800000014', 'tm_user114@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '教育技术学', '2021级', '产品运营,数据分析,问卷设计', '擅长调研与用户访谈。', 'student', 1, 0, NULL, 0),
      (115, 'TM2026115', '蒋涛', '涛涛', 'wx_tm_u115', '13800000015', 'tm_user115@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '自动化', '2023级', 'Python,控制算法,OpenCV', '做过智能硬件与视觉检测小项目。', 'student', 1, 0, NULL, 0),
      (116, 'TM2026116', '沈悦', '小悦', 'wx_tm_u116', '13800000016', 'tm_user116@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '软件工程', '2022级', 'Java,MySQL,测试开发', '后端与测试双栈，关注质量保障。', 'student', 1, 1, 309, 0),
      (117, 'TM2026117', '韩磊', '磊子', 'wx_tm_u117', '13800000017', 'tm_user117@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '统计学', '2024级', 'R,Python,数据建模', '希望参与数据分析类竞赛项目。', 'student', 0, 0, NULL, 0),
      (118, 'TM2026118', '杨柳', '柳柳', 'wx_tm_u118', '13800000018', 'tm_user118@example.com',
       '$2a$10$9gyHxNzXYwNXPF333g1nG.8ToI9pQSvIvEwo53hof.RlkzYmCOUuu',
       '电子信息工程', '2021级', '嵌入式,电路设计,C++', '偏硬件与底层开发。', 'student', 1, 0, NULL, 0);

INSERT INTO project (
    project_id, name, belong_track, level, project_type, project_intro, tags,
    publisher_user_id, is_anonymous, contact_info, status, audit_status
) VALUES
      (203, '多模态校园导览助手', '挑战杯', 2, '创新训练',
       '结合文本与图像能力，提供校内场景导览与问答。', 'AI,多模态,校园服务',
       106, 0, NULL, 2, 1),
      (204, '智能自习室排座系统', '大创', 1, '创新训练',
       '支持预约、签到、违约记录与热度分析。', '预约系统,小程序,数据分析',
       107, 0, NULL, 1, 1),
      (205, '高校二手书流通平台', '互联网+', 1, '创业实践',
       '面向校内同学的二手教材交易与捐赠平台。', '电商,交易,校园',
       108, 0, NULL, 2, 0),
      (206, '低碳出行打卡小程序', '互联网+', 2, '创新训练',
       '记录步行与骑行行为，形成绿色积分体系。', '低碳,打卡,积分',
       109, 0, NULL, 3, 1),
      (207, '宿舍能耗可视化看板', '大创', 2, '创新训练',
       '接入宿舍能耗数据，形成可视化分析看板。', 'IoT,可视化,能耗',
       110, 0, NULL, 4, 1),
      (208, '竞赛资料知识图谱', '挑战杯', 3, '创新训练',
       '沉淀历届竞赛资料，构建可检索知识图谱。', '知识图谱,NLP,检索',
       111, 0, NULL, 2, 2),
      (209, '课堂互动问答系统', '互联网+', 1, '创新训练',
       '课堂实时问答与匿名提问，提高课堂参与度。', '教学,互动,问答',
       112, 0, NULL, 2, 1),
      (210, '校园失物招领平台', '大创', 1, '创新训练',
       '支持物品发布、认领匹配与流程留痕。', '平台,校园服务,匹配',
       113, 0, NULL, 2, 1),
      (211, '新生社团匹配推荐', '挑战杯', 2, '创新训练',
       '根据兴趣标签和时间偏好推荐合适社团。', '推荐系统,标签,社团',
       114, 0, NULL, 0, 0);

INSERT INTO talent_card (
    card_id, user_id, status, is_visible, display_name, major, grade,
    card_title, target_direction, expected_competition, expected_role,
    self_statement, skill_tags, github_url, view_count, invite_count, last_visible_time
) VALUES
      (304, 106, 1, 1, '阿六', '人工智能', '2022级',
       '希望参与校园 AI 产品落地', '后端开发/系统架构', '挑战杯', '队员',
       '可负责服务端架构、接口设计和部署。', 'Java,Spring Boot,Redis',
       'https://github.com/tm-zhaoliu', 29, 2, '2026-04-08 09:00:00'),
      (305, 107, 0, 0, '小七', '软件工程', '2023级',
       '前端同学，阶段性关闭中', '前端开发', '互联网+', '队员',
       '近期课程较满，暂不主动参与新队伍。', 'Vue,TypeScript',
       'https://github.com/tm-sunqi', 12, 0, '2026-04-01 14:20:00'),
      (306, 109, 1, 1, '九哥', '数据科学', '2022级',
       '寻找算法实践型队伍', '算法工程', '挑战杯', '队员',
       '可承担数据处理、特征工程与模型训练。', 'Python,机器学习,PyTorch',
       'https://github.com/tm-wujiu', 41, 4, '2026-04-09 16:30:00'),
      (307, 111, 2, 0, '一鸣', '信息管理', '2023级',
       '产品方向已完成组队', '产品经理', '大创', '队长',
       '当前项目成员已齐，暂不接新邀请。', '产品设计,SQL',
       'https://github.com/tm-fengyiming', 18, 1, '2026-03-29 10:10:00'),
      (308, 113, 1, 1, '晴晴', '数字媒体', '2024级',
       '希望加入有完整开发流程的团队', 'UI/UX 设计', '互联网+', '队员',
       '负责视觉规范、交互稿与高保真原型。', 'UI,UX,Figma',
       'https://github.com/tm-chuqing', 25, 1, '2026-04-07 11:40:00'),
      (309, 116, 1, 1, '小悦', '软件工程', '2022级',
       '后端与测试协同角色可投递', '后端开发/测试开发', '大创', '队员',
       '可支持接口开发与自动化测试联调。', 'Java,MySQL,测试开发',
       'https://github.com/tm-shenyue', 33, 2, '2026-04-10 08:20:00');

INSERT INTO user_skill_relation (relation_id, user_id, tag_id, proficiency) VALUES
                                                                                (7, 106, 1, 3),
                                                                                (8, 106, 2, 3),
                                                                                (9, 107, 4, 3),
                                                                                (10, 109, 5, 3),
                                                                                (11, 112, 2, 2),
                                                                                (12, 116, 3, 3);

INSERT INTO follow (follow_id, follower_id, following_id) VALUES
                                                              (3, 106, 101),
                                                              (4, 106, 109),
                                                              (5, 107, 106),
                                                              (6, 108, 102),
                                                              (7, 109, 113),
                                                              (8, 110, 106),
                                                              (9, 111, 101),
                                                              (10, 112, 103),
                                                              (11, 113, 107),
                                                              (12, 118, 116);

INSERT INTO chat_session (
    session_id, user1_id, user2_id, project_id, last_message, last_msg_time,
    user1_unread, user2_unread, recruit_status, status
) VALUES
      (404, 106, 101, 203, '张三，我这边接口清单已整理。', '2026-04-09 20:10:00', 0, 1, 'communicating', 1),
      (405, 107, 113, 210, '我想参与前端页面实现。', '2026-04-08 18:30:00', 0, 0, 'communicating', 1),
      (406, 112, 103, 209, '想请教你课堂问答的算法方案。', '2026-04-09 09:45:00', 0, 0, 'offer', 1),
      (407, 116, 102, 203, '可以一起对接数据库结构。', '2026-04-10 08:05:00', 0, 1, 'communicating', 1),
      (408, 114, 105, 211, '欢迎一起做社团推荐功能。', '2026-04-07 15:20:00', 0, 0, 'reject', 1);

INSERT INTO contact_exchange (
    exchange_id, requester_id, receiver_id, project_id, status, request_time, response_time
) VALUES
      (504, 106, 101, 203, 1, '2026-04-08 10:00:00', '2026-04-08 10:05:00'),
      (505, 107, 113, 210, 0, '2026-04-09 12:20:00', NULL),
      (506, 112, 103, 209, 2, '2026-04-09 09:50:00', '2026-04-09 10:15:00'),
      (507, 116, 102, 203, 1, '2026-04-10 08:10:00', '2026-04-10 08:25:00'),
      (508, 114, 105, 211, 0, '2026-04-07 16:00:00', NULL),
      (509, 109, 101, 206, 2, '2026-04-06 14:30:00', '2026-04-06 15:00:00');

INSERT INTO community_post (
    post_id, user_id, section, title, content,
    view_count, like_count, comment_count, is_top, is_essence, status
) VALUES
      (603, 106, 1, 'Spring Boot 接口分层实践', '总结了我在联调中使用的分层与异常处理方案。', 54, 6, 2, 0, 0, 1),
      (604, 107, 2, '从 0 到 1 设计项目首页', '分享一个面向校园产品的首页信息架构思路。', 38, 4, 2, 0, 0, 1),
      (605, 109, 1, '竞赛项目中的特征工程避坑', '记录了几个容易忽略但影响很大的细节。', 62, 8, 2, 0, 1, 1),
      (606, 112, 3, '组队沟通时如何减少返工', '从需求澄清和任务拆分两个角度给建议。', 45, 5, 2, 0, 0, 1),
      (607, 113, 2, '设计规范在学生项目中的价值', '统一设计规范后，联调效率明显提升。', 33, 3, 2, 0, 0, 1),
      (608, 116, 1, '后端联调自测清单模板', '提供一份可直接复用的接口联调自测清单。', 57, 7, 2, 0, 1, 1);

INSERT INTO comment (
    comment_id, post_id, user_id, parent_id, content, like_count, status, created_time
) VALUES
      (704, 603, 101, NULL, '这套分层思路很清晰，适合团队统一。', 2, 1, '2026-04-09 21:00:00'),
      (705, 603, 112, NULL, '异常码约定这块对联调帮助很大。', 1, 1, '2026-04-09 21:15:00'),
      (706, 604, 105, NULL, '首页结构图如果能补一版会更直观。', 1, 1, '2026-04-08 19:10:00'),
      (707, 604, 113, NULL, '我也认同先做信息层级再做视觉。', 0, 1, '2026-04-08 19:20:00'),
      (708, 605, 103, NULL, '特征泄漏提醒非常关键。', 3, 1, '2026-04-09 10:40:00'),
      (709, 605, 109, NULL, '后面我再补一篇实验对比细节。', 1, 1, '2026-04-09 10:55:00'),
      (710, 606, 106, NULL, '任务拆分模板能分享一下吗？', 1, 1, '2026-04-09 17:30:00'),
      (711, 606, 112, NULL, '可以，我晚点整理成表格发出来。', 0, 1, '2026-04-09 17:45:00'),
      (712, 607, 107, NULL, '设计规范确实能减少反复改稿。', 0, 1, '2026-04-08 13:20:00'),
      (713, 607, 104, NULL, '建议再加一套组件命名规则。', 1, 1, '2026-04-08 13:35:00'),
      (714, 608, 102, NULL, '这个清单我已经拿去给组员用了。', 2, 1, '2026-04-10 09:10:00'),
      (715, 608, 116, NULL, '欢迎补充更多联调前置检查项。', 1, 1, '2026-04-10 09:25:00');

INSERT INTO team_member (
    team_member_id, project_id, user_id, role, status
) VALUES
      (904, 203, 106, '队长', 0),
      (905, 203, 116, '后端开发', 0),
      (906, 210, 113, '产品负责人', 0);

INSERT INTO team_application (
    application_id, applicant_user_id, project_id, requirement_id, role,
    apply_reason, custom_resume_file_id, application_attachment_file_id,
    result, audit_time, auditor_user_id, remark, apply_time, update_time
) VALUES
      (1001, 107, 203, NULL, '前端开发',
       '希望参与完整联调流程，负责页面与接口对接。', NULL, NULL,
       0, NULL, NULL, NULL, '2026-04-09 18:00:00', '2026-04-09 18:00:00'),
      (1002, 112, 210, NULL, '测试工程',
       '可负责接口测试和回归测试用例整理。', NULL, NULL,
       1, '2026-04-08 20:10:00', 113, '沟通清晰，匹配岗位。', '2026-04-08 19:20:00', '2026-04-08 20:10:00'),
      (1003, 118, 209, NULL, '算法工程',
       '希望参与推荐策略与召回模块开发。', NULL, NULL,
       2, '2026-04-09 11:10:00', 112, '当前岗位需求以后端为主。', '2026-04-09 10:20:00', '2026-04-09 11:10:00');

INSERT INTO team_post (
    post_id, project_id, user_id, title, content, status
) VALUES
      (1201, 203, 106, '本周开发计划', '本周完成用户模块联调、日志追踪与部署脚本整理。', 1),
      (1202, 210, 113, '需求评审纪要', '已确认失物发布、认领审核、消息通知三个优先功能。', 1);

INSERT INTO team_task (
    task_id, project_id, title, description, assignee_id, creator_id, deadline, status
) VALUES
      (1301, 203, '完成登录接口联调', '对齐前后端字段并补充异常码说明。', 116, 106, '2026-04-15', 0),
      (1302, 203, '补充数据库索引检查', '确认高频查询字段索引是否完备。', 106, 106, '2026-04-14', 1),
      (1303, 210, '输出原型评审结论', '整理评审意见并更新下一版原型。', 113, 113, '2026-04-13', 0);

INSERT INTO message (
    message_id, session_id, sender_id, receiver_id, content, msg_type, status, send_time
) VALUES
      (1501, 404, 106, 101, '张三学长，我这边接口文档已更新。', 1, 1, '2026-04-09 20:05:00'),
      (1502, 404, 101, 106, '收到，今晚一起过字段定义。', 1, 0, '2026-04-09 20:10:00'),
      (1503, 405, 107, 113, '我想申请前端岗位，已看过需求文档。', 1, 0, '2026-04-08 18:30:00');

INSERT INTO like_record (
    like_id, user_id, target_type, target_id
) VALUES
      (1601, 106, 1, 603),
      (1602, 107, 2, 704),
      (1603, 112, 1, 606);

INSERT INTO favorite (
    favorite_id, user_id, target_type, target_id
) VALUES
      (1701, 106, 1, 203),
      (1702, 107, 2, 603),
      (1703, 118, 3, 309);

INSERT INTO file_resource (
    file_id, file_name, file_key, file_url, file_size, file_type, file_extension, md5_hash,
    user_id, target_type, target_id, is_temp, is_deleted
) VALUES
      (1901, 'java_cert_zhaoliu.pdf', 'cert/1901-java-cert.pdf', 'https://example.com/files/1901', 245760, 'application/pdf', 'pdf', 'md5_demo_1901',
       106, 2, NULL, 0, 0),
      (1902, 'student_card_sunqi.jpg', 'auth/1902-student-card.jpg', 'https://example.com/files/1902', 102400, 'image/jpeg', 'jpg', 'md5_demo_1902',
       107, 8, NULL, 0, 0),
      (1903, 'xuexin_report_chenchen.pdf', 'auth/1903-xuexin-report.pdf', 'https://example.com/files/1903', 198765, 'application/pdf', 'pdf', 'md5_demo_1903',
       112, 8, NULL, 0, 0),
      (1904, 'python_cert_shenyue.pdf', 'cert/1904-python-cert.pdf', 'https://example.com/files/1904', 286720, 'application/pdf', 'pdf', 'md5_demo_1904',
       116, 2, NULL, 0, 0);

INSERT INTO skill_certification (
    cert_id, user_id, skill_name, cert_name, cert_file_id, issue_date, issuer, cert_number, description,
    status, audit_status, audit_time, auditor_user_id, remark
) VALUES
      (1801, 106, 'Java', '软件设计师证书', 1901, '2025-11-20', '工业和信息化部', 'CERT-JAVA-106',
       '具备扎实 Java 工程开发能力。', 1, 1, '2026-04-02 09:30:00', 101, '审核通过'),
      (1802, 116, 'Python', '数据分析技能证书', 1904, '2025-09-15', '某在线教育平台', 'CERT-PY-116',
       '用于联调展示的测试证书数据。', 1, 0, NULL, NULL, NULL);

INSERT INTO auth_material (
    material_id, user_id, material_type, file_id, description, audit_status, audit_time, auditor_user_id, remark
) VALUES
      (2001, 107, 1, 1902, '学生证正面照片', 0, NULL, NULL, NULL),
      (2002, 112, 4, 1903, '学信网在读证明', 1, '2026-04-03 15:20:00', 101, '材料清晰，审核通过');

-- =====================================================
-- 项目与角色需求补充（仅追加）
-- 顺序：project -> project_role_requirements -> team_application
-- =====================================================

INSERT INTO project (
    project_id, name, belong_track, level, project_type,
    project_intro, project_progress, project_features, tags,
    allow_cross_major_application, publisher_user_id, is_anonymous, contact_info,
    deadline_recruit, view_count, favorite_count, apply_count,
    audit_status, audit_time, auditor_user_id, remark, status
) VALUES
      (201, '校园智能助手', '挑战杯', 2, '创新训练',
       '面向校内师生的智能问答与事务助手。',
       '已完成核心问答流程与用户中心接口，当前推进多轮对话和知识库更新策略。',
       '多角色协作、可扩展知识库、支持移动端轻量接入。',
       'AI,助手,校园,问答',
       1, 101, 0, NULL,
       '2026-05-20 23:59:59', 268, 42, 17,
       1, '2026-03-25 14:10:00', 101, '立项材料完整，允许进入招募阶段。', 2),

      (202, '智能排课规划器', '互联网+', 1, '创新训练',
       '用于课程安排与学习负载平衡的工具平台。',
       '课程冲突检测与推荐模块已上线测试，正在优化移动端交互。',
       '支持多维约束排课、学习压力可视化与个性化建议。',
       '教育,排课,Web,规划',
       1, 104, 0, NULL,
       '2026-05-10 23:59:59', 193, 33, 12,
       1, '2026-03-28 09:30:00', 101, '方向清晰，建议补充稳定性测试报告。', 2),

      (203, '多模态校园导览助手', '挑战杯', 2, '创新训练',
       '结合文本与图像能力，提供校内场景导览与问答。',
       '已完成图文检索原型，正在接入路线推荐与语音播报能力。',
       '图文混合检索、场景问答、路径推荐。',
       'AI,多模态,校园服务,导览',
       1, 106, 0, NULL,
       '2026-06-05 23:59:59', 221, 28, 15,
       1, '2026-04-01 11:20:00', 101, '通过评审，建议加强端侧体验。', 2),

      (204, '智能自习室排座系统', '大创', 1, '创新训练',
       '支持预约、签到、违约记录与热度分析。',
       '预约与签到链路已联调完成，正在灰度测试违约策略。',
       '预约规则可配置、热度看板、违约治理闭环。',
       '预约系统,小程序,数据分析,校园',
       1, 107, 0, NULL,
       '2026-05-25 23:59:59', 156, 21, 9,
       1, '2026-04-02 10:00:00', 101, '进展稳定，可继续实施。', 1),

      (205, '高校二手书流通平台', '互联网+', 1, '创业实践',
       '面向校内同学的二手教材交易与捐赠平台。',
       '已完成需求调研与原型设计，交易担保流程待明确。',
       '校内身份校验、交易与捐赠双模式、可追溯记录。',
       '电商,交易,校园,二手书',
       1, 108, 0, NULL,
       '2026-05-30 23:59:59', 132, 17, 8,
       0, NULL, NULL, '处于待审核阶段，请补充风险控制说明。', 2),

      (206, '低碳出行打卡小程序', '互联网+', 2, '创新训练',
       '记录步行与骑行行为，形成绿色积分体系。',
       '积分与排行榜功能已上线，正在对接校园活动激励模块。',
       '低碳行为识别、积分体系、活动联动。',
       '低碳,打卡,积分,小程序',
       1, 109, 0, NULL,
       '2026-04-30 23:59:59', 247, 36, 14,
       1, '2026-03-29 16:40:00', 101, '已通过评审，进入成果打磨阶段。', 3),

      (207, '宿舍能耗可视化看板', '大创', 2, '创新训练',
       '接入宿舍能耗数据，形成可视化分析看板。',
       '采集端部署完成，已形成阶段性分析报告并结题归档。',
       '实时采集、异常预警、趋势分析。',
       'IoT,可视化,能耗,看板',
       0, 110, 0, NULL,
       '2026-03-31 23:59:59', 302, 49, 18,
       1, '2026-03-20 15:00:00', 101, '项目已完成，建议准备复盘材料。', 4),

      (208, '竞赛资料知识图谱', '挑战杯', 3, '创新训练',
       '沉淀历届竞赛资料，构建可检索知识图谱。',
       '实体抽取效果不达标，当前处于方案重构阶段。',
       '知识抽取、语义检索、可视化关联分析。',
       '知识图谱,NLP,检索,竞赛',
       1, 111, 0, NULL,
       '2026-05-28 23:59:59', 118, 11, 6,
       2, '2026-04-04 10:50:00', 101, '驳回：技术路线与里程碑不够清晰。', 2),

      (209, '课堂互动问答系统', '互联网+', 1, '创新训练',
       '课堂实时问答与匿名提问，提高课堂参与度。',
       '核心问答功能已上线内测，正在补齐课堂统计报表。',
       '实时互动、匿名提问、课堂数据分析。',
       '教学,互动,问答,课堂',
       1, 112, 0, NULL,
       '2026-05-18 23:59:59', 176, 24, 10,
       1, '2026-04-03 14:30:00', 101, '方向明确，建议加强高并发场景验证。', 2),

      (210, '校园失物招领平台', '大创', 1, '创新训练',
       '支持物品发布、认领匹配与流程留痕。',
       '发布与认领闭环已联调完成，审核流与消息通知迭代中。',
       '认领匹配、流程留痕、消息提醒。',
       '平台,校园服务,匹配,失物招领',
       1, 113, 0, NULL,
       '2026-05-22 23:59:59', 208, 31, 13,
       1, '2026-04-05 09:45:00', 101, '通过评审，建议完善违规内容拦截策略。', 2),

      (211, '新生社团匹配推荐', '挑战杯', 2, '创新训练',
       '根据兴趣标签和时间偏好推荐合适社团。',
       '完成用户画像设计，推荐策略仍在AB测试中。',
       '兴趣标签建模、冷启动策略、推荐可解释。',
       '推荐系统,标签,社团,新生',
       1, 114, 0, NULL,
       '2026-06-01 23:59:59', 97, 12, 5,
       0, NULL, NULL, '待审核：请补充样本来源与评估指标。', 0)
    ON DUPLICATE KEY UPDATE
                         name = VALUES(name),
                         belong_track = VALUES(belong_track),
                         level = VALUES(level),
                         project_type = VALUES(project_type),
                         project_intro = VALUES(project_intro),
                         project_progress = VALUES(project_progress),
                         project_features = VALUES(project_features),
                         tags = VALUES(tags),
                         allow_cross_major_application = VALUES(allow_cross_major_application),
                         publisher_user_id = VALUES(publisher_user_id),
                         is_anonymous = VALUES(is_anonymous),
                         contact_info = VALUES(contact_info),
                         deadline_recruit = VALUES(deadline_recruit),
                         view_count = VALUES(view_count),
                         favorite_count = VALUES(favorite_count),
                         apply_count = VALUES(apply_count),
                         audit_status = VALUES(audit_status),
                         audit_time = VALUES(audit_time),
                         auditor_user_id = VALUES(auditor_user_id),
                         remark = VALUES(remark),
                         status = VALUES(status);

INSERT INTO project_role_requirements (
    requirement_id, project_id, role, member_quota, current_applicants, current_members, recruit_requirements
) VALUES
      (3001, 201, '后端开发', 2, 5, 1, '熟悉 Java/Spring Boot，能独立完成接口与数据库设计。'),
      (3002, 201, '算法工程', 1, 3, 1, '具备基础 NLP 或排序算法实践经验。'),
      (3003, 201, 'UI设计', 1, 2, 0, '能够输出移动端高保真稿与组件规范。'),

      (3004, 202, '前端开发', 2, 4, 1, '熟悉 Vue 技术栈，具备页面性能优化意识。'),
      (3005, 202, '产品经理', 1, 2, 0, '能完成需求拆解、原型与评审文档。'),

      (3006, 203, '后端开发', 2, 4, 1, '负责多模态检索服务接口与数据管理。'),
      (3007, 203, '前端开发', 1, 3, 0, '负责导览页面交互与接口联调。'),
      (3008, 203, '测试开发', 1, 2, 0, '编写接口测试与回归脚本。'),

      (3009, 204, '后端开发', 1, 2, 1, '负责预约规则与签到逻辑实现。'),
      (3010, 204, 'UI设计', 1, 2, 0, '输出小程序页面视觉与交互规范。'),

      (3011, 205, '后端开发', 2, 3, 0, '实现交易流程、订单状态与权限控制。'),
      (3012, 205, '前端开发', 1, 2, 0, '完成交易页面和发布页面联调。'),

      (3013, 206, '小程序开发', 2, 3, 1, '负责打卡、积分、排行榜页面与接口。'),
      (3014, 206, '数据分析', 1, 2, 1, '负责低碳行为统计模型与效果分析。'),

      (3015, 207, '嵌入式开发', 1, 2, 1, '负责采集端设备接入与协议对接。'),
      (3016, 207, '数据可视化', 1, 2, 1, '负责看板展示与异常趋势分析页面。'),

      (3017, 208, '算法工程', 1, 2, 0, '负责实体抽取与关系建模方案落地。'),
      (3018, 208, '后端开发', 1, 2, 0, '负责知识检索接口与索引服务。'),

      (3019, 209, '后端开发', 1, 2, 0, '负责课堂问答实时消息与存储。'),
      (3020, 209, '算法工程', 1, 3, 0, '负责问答匹配与推荐策略优化。'),
      (3021, 209, '产品经理', 1, 1, 0, '负责课堂场景需求梳理与指标设计。'),

      (3022, 210, '后端开发', 1, 2, 0, '负责失物发布与认领流程接口。'),
      (3023, 210, '前端开发', 1, 2, 0, '负责发布、认领、消息页面实现。'),
      (3024, 210, '测试工程', 1, 2, 0, '负责流程回归与异常路径测试。'),

      (3025, 211, '算法工程', 1, 2, 0, '负责社团推荐策略与冷启动优化。'),
      (3026, 211, '产品经理', 1, 2, 0, '负责用户画像与推荐解释方案设计。')
    ON DUPLICATE KEY UPDATE
                         member_quota = VALUES(member_quota),
                         current_applicants = VALUES(current_applicants),
                         current_members = VALUES(current_members),
                         recruit_requirements = VALUES(recruit_requirements);

INSERT INTO team_application (
    application_id, applicant_user_id, project_id, requirement_id, role,
    apply_reason, custom_resume_file_id, application_attachment_file_id,
    result, audit_time, auditor_user_id, remark, apply_time, update_time
) VALUES
      (1001, 107, 203, 3007, '前端开发',
       '希望参与完整联调流程，负责页面与接口对接。', NULL, NULL,
       0, NULL, NULL, '已补充岗位关联，等待处理。', '2026-04-09 18:00:00', '2026-04-11 11:20:00'),
      (1002, 112, 210, 3024, '测试工程',
       '可负责接口测试和回归测试用例整理。', NULL, NULL,
       1, '2026-04-08 20:10:00', 113, '沟通清晰，匹配岗位。', '2026-04-08 19:20:00', '2026-04-11 11:20:00'),
      (1003, 118, 209, 3020, '算法工程',
       '希望参与推荐策略与召回模块开发。', NULL, NULL,
       2, '2026-04-09 11:10:00', 112, '当前岗位需求以后端为主。', '2026-04-09 10:20:00', '2026-04-11 11:20:00')
    ON DUPLICATE KEY UPDATE
                         requirement_id = VALUES(requirement_id),
                         role = VALUES(role),
                         apply_reason = VALUES(apply_reason),
                         result = VALUES(result),
                         audit_time = VALUES(audit_time),
                         auditor_user_id = VALUES(auditor_user_id),
                         remark = VALUES(remark),
                         update_time = VALUES(update_time);

-- =====================================================
-- 数据一致性修正（仅追加）
-- 1) project.apply_count 与 team_application 数量一致
-- 2) project_role_requirements.current_members 与 team_member 匹配
-- 3) 为每个项目补充 contact_info
-- 顺序：project -> project_role_requirements -> team_application
-- =====================================================

INSERT INTO project (
    project_id, name, publisher_user_id, contact_info, apply_count
) VALUES
      (201, '校园智能助手', 101, '联系邮箱：ai-assistant201@teammatch.edu.cn', 0),
      (202, '智能排课规划器', 104, '联系微信：tm_plan202', 0),
      (203, '多模态校园导览助手', 106, '联系邮箱：guide203@teammatch.edu.cn', 1),
      (204, '智能自习室排座系统', 107, '联系微信：tm_study204', 0),
      (205, '高校二手书流通平台', 108, '联系邮箱：book205@teammatch.edu.cn', 0),
      (206, '低碳出行打卡小程序', 109, '联系微信：tm_green206', 0),
      (207, '宿舍能耗可视化看板', 110, '联系邮箱：energy207@teammatch.edu.cn', 0),
      (208, '竞赛资料知识图谱', 111, '联系微信：tm_graph208', 0),
      (209, '课堂互动问答系统', 112, '联系邮箱：qa209@teammatch.edu.cn', 1),
      (210, '校园失物招领平台', 113, '联系微信：tm_lost210', 1),
      (211, '新生社团匹配推荐', 114, '联系邮箱：club211@teammatch.edu.cn', 0)
    ON DUPLICATE KEY UPDATE
                         contact_info = VALUES(contact_info),
                         apply_count = VALUES(apply_count);

INSERT INTO project_role_requirements (
    requirement_id, project_id, role, member_quota, current_applicants, current_members, recruit_requirements
) VALUES
      (3001, 201, '后端开发', 2, 0, 0, '熟悉 Java/Spring Boot，能独立完成接口与数据库设计。'),
      (3002, 201, '算法工程', 1, 0, 0, '具备基础 NLP 或排序算法实践经验。'),
      (3003, 201, 'UI设计', 1, 0, 0, '能够输出移动端高保真稿与组件规范。'),

      (3004, 202, '前端开发', 2, 0, 0, '熟悉 Vue 技术栈，具备页面性能优化意识。'),
      (3005, 202, '产品经理', 1, 0, 0, '能完成需求拆解、原型与评审文档。'),

      (3006, 203, '后端开发', 2, 0, 1, '负责多模态检索服务接口与数据管理。'),
      (3007, 203, '前端开发', 1, 1, 0, '负责导览页面交互与接口联调。'),
      (3008, 203, '测试开发', 1, 0, 0, '编写接口测试与回归脚本。'),

      (3009, 204, '后端开发', 1, 0, 0, '负责预约规则与签到逻辑实现。'),
      (3010, 204, 'UI设计', 1, 0, 0, '输出小程序页面视觉与交互规范。'),

      (3011, 205, '后端开发', 2, 0, 0, '实现交易流程、订单状态与权限控制。'),
      (3012, 205, '前端开发', 1, 0, 0, '完成交易页面和发布页面联调。'),

      (3013, 206, '小程序开发', 2, 0, 0, '负责打卡、积分、排行榜页面与接口。'),
      (3014, 206, '数据分析', 1, 0, 0, '负责低碳行为统计模型与效果分析。'),

      (3015, 207, '嵌入式开发', 1, 0, 0, '负责采集端设备接入与协议对接。'),
      (3016, 207, '数据可视化', 1, 0, 0, '负责看板展示与异常趋势分析页面。'),

      (3017, 208, '算法工程', 1, 0, 0, '负责实体抽取与关系建模方案落地。'),
      (3018, 208, '后端开发', 1, 0, 0, '负责知识检索接口与索引服务。'),

      (3019, 209, '后端开发', 1, 0, 0, '负责课堂问答实时消息与存储。'),
      (3020, 209, '算法工程', 1, 1, 0, '负责问答匹配与推荐策略优化。'),
      (3021, 209, '产品经理', 1, 0, 0, '负责课堂场景需求梳理与指标设计。'),

      (3022, 210, '后端开发', 1, 0, 0, '负责失物发布与认领流程接口。'),
      (3023, 210, '前端开发', 1, 0, 0, '负责发布、认领、消息页面实现。'),
      (3024, 210, '测试工程', 1, 1, 0, '负责流程回归与异常路径测试。'),

      (3025, 211, '算法工程', 1, 0, 0, '负责社团推荐策略与冷启动优化。'),
      (3026, 211, '产品经理', 1, 0, 0, '负责用户画像与推荐解释方案设计。'),

      (3027, 203, '队长', 1, 0, 1, '负责项目推进、技术方案拍板与对外沟通。'),
      (3028, 210, '产品负责人', 1, 0, 1, '负责需求优先级与迭代节奏管理。')
    ON DUPLICATE KEY UPDATE
                         member_quota = VALUES(member_quota),
                         current_applicants = VALUES(current_applicants),
                         current_members = VALUES(current_members),
                         recruit_requirements = VALUES(recruit_requirements);

INSERT INTO team_application (
    application_id, applicant_user_id, project_id, requirement_id, role,
    apply_reason, custom_resume_file_id, application_attachment_file_id,
    result, audit_time, auditor_user_id, remark, apply_time, update_time
) VALUES
      (1001, 107, 203, 3007, '前端开发',
       '希望参与完整联调流程，负责页面与接口对接。', NULL, NULL,
       0, NULL, NULL, '岗位关联已校准。', '2026-04-09 18:00:00', '2026-04-11 12:30:00'),
      (1002, 112, 210, 3024, '测试工程',
       '可负责接口测试和回归测试用例整理。', NULL, NULL,
       1, '2026-04-08 20:10:00', 113, '沟通清晰，匹配岗位。', '2026-04-08 19:20:00', '2026-04-11 12:30:00'),
      (1003, 118, 209, 3020, '算法工程',
       '希望参与推荐策略与召回模块开发。', NULL, NULL,
       2, '2026-04-09 11:10:00', 112, '当前岗位需求以后端为主。', '2026-04-09 10:20:00', '2026-04-11 12:30:00')
    ON DUPLICATE KEY UPDATE
                         requirement_id = VALUES(requirement_id),
                         role = VALUES(role),
                         apply_reason = VALUES(apply_reason),
                         result = VALUES(result),
                         audit_time = VALUES(audit_time),
                         auditor_user_id = VALUES(auditor_user_id),
                         remark = VALUES(remark),
                         update_time = VALUES(update_time);

-- =====================================================
-- project -> project_role_requirements -> team_application
-- 追加到 test_data.sql 末尾（幂等修正）
-- =====================================================

INSERT INTO project (
    project_id, name, publisher_user_id, contact_info, apply_count
) VALUES
      (201, '校园智能助手', 101, '联系邮箱：ai-assistant201@teammatch.edu.cn', 0),
      (202, '智能排课规划器', 104, '联系微信：tm_plan202', 0),
      (203, '多模态校园导览助手', 106, '联系邮箱：guide203@teammatch.edu.cn', 1),
      (204, '智能自习室排座系统', 107, '联系微信：tm_study204', 0),
      (205, '高校二手书流通平台', 108, '联系邮箱：book205@teammatch.edu.cn', 0),
      (206, '低碳出行打卡小程序', 109, '联系微信：tm_green206', 0),
      (207, '宿舍能耗可视化看板', 110, '联系邮箱：energy207@teammatch.edu.cn', 0),
      (208, '竞赛资料知识图谱', 111, '联系微信：tm_graph208', 0),
      (209, '课堂互动问答系统', 112, '联系邮箱：qa209@teammatch.edu.cn', 1),
      (210, '校园失物招领平台', 113, '联系微信：tm_lost210', 1),
      (211, '新生社团匹配推荐', 114, '联系邮箱：club211@teammatch.edu.cn', 0)
    ON DUPLICATE KEY UPDATE
                         contact_info = VALUES(contact_info),
                         apply_count = VALUES(apply_count);

INSERT INTO project_role_requirements (
    requirement_id, project_id, role, member_quota, current_applicants, current_members, recruit_requirements
) VALUES
      (3001, 201, '后端开发', 2, 0, 0, '熟悉 Java/Spring Boot，能独立完成接口与数据库设计。'),
      (3002, 201, '算法工程', 1, 0, 0, '具备基础 NLP 或排序算法实践经验。'),
      (3003, 201, 'UI设计', 1, 0, 0, '能够输出移动端高保真稿与组件规范。'),

      (3004, 202, '前端开发', 2, 0, 0, '熟悉 Vue 技术栈，具备页面性能优化意识。'),
      (3005, 202, '产品经理', 1, 0, 0, '能完成需求拆解、原型与评审文档。'),

      (3006, 203, '后端开发', 2, 0, 1, '负责多模态检索服务接口与数据管理。'),
      (3007, 203, '前端开发', 1, 1, 0, '负责导览页面交互与接口联调。'),
      (3008, 203, '测试开发', 1, 0, 0, '编写接口测试与回归脚本。'),
      (3027, 203, '队长', 1, 0, 1, '负责项目推进、技术方案拍板与对外沟通。'),

      (3009, 204, '后端开发', 1, 0, 0, '负责预约规则与签到逻辑实现。'),
      (3010, 204, 'UI设计', 1, 0, 0, '输出小程序页面视觉与交互规范。'),

      (3011, 205, '后端开发', 2, 0, 0, '实现交易流程、订单状态与权限控制。'),
      (3012, 205, '前端开发', 1, 0, 0, '完成交易页面和发布页面联调。'),

      (3013, 206, '小程序开发', 2, 0, 0, '负责打卡、积分、排行榜页面与接口。'),
      (3014, 206, '数据分析', 1, 0, 0, '负责低碳行为统计模型与效果分析。'),

      (3015, 207, '嵌入式开发', 1, 0, 0, '负责采集端设备接入与协议对接。'),
      (3016, 207, '数据可视化', 1, 0, 0, '负责看板展示与异常趋势分析页面。'),

      (3017, 208, '算法工程', 1, 0, 0, '负责实体抽取与关系建模方案落地。'),
      (3018, 208, '后端开发', 1, 0, 0, '负责知识检索接口与索引服务。'),

      (3019, 209, '后端开发', 1, 0, 0, '负责课堂问答实时消息与存储。'),
      (3020, 209, '算法工程', 1, 1, 0, '负责问答匹配与推荐策略优化。'),
      (3021, 209, '产品经理', 1, 0, 0, '负责课堂场景需求梳理与指标设计。'),

      (3022, 210, '后端开发', 1, 0, 0, '负责失物发布与认领流程接口。'),
      (3023, 210, '前端开发', 1, 0, 0, '负责发布、认领、消息页面实现。'),
      (3024, 210, '测试工程', 1, 1, 0, '负责流程回归与异常路径测试。'),
      (3028, 210, '产品负责人', 1, 0, 1, '负责需求优先级与迭代节奏管理。'),

      (3025, 211, '算法工程', 1, 0, 0, '负责社团推荐策略与冷启动优化。'),
      (3026, 211, '产品经理', 1, 0, 0, '负责用户画像与推荐解释方案设计。')
    ON DUPLICATE KEY UPDATE
                         member_quota = VALUES(member_quota),
                         current_applicants = VALUES(current_applicants),
                         current_members = VALUES(current_members),
                         recruit_requirements = VALUES(recruit_requirements);

INSERT INTO team_application (
    application_id, applicant_user_id, project_id, requirement_id, role,
    apply_reason, custom_resume_file_id, application_attachment_file_id,
    result, audit_time, auditor_user_id, remark, apply_time, update_time
) VALUES
      (1001, 107, 203, 3007, '前端开发',
       '希望参与完整联调流程，负责页面与接口对接。', NULL, NULL,
       0, NULL, NULL, '岗位关联已校准。', '2026-04-09 18:00:00', '2026-04-13 10:30:00'),
      (1002, 112, 210, 3024, '测试工程',
       '可负责接口测试和回归测试用例整理。', NULL, NULL,
       1, '2026-04-08 20:10:00', 113, '沟通清晰，匹配岗位。', '2026-04-08 19:20:00', '2026-04-13 10:30:00'),
      (1003, 118, 209, 3020, '算法工程',
       '希望参与推荐策略与召回模块开发。', NULL, NULL,
       2, '2026-04-09 11:10:00', 112, '当前岗位需求以后端为主。', '2026-04-09 10:20:00', '2026-04-13 10:30:00')
    ON DUPLICATE KEY UPDATE
                         requirement_id = VALUES(requirement_id),
                         role = VALUES(role),
                         apply_reason = VALUES(apply_reason),
                         result = VALUES(result),
                         audit_time = VALUES(audit_time),
                         auditor_user_id = VALUES(auditor_user_id),
                         remark = VALUES(remark),
                         update_time = VALUES(update_time);