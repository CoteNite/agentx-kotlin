package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * 服务商协议转换器
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(ProviderProtocol::class)
class ProviderProtocolConverter : BaseTypeHandler<ProviderProtocol>() {

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: ProviderProtocol, jdbcType: JdbcType?) {
        ps.setString(i, parameter.code)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): ProviderProtocol? =
        rs.getString(columnName)?.let(ProviderProtocol::fromCode)

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): ProviderProtocol? =
        rs.getString(columnIndex)?.let(ProviderProtocol::fromCode)

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): ProviderProtocol? =
        cs.getString(columnIndex)?.let(ProviderProtocol::fromCode)
}
