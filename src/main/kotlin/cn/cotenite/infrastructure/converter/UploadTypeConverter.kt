package cn.cotenite.infrastructure.converter

import cn.cotenite.domain.tool.constant.UploadType
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/** 上传类型转换器 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(UploadType::class)
class UploadTypeConverter : BaseTypeHandler<UploadType>() {

    @Throws(SQLException::class)
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: UploadType,
        jdbcType: JdbcType?
    ) {
        // Kotlin 直接访问枚举的 name 属性
        ps.setString(i, parameter.name)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnName: String): UploadType? {
        return rs.getString(columnName)?.let { UploadType.fromCode(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): UploadType? {
        return rs.getString(columnIndex)?.let { UploadType.fromCode(it) }
    }

    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): UploadType? {
        return cs.getString(columnIndex)?.let { UploadType.fromCode(it) }
    }
}