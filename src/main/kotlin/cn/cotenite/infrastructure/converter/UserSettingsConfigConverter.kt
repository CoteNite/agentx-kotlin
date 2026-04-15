package cn.cotenite.infrastructure.converter

import cn.cotenite.domain.user.model.config.UserSettingsConfig
import cn.cotenite.infrastructure.utils.JsonUtils
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import org.postgresql.util.PGobject
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/** 用户设置配置转换器 处理 JSON 存储的用户设置配置信息 */
@MappedTypes(UserSettingsConfig::class)
@MappedJdbcTypes(JdbcType.OTHER)
class UserSettingsConfigConverter : BaseTypeHandler<UserSettingsConfig>() {

    @Throws(SQLException::class)
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: UserSettingsConfig,
        jdbcType: JdbcType?
    ) {
        val jsonObject = PGobject().apply {
            type = "jsonb"
            value = JsonUtils.toJsonString(parameter)
        }
        ps.setObject(i, jsonObject)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnName: String): UserSettingsConfig =
        parseJson(rs.getString(columnName))

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): UserSettingsConfig =
        parseJson(rs.getString(columnIndex))

    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): UserSettingsConfig =
        parseJson(cs.getString(columnIndex))

    private fun parseJson(json: String?): UserSettingsConfig {
        if (json.isNullOrBlank()) {
            return UserSettingsConfig()
        }
        return JsonUtils.parseObject(json, UserSettingsConfig::class.java) ?: UserSettingsConfig()
    }
}