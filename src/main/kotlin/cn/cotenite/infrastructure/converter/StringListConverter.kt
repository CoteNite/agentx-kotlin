package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import cn.cotenite.infrastructure.utils.JsonUtils
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * String列表JSON转换器
 */
@MappedTypes(MutableList::class)
@MappedJdbcTypes(JdbcType.OTHER)
class StringListConverter : BaseTypeHandler<MutableList<String>>() {

    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: MutableList<String>,
        jdbcType: JdbcType?
    ) {
        ps.setObject(i, JsonUtils.toJsonString(parameter), Types.OTHER)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): MutableList<String>? =
        parseJson(rs.getString(columnName))

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): MutableList<String>? =
        parseJson(rs.getString(columnIndex))

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): MutableList<String>? =
        parseJson(cs.getString(columnIndex))

    private fun parseJson(json: String?): MutableList<String>? =
        json?.takeIf { it.isNotBlank() }
            ?.let { JsonUtils.parseArray(it, String::class.java).toMutableList() }
}
