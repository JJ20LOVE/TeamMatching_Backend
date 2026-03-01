package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息表操作Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    // 消息表操作方法
}