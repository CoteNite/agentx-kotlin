package cn.cotenite.domain.agent.model

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import org.apache.ibatis.type.JdbcType
import cn.cotenite.infrastructure.converter.LLMModelConfigConverter
import cn.cotenite.infrastructure.entity.BaseEntity

/**
 * Agent工作区实体
 */
@TableName(value = "agent_workspace", autoResultMap = true)
class AgentWorkspaceEntity() : BaseEntity() {
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    var id: String? = null
    @TableField("agent_id")
    var agentId: String? = null
    @TableField("user_id")
    var userId: String? = null

    @TableField(value = "llm_model_config", typeHandler = LLMModelConfigConverter::class, jdbcType = JdbcType.OTHER)
    var llmModelConfig: LLMModelConfig = LLMModelConfig()

    constructor(agentId: String?, userId: String?, llmModelConfig: LLMModelConfig?) : this() {
        this.agentId = agentId
        this.userId = userId
        this.llmModelConfig = llmModelConfig ?: LLMModelConfig()
    }
}
