package com.novel2video.mapper;

import com.novel2video.entity.Chapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 章节 Mapper
 * 
 * @author developer
 * @since 2026-03-10
 */
@Mapper
public interface ChapterMapper {
    
    /**
     * 批量插入章节
     */
    int batchInsert(@Param("list") List<Chapter> chapters);
    
    /**
     * 根据项目 ID 查询
     */
    List<Chapter> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 根据分组 ID 查询
     */
    List<Chapter> selectByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 更新章节分组
     */
    int updateGroupId(@Param("chapterId") Long chapterId, @Param("groupId") Long groupId);
    
    /**
     * 根据项目 ID 删除章节
     */
    int deleteByProjectId(@Param("projectId") Long projectId);
}
