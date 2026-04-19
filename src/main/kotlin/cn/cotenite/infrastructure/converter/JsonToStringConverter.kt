package cn.cotenite.infrastructure.converter

import com.fasterxml.jackson.core.type.TypeReference
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
    private val type: Class<T>? = null,
    private val typeReference: TypeReference<T>? = null
) : BaseTypeHandler<T>() {

    protected constructor(type: Class<T>) : this(type = type, typeReference = null)

    protected constructor(typeReference: TypeReference<T>) : this(type = null, typeReference = typeReference)

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: T, jdbcType: JdbcType?) {
        ps.setObject(i, JsonUtils.toJsonString(parameter), Types.OTHER)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): T? =
        parseJson(rs.getString(columnName))

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): T? =
        parseJson(rs.getString(columnIndex))

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): T? =
        parseJson(cs.getString(columnIndex))

    protected open fun parseJson(json: String?): T? =
        json?.takeIf { it.isNotBlank() }?.let {
            typeReference?.let { ref -> JsonUtils.parseObject(it, ref) }
                ?: JsonUtils.parseObject(it, requireNotNull(type))
        }
}
