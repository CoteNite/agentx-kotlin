package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.MappedTypes
import cn.cotenite.domain.agent.model.LLMModelConfig

/**
 * LLMModelConfig JSON转换器
 */
@MappedTypes(LLMModelConfig::class)
class LLMModelConfigConverter : JsonToStringConverter<LLMModelConfig>(LLMModelConfig::class.java)
