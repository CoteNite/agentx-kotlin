package cn.cotenite.infrastructure.converter

import cn.cotenite.domain.scheduledtask.model.RepeatConfig
import org.apache.ibatis.type.MappedTypes

/** 重复配置转换器 */
@MappedTypes(RepeatConfig::class)
class RepeatConfigConverter : JsonToStringConverter<RepeatConfig>(RepeatConfig::class.java)
