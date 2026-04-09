package cn.cotenite.infrastructure.converter

import cn.cotenite.domain.tool.constant.ToolType
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/** 工具类型转换器 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(ToolType::class)
class ToolTypeConverter : BaseTypeHandler<ToolType>() {

    @Throws(SQLException::class)
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: ToolType,
        jdbcType: JdbcType?
    ) {
        // 直接访问属性 name，相当于调用 Java 的 name()
        ps.setString(i, parameter.name)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnName: String): ToolType? {
        return rs.getString(columnName)?.let { ToolType.fromCode(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): ToolType? {
        return rs.getString(columnIndex)?.let { ToolType.fromCode(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): ToolType? {
        return cs.getString(columnIndex)?.let { ToolType.fromCode(it) }
    }
}