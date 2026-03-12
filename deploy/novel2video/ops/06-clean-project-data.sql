-- 小说转短视频项目 - 数据清理 SQL
-- 说明：默认清空业务数据，并重置自增 ID；保留 sys_config 配置表
-- 执行前请确认当前库为 ai-translation

USE `ai-translation`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- 业务数据清理：按依赖顺序删除
DELETE FROM `video_task`;
DELETE FROM `storyboard`;
DELETE FROM `character`;
DELETE FROM `chapter_group`;
DELETE FROM `chapter`;
DELETE FROM `novel_project`;

-- 重置自增
ALTER TABLE `video_task` AUTO_INCREMENT = 1;
ALTER TABLE `storyboard` AUTO_INCREMENT = 1;
ALTER TABLE `character` AUTO_INCREMENT = 1;
ALTER TABLE `chapter_group` AUTO_INCREMENT = 1;
ALTER TABLE `chapter` AUTO_INCREMENT = 1;
ALTER TABLE `novel_project` AUTO_INCREMENT = 1;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- 如果你连 sys_config 也想一起清掉，手动取消下面这段注释后单独执行：
-- START TRANSACTION;
-- DELETE FROM `sys_config`;
-- ALTER TABLE `sys_config` AUTO_INCREMENT = 1;
-- COMMIT;
