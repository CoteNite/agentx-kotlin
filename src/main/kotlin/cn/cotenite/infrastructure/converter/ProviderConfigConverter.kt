package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import cn.cotenite.domain.llm.model.config.ProviderConfig
import cn.cotenite.infrastructure.utils.EncryptUtils
import cn.cotenite.infrastructure.utils.JsonUtils
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * 服务商配置转换器
 */
@MappedTypes(ProviderConfig::class)
@MappedJdbcTypes(JdbcType.VARCHAR, JdbcType.LONGVARCHAR, JdbcType.OTHER)
class ProviderConfigConverter : org.apache.ibatis.type.BaseTypeHandler<ProviderConfig>() {

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: ProviderConfig, jdbcType: JdbcType?) {
        ps.setObject(i, EncryptUtils.encrypt(JsonUtils.toJsonString(parameter)), Types.OTHER)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): ProviderConfig =
        parseEncryptedJson(rs.getString(columnName))

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): ProviderConfig =
        parseEncryptedJson(rs.getString(columnIndex))

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): ProviderConfig =
        parseEncryptedJson(cs.getString(columnIndex))

    private fun parseEncryptedJson(encryptedStr: String?): ProviderConfig {
        if (encryptedStr.isNullOrBlank()) return ProviderConfig()
        val jsonStr = EncryptUtils.decrypt(encryptedStr) ?: "{}"
        return JsonUtils.parseObject(jsonStr, ProviderConfig::class.java) ?: ProviderConfig()
    }
}
