package com.novel2video.mapper;

import com.novel2video.entity.NovelCharacter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人物 Mapper
 * 
 * @author developer
 * @since 2026-03-10
 */
@Mapper
public interface CharacterMapper {
    
    /**
     * 插入人物
     */
    int insert(NovelNovelCharacter character);
    
    /**
     * 根据项目 ID 查询
     */
    List<Character> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 根据 ID 查询
     */
    Character selectById(@Param("id") Long id);
    
    /**
     * 更新人物
     */
    int update(NovelNovelCharacter character);
    
    /**
     * 批量更新人物确认状态
     */
    int batchUpdateConfirmStatus(@Param("ids") List<Long> ids, @Param("isConfirmed") Integer isConfirmed);
}
