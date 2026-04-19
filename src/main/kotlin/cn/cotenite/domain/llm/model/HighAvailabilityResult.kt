package cn.cotenite.domain.llm.model

/**
 * 高可用选择结果
 *
 * @author xhy
 * @since 1.0.0
 */
class HighAvailabilityResult {
    /** 选择的 Provider */
    var provider: ProviderEntity? = null

    /** 选择的 Model（可能有不同的部署名称） */
    var model: ModelEntity? = null

    /** 实例 ID（用于结果上报） */
    var instanceId: String? = null
}
