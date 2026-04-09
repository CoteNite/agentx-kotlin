package cn.cotenite.infrastructure.converter

import cn.cotenite.domain.scheduledtask.constant.RepeatType
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/** 重复类型转换器 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(RepeatType::class)
class RepeatTypeConverter : BaseTypeHandler<RepeatType>() {

    @Throws(SQLException::class)
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: RepeatType,
        jdbcType: JdbcType?
    ) {
        ps.setString(i, parameter.name)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnName: String): RepeatType? {
        return rs.getString(columnName)?.let { RepeatType.fromCode(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): RepeatType? {
        return rs.getString(columnIndex)?.let { RepeatType.fromCode(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): RepeatType? {
        return cs.getString(columnIndex)?.let { RepeatType.fromCode(it) }
    }
}
