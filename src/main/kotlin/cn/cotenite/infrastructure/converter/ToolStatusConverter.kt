package cn.cotenite.infrastructure.converter

import cn.cotenite.domain.tool.constant.ToolStatus
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/** 工具状态转换器 */
@MappedTypes(ToolStatus::class)
@MappedJdbcTypes(JdbcType.INTEGER)
class ToolStatusConverter : BaseTypeHandler<ToolStatus>() {

    @Throws(SQLException::class)
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: ToolStatus,
        jdbcType: JdbcType?
    ) {
        ps.setString(i, parameter.name)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnName: String): ToolStatus? {
        return rs.getString(columnName)?.let { ToolStatus.fromCode(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): ToolStatus? {
        return rs.getString(columnIndex)?.let { ToolStatus.fromCode(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): ToolStatus? {
        return cs.getString(columnIndex)?.let { ToolStatus.fromCode(it) }
    }
}