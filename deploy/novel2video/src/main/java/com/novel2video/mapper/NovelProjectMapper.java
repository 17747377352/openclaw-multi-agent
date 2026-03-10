package com.novel2video.mapper;

import com.novel2video.entity.NovelProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小说项目 Mapper
 * 
 * @author developer
 * @since 2026-03-10
 */
@Mapper
public interface NovelProjectMapper {
    
    /**
     * 插入项目
     */
    int insert(NovelProject project);
    
    /**
     * 根据 ID 查询
     */
    NovelProject selectById(@Param("id") Long id);
    
    /**
     * 根据用户 ID 查询列表
     */
    List<NovelProject> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 更新项目
     */
    int update(NovelProject project);
    
    /**
     * 删除项目
     */
    int deleteById(@Param("id") Long id);
}
