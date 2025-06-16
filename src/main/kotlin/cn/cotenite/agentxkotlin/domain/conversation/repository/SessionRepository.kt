package cn.cotenite.agentxkotlin.domain.conversation.repository

import cn.cotenite.agentxkotlin.domain.conversation.model.Session
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import org.apache.ibatis.annotations.Mapper

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:30
 */
@Mapper
interface SessionRepository: BaseMapper<Session> {
}
