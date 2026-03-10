package com.novel2video.mapper;

import com.novel2video.entity.ChapterGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 章节分组 Mapper
 * 
 * @author developer
 * @since 2026-03-10
 */
@Mapper
public interface ChapterGroupMapper {
    
    /**
     * 插入分组
     */
    int insert(ChapterGroup group);
    
    /**
     * 根据项目 ID 查询
     */
    List<ChapterGroup> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 根据 ID 查询
     */
    ChapterGroup selectById(@Param("id") Long id);
    
    /**
     * 更新分组
     */
    int update(ChapterGroup group);
}
