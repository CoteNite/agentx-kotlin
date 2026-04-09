package cn.cotenite.infrastructure.converter

import cn.cotenite.domain.scheduledtask.constant.ScheduleTaskStatus
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/** 定时任务状态转换器 */
@MappedTypes(ScheduleTaskStatus::class)
@MappedJdbcTypes(JdbcType.VARCHAR)
class ScheduledTaskStatusConverter : BaseTypeHandler<ScheduleTaskStatus>() {

    @Throws(SQLException::class)
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: ScheduleTaskStatus,
        jdbcType: JdbcType?
    ) {
        ps.setString(i, parameter.name)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnName: String): ScheduleTaskStatus? {
        return rs.getString(columnName)?.let { ScheduleTaskStatus.fromCode(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): ScheduleTaskStatus? {
        return rs.getString(columnIndex)?.let { ScheduleTaskStatus.fromCode(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): ScheduleTaskStatus? {
        return cs.getString(columnIndex)?.let { ScheduleTaskStatus.fromCode(it) }
    }
}