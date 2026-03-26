package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import cn.cotenite.domain.agent.model.AgentTool
import cn.cotenite.infrastructure.utils.JsonUtils
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * AgentTool列表JSON转换器
 */
@MappedTypes(MutableList::class)
@MappedJdbcTypes(JdbcType.OTHER)
class AgentToolListConverter : BaseTypeHandler<MutableList<AgentTool>>() {

    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: MutableList<AgentTool>,
        jdbcType: JdbcType?
    ) {
        ps.setObject(i, JsonUtils.toJsonString(parameter), Types.OTHER)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): MutableList<AgentTool>? =
        parseJson(rs.getString(columnName))

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): MutableList<AgentTool>? =
        parseJson(rs.getString(columnIndex))

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): MutableList<AgentTool>? =
        parseJson(cs.getString(columnIndex))

    private fun parseJson(json: String?): MutableList<AgentTool>? =
        json?.takeIf { it.isNotBlank() }
            ?.let { JsonUtils.parseArray(it, AgentTool::class.java).toMutableList() }
}
