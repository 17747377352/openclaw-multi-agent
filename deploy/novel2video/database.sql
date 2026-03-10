-- =====================================================
-- 小说转短视频平台 - 数据库表结构
-- 数据库：ai-translation
-- 创建时间：2026-03-10
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 表：novel_project (小说项目表)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `novel_project`;
CREATE TABLE `novel_project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '项目 ID',
  `title` varchar(255) NOT NULL COMMENT '小说标题',
  `author` varchar(100) DEFAULT NULL COMMENT '作者',
  `original_file_path` varchar(500) DEFAULT NULL COMMENT '原始文件 OSS 路径',
  `total_chapters` int(11) DEFAULT 0 COMMENT '总章节数',
  `total_words` bigint(20) DEFAULT 0 COMMENT '总字数',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-处理中 2-人物审核中 3-分镜审核中 4-视频生成中 5-已完成 9-已废弃',
  `user_id` bigint(20) DEFAULT NULL COMMENT '创建用户 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小说项目表';

-- -----------------------------------------------------
-- 表：chapter (章节表)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `chapter`;
CREATE TABLE `chapter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '章节 ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目 ID',
  `chapter_number` int(11) NOT NULL COMMENT '章节序号',
  `title` varchar(255) NOT NULL COMMENT '章节标题',
  `content` text COMMENT '章节内容',
  `word_count` int(11) DEFAULT 0 COMMENT '字数',
  `group_id` bigint(20) DEFAULT NULL COMMENT '所属分组 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节表';

-- -----------------------------------------------------
-- 表：chapter_group (章节分组表)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `chapter_group`;
CREATE TABLE `chapter_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分组 ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目 ID',
  `group_number` int(11) NOT NULL COMMENT '分组序号',
  `name` varchar(255) DEFAULT NULL COMMENT '分组名称',
  `chapter_ids` varchar(500) DEFAULT NULL COMMENT '章节 ID 列表，逗号分隔',
  `start_chapter` int(11) NOT NULL COMMENT '起始章节号',
  `end_chapter` int(11) NOT NULL COMMENT '结束章节号',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待处理 1-人物审核中 2-分镜审核中 3-视频生成中 4-已完成',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节分组表（5-8 章/组）';

-- -----------------------------------------------------
-- 表：character (人物表)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `character`;
CREATE TABLE `character` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '人物 ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目 ID',
  `name` varchar(100) NOT NULL COMMENT '人物名称',
  `description` text COMMENT 'AI 提取的人物描述',
  `user_edited_description` text COMMENT '用户编辑后的人物描述',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别：0-未知 1-男 2-女',
  `role` varchar(50) DEFAULT NULL COMMENT '角色类型：主角/配角/反派等',
  `seed_image_url` varchar(500) DEFAULT NULL COMMENT '人物标准照 OSS 路径',
  `seed_prompt` text COMMENT '生图提示词',
  `seed_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '生图状态：0-待生成 1-生成中 2-已完成 3-失败',
  `is_confirmed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已确认：0-否 1-是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_confirmed` (`is_confirmed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人物表（含人物标准照）';

-- -----------------------------------------------------
-- 表：storyboard (分镜表)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `storyboard`;
CREATE TABLE `storyboard` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分镜 ID',
  `group_id` bigint(20) NOT NULL COMMENT '分组 ID',
  `scene_number` int(11) NOT NULL COMMENT '分镜序号',
  `description` text COMMENT '分镜描述',
  `prompt` text COMMENT 'AI 生成的提示词（含人物链接）',
  `character_ids` varchar(500) DEFAULT NULL COMMENT '涉及人物 ID 列表，逗号分隔',
  `frame_image_url` varchar(500) DEFAULT NULL COMMENT '首帧图 OSS 路径',
  `frame_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '首帧图状态：0-待生成 1-生成中 2-已完成 3-失败',
  `is_confirmed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已确认：0-否 1-是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_confirmed` (`is_confirmed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分镜表（含首帧图）';

-- -----------------------------------------------------
-- 表：video_task (视频任务表)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `video_task`;
CREATE TABLE `video_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务 ID',
  `group_id` bigint(20) NOT NULL COMMENT '分组 ID',
  `storyboard_id` bigint(20) NOT NULL COMMENT '分镜 ID',
  `task_id` varchar(100) DEFAULT NULL COMMENT '火山引擎任务 ID',
  `video_url` varchar(500) DEFAULT NULL COMMENT '生成完成的视频 OSS 路径',
  `video_duration` int(11) DEFAULT NULL COMMENT '视频时长（秒）',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待生成 1-生成中 2-已完成 3-失败',
  `fail_reason` text COMMENT '失败原因',
  `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
  `progress` int(11) NOT NULL DEFAULT 0 COMMENT '生成进度百分比',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_storyboard_id` (`storyboard_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频任务表';

-- -----------------------------------------------------
-- 表：sys_config (系统配置表)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置 ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `description` varchar(255) DEFAULT NULL COMMENT '配置说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- -----------------------------------------------------
-- 初始化配置数据
-- -----------------------------------------------------
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES
('novel_chapters_per_group', '5', '每组合并章节数'),
('character_image_width', '1024', '人物图宽度'),
('character_image_height', '1024', '人物图高度'),
('video_duration_seconds', '10', '视频时长（秒）'),
('video_max_retry', '3', '视频生成最大重试次数'),
('ark_base_url', 'https://ark.cn-beijing.volces.com/api/v3', '火山 Ark API 地址'),
('ark_image_model', 'doubao-seedream-4-5-251128', '火山生图模型'),
('ark_video_model', 'doubao-seedance-1-5-pro-251215', '火山视频生成模型'),
('kimi_base_url', 'https://api.moonshot.cn/v1', 'Kimi API 地址'),
('kimi_model', 'kimi-latest', 'Kimi 模型');

SET FOREIGN_KEY_CHECKS = 1;
