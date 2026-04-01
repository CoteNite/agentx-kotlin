package cn.cotenite.infrastructure.converter

import cn.cotenite.domain.task.constant.TaskStatus
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * @author  yhk
 * Description  
 * Date  2026/4/1 18:56
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(TaskStatus::class)
class TaskStatusConverter: BaseTypeHandler<TaskStatus>(){

    @Throws(SQLException::class)
    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: TaskStatus, jdbcType: JdbcType) {
        ps.setString(i, parameter.name)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnName: String?): TaskStatus? {
        val value = rs.getString(columnName)
        return if (value == null) null else TaskStatus.valueOf(value)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): TaskStatus? {
        val value = rs.getString(columnIndex)
        return if (value == null) null else TaskStatus.valueOf(value)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): TaskStatus? {
        val value = cs.getString(columnIndex)
        return if (value == null) null else TaskStatus.valueOf(value)
    }
    
}