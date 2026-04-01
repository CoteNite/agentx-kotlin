package cn.cotenite.infrastructure.converter

import cn.cotenite.domain.conversation.constant.MessageType
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * MessageType 枚举转换器
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(MessageType::class)
class MessageTypeConverter : BaseTypeHandler<MessageType>() {

    @Throws(SQLException::class)
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: MessageType,
        jdbcType: JdbcType?
    ) {
        ps.setString(i, parameter.name)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnName: String): MessageType? {
        return rs.getString(columnName)?.let { MessageType.valueOf(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): MessageType? {
        return rs.getString(columnIndex)?.let { MessageType.valueOf(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): MessageType? {
        return cs.getString(columnIndex)?.let { MessageType.valueOf(it) }
    }
}