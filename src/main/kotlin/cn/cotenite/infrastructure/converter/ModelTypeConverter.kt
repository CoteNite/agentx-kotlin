package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import cn.cotenite.domain.llm.model.enums.ModelType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * 模型类型转换器
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(ModelType::class)
class ModelTypeConverter : BaseTypeHandler<ModelType>() {

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: ModelType, jdbcType: JdbcType?) {
        ps.setString(i, parameter.code)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): ModelType? =
        rs.getString(columnName)?.let(ModelType::fromCode)

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): ModelType? =
        rs.getString(columnIndex)?.let(ModelType::fromCode)

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): ModelType? =
        cs.getString(columnIndex)?.let(ModelType::fromCode)
}
