package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import cn.cotenite.domain.conversation.constant.Role
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * 角色转换器
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(Role::class)
class RoleConverter : BaseTypeHandler<Role>() {

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: Role, jdbcType: JdbcType?) {
        ps.setString(i, parameter.code)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): Role? =
        rs.getString(columnName)?.let(Role::fromCode)

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): Role? =
        rs.getString(columnIndex)?.let(Role::fromCode)

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): Role? =
        cs.getString(columnIndex)?.let(Role::fromCode)
}
