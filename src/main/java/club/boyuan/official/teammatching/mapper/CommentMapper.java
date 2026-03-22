package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 评论表操作Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 增加帖子评论计数（数据库层面自增，避免并发问题）
     * @param postId 帖子ID
     * @return 影响的行数
     */
    @Update("UPDATE community_post SET comment_count = COALESCE(comment_count, 0) + 1 WHERE post_id = #{postId}")
    int incrementCommentCount(Integer postId);
}