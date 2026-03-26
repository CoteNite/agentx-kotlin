package cn.cotenite.infrastructure.typehandler

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import cn.cotenite.infrastructure.utils.JsonUtils
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * 通用JSON类型处理器
 */
@MappedJdbcTypes(JdbcType.VARCHAR, JdbcType.OTHER)
@MappedTypes(Any::class)
open class JsonTypeHandler<T>(private val type: Class<T>) : BaseTypeHandler<T>() {

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: T, jdbcType: JdbcType?) {
        ps.setString(i, JsonUtils.toJsonString(parameter))
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
