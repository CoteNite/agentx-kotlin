package cn.cotenite.domain.tool.model.config

import dev.langchain4j.agent.tool.ToolSpecification
import org.slf4j.LoggerFactory
import java.lang.reflect.Field

/**
 * 工具规范转换器，用于将ToolSpecification对象转换为可序列化的DTO对象
 */
object ToolSpecificationConverter {

    private val log = LoggerFactory.getLogger(ToolSpecificationConverter::class.java)

    /**
     * 将 ToolSpecification 列表转换为 ToolDefinition 列表
     */
    fun convert(specifications: List<ToolSpecification>?): List<ToolDefinition> {
        return specifications?.mapNotNull { spec ->
            try {
                convertSingle(spec)
            } catch (e: Exception) {
                log.error("转换工具规范失败: {}", spec.name(), e)
                null
            }
        } ?: emptyList()
    }

    /**
     * 转换单个 [ToolSpecification] 对象
     */
    fun convertSingle(spec: ToolSpecification?): ToolDefinition {
        requireNotNull(spec) { "工具规范不能为空" }

        return ToolDefinition().apply {
            name = spec.name()
            description = spec.description()
            enabled = true

            // 处理参数
            spec.parameters()?.let { params ->
                val toolParameter = extractParametersReflectively(params)
                parameters = mapOf(
                    "properties" to toolParameter.properties,
                    "required" to toolParameter.required
                )
            }
        }
    }

    /**
     * 使用反射机制提取参数信息
     */
    private fun extractParametersReflectively(parameters: Any): ToolParameter {
        val properties = mutableMapOf<String, ParameterProperty>()
        val requiredNames = mutableListOf<String>()

        try {
            // 获取 properties 字段或 getter
            val propertiesMap = getFieldValueSafely(parameters, "properties", Map::class.java)
            propertiesMap?.forEach { (key, value) ->
                val propertyName = key.toString()
                // 获取属性的 description
                val descObj = value?.let { getFieldValueSafely(it, "description", Any::class.java) }
                properties[propertyName] = ParameterProperty(descObj?.toString())
            }

            // 获取 required 字段
            when (val requiredObj = getFieldValueSafely(parameters, "required", Any::class.java)) {
                is Collection<*> -> requiredNames.addAll(requiredObj.filterNotNull().map { it.toString() })
                is Array<*> -> requiredNames.addAll(requiredObj.filterNotNull().map { it.toString() })
            }

        } catch (e: Exception) {
            log.error("反射提取参数失败", e)
        }

        return ToolParameter().apply {
            this.properties = properties
            this.required = requiredNames
        }
    }

    /**
     * 安全地获取对象字段值
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getFieldValueSafely(obj: Any?, fieldName: String, expectedType: Class<T>): T? {
        if (obj == null) return null

        // 1. 尝试通过 getter 方法获取 (Kotlin 属性访问底层也是这个)
        try {
            val getterName = "get${fieldName.replaceFirstChar { it.uppercase() }}"
            val method = obj.javaClass.getMethod(getterName)
            val result = method.invoke(obj)
            if (expectedType.isInstance(result)) return result as T
        } catch (ignored: Exception) { }

        // 2. 尝试直接通过 Field 访问
        try {
            findField(obj.javaClass, fieldName)?.apply {
                isAccessible = true
                val result = get(obj)
                if (expectedType.isInstance(result)) return result as T
            }
        } catch (ignored: Exception) { }

        // 3. 如果是 Map
        if (obj is Map<*, *>) {
            val result = obj[fieldName]
            if (expectedType.isInstance(result)) return result as T
        }

        return null
    }

    /**
     * 递归查找字段
     */
    private tailrec fun findField(clazz: Class<*>?, fieldName: String): Field? {
        if (clazz == null || clazz == Any::class.java) return null
        return try {
            clazz.getDeclaredField(fieldName)
        } catch (e: NoSuchFieldException) {
            findField(clazz.superclass, fieldName)
        }
    }

}