package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论表操作Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    // 评论表操作方法
}