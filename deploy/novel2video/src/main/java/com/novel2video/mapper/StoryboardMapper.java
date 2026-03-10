package com.novel2video.mapper;

import com.novel2video.entity.Storyboard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分镜 Mapper
 * 
 * @author developer
 * @since 2026-03-10
 */
@Mapper
public interface StoryboardMapper {
    
    /**
     * 批量插入分镜
     */
    int batchInsert(@Param("list") List<Storyboard> storyboards);
    
    /**
     * 根据分组 ID 查询
     */
    List<Storyboard> selectByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 根据 ID 查询
     */
    Storyboard selectById(@Param("id") Long id);
    
    /**
     * 更新分镜
     */
    int update(Storyboard storyboard);
}
