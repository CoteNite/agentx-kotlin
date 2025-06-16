package cn.cotenite.agentxkotlin.infrastructure.typehandler

import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import cn.cotenite.agentxkotlin.domain.agent.model.ModelConfig
import cn.cotenite.agentxkotlin.infrastructure.utils.JsonUtils.parseArray
import cn.cotenite.agentxkotlin.infrastructure.utils.JsonUtils.parseObject
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import org.postgresql.util.PGobject
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 03:06
 */
@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes( Object::class, List::class, ModelConfig::class, AgentTool::class )
class JsonTypeHandler<T:Any>:BaseTypeHandler<T> {

    private val clazz: KClass<T>
    private val isList: Boolean
    private val itemClazz: KClass<*>?

    constructor() : this(Any::class as KClass<T>, false, null)
    constructor(clazz: KClass<T>) : this(clazz, false, null)


    private fun parse(json: String?): T? {
        if (json.isNullOrEmpty()) {
            return null
        }

        if (isList && itemClazz != null) {
            val list = parseArray(json, itemClazz.java)
            return list as T
        } else {
            return parseObject(json, clazz.java)
        }
    }

    /**
     * 辅助构造函数，用于列表类型
     * @param clazz 整个类型的 KClass，对于 List<Foo> 来说就是 List::class
     * @param isList 标记是否为列表类型
     * @param itemClazz 列表项的 KClass，例如 Foo::class
     */
    constructor(clazz: KClass<T>, isList: Boolean, itemClazz: KClass<*>?) : super() {
        this.clazz = clazz
        this.isList = isList
        this.itemClazz = itemClazz
    }

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: T, jdbcType: JdbcType?) {
        val jsonObject = PGobject().apply {
            type = "json"
            value = parameter.toString()
        }
        ps.setObject(i, jsonObject)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String?): T? {
        return parse(rs.getString(columnName))
    }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): T? {
        return parse(rs.getString(columnIndex))
    }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): T? {
        return parse(cs.getString(columnIndex))
    }
}
