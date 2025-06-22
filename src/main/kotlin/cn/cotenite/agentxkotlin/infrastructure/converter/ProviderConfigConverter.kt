package cn.cotenite.agentxkotlin.infrastructure.converter

import cn.cotenite.agentxkotlin.domain.llm.model.config.ProviderConfig
import cn.cotenite.agentxkotlin.infrastructure.util.EncryptUtils
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 03:11
 */
@Converter(autoApply = false) // 通常设置为 false，在实体字段上显式指定 @Convert
class ProviderConfigConverter : AttributeConverter<ProviderConfig?, String?> {

    private val objectMapper: ObjectMapper by lazy { ObjectMapper() }

    /**
     * 将 ProviderConfig 对象转换为加密的 JSON 字符串存储到数据库。
     *
     * @param attribute 要转换的 ProviderConfig 对象
     * @return 加密后的 JSON 字符串
     */
    override fun convertToDatabaseColumn(attribute: ProviderConfig?): String? {
        if (attribute == null) {
            return null
        }
        return try {
            val jsonStr = objectMapper.writeValueAsString(attribute)
            EncryptUtils.encrypt(jsonStr) // 使用你的加密工具类进行加密
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Error converting ProviderConfig to encrypted JSON string", e)
        }
    }

    /**
     * 将数据库中加密的 JSON 字符串解密后转换回 ProviderConfig 对象。
     *
     * @param dbData 数据库中存储的加密字符串
     * @return 对应的 ProviderConfig 对象
     */
    override fun convertToEntityAttribute(dbData: String?): ProviderConfig? {
        if (dbData.isNullOrBlank()) {
            return ProviderConfig() // 返回一个默认的 ProviderConfig 实例
        }
        return try {
            val decryptedJsonStr = EncryptUtils.decrypt(dbData) // 使用你的解密工具类进行解密
            objectMapper.readValue(decryptedJsonStr, ProviderConfig::class.java)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Error converting encrypted JSON string to ProviderConfig", e)
        } catch (e: Exception) {
            // 捕获解密或解析过程中可能出现的其他异常
            throw IllegalArgumentException("Error decrypting or parsing ProviderConfig JSON", e)
        }
    }
}