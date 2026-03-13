package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.ChatSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话表操作 Mapper
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
    // 会话表操作方法
}

