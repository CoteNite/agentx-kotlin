package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import cn.cotenite.infrastructure.utils.JsonUtils
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * JSON类型转换器
 */
@MappedJdbcTypes(JdbcType.OTHER)
abstract class JsonToStringConverter<T>(
    private val type: Class<T>
) : BaseTypeHandler<T>() {

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: T, jdbcType: JdbcType?) {
        ps.setObject(i, JsonUtils.toJsonString(parameter), Types.OTHER)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): T? =
        parseJson(rs.getString(columnName))

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): T? =
        parseJson(rs.getString(columnIndex))

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): T? =
        parseJson(cs.getString(columnIndex))

    private fun parseJson(json: String?): T? =
        json?.takeIf { it.isNotBlank() }?.let { JsonUtils.parseObject(it, type) }
}
