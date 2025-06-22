package cn.cotenite.agentxkotlin.application.agent.service

import cn.cotenite.agentxkotlin.application.conversation.assmebler.SessionAssembler
import cn.cotenite.agentxkotlin.application.conversation.dto.SessionDTO
import cn.cotenite.agentxkotlin.domain.agent.service.AgentDomainService
import cn.cotenite.agentxkotlin.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.agentxkotlin.domain.conversation.constant.Role
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import cn.cotenite.agentxkotlin.domain.conversation.model.SessionEntity
import cn.cotenite.agentxkotlin.domain.conversation.service.ConversationDomainService
import cn.cotenite.agentxkotlin.domain.conversation.service.SessionDomainService
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.interfaces.dto.conversation.ConversationRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @Author  RichardYoung
 * @Description  
 * @Date  2025/6/22 17:26
*/
@Service
class AgentSessionAppService(
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
    private val agentServiceDomainService: AgentDomainService,
    private val sessionDomainService: SessionDomainService,
    private val conversationDomainService: ConversationDomainService
){

    /**
     * 获取助理下的会话列表
     *
     * @param userId  用户id
     * @param agentId 助理id
     * @return 会话列表
     */
    fun getAgentSessionList(userId: String?, agentId: String?): List<SessionDTO> {
        // 校验该 agent 是否被添加了工作区，判断条件：是否是自己的助理 or 在工作区中

        val b = agentServiceDomainService.exist(agentId!!, userId!!)
        val b1 = agentWorkspaceDomainService.exist(agentId, userId)

        if (!b && !b1) {
            throw BusinessException("助理不存在")
        }

        // 获取对应的会话列表
        val sessions= sessionDomainService.getSessionsByAgentId(agentId).toMutableList()
        if (sessions.isEmpty()) {
            // 如果会话列表为空，则新创建一个并且返回
            val session= sessionDomainService.createSession(agentId, userId)
            sessions.add(session)
        }
        return SessionAssembler.toDTOs(sessions)
    }

    /**
     * 创建会话
     *
     * @param userId  用户id
     * @param agentId 助理id
     * @return 会话
     */
    fun createSession(userId: String?, agentId: String?): SessionDTO? {
        val session: SessionEntity = sessionDomainService.createSession(agentId!!, userId!!)
        val agent= agentServiceDomainService.getAgentWithPermissionCheck(agentId, userId)
        val welcomeMessage = agent.welcomeMessage
        val messageEntity= MessageEntity()
        messageEntity.role=Role.SYSTEM
        messageEntity.content=welcomeMessage
        messageEntity.sessionId=session.id
        conversationDomainService.saveMessage(messageEntity)
        return SessionAssembler.toDTO(session)
    }


    /**
     * 更新会话
     *
     * @param id     会话id
     * @param userId 用户id
     * @param title  标题
     */
    fun updateSession(id: String?, userId: String?, title: String?) {
        sessionDomainService.updateSession(id!!, userId!!, title!!)
    }

    /**
     * 删除会话
     *
     * @param id 会话id
     */
    @Transactional
    fun deleteSession(id: String?, userId: String?) {
        sessionDomainService.deleteSession(id!!, userId!!)

        // 删除会话下的消息
        conversationDomainService.deleteConversationMessages(id)
    }

    /**
     * 发送消息
     *
     * @param id 会话id
     * @param userId 用户id
     * @param conversationRequest 会话请求
     */
    fun sendMessage(id: String?, userId: String?, conversationRequest: ConversationRequest?) {

    }

}