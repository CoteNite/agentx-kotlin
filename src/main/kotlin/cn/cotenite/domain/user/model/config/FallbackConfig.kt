package cn.cotenite.domain.user.model.config

/**
 * @author  yhk
 * Description  
 * Date  2026/4/18 19:59
 */
data class FallbackConfig(
    var enabled: Boolean=false,
    var fallbackChain: MutableList<String> = mutableListOf()
){

    /** 添加降级模型到链中  */
    fun addFallbackModel(modelId: String?) {
        if (modelId != null && !fallbackChain.contains(modelId)) {
            fallbackChain.add(modelId)
        }
    }

    /** 移除降级模型  */
    fun removeFallbackModel(modelId: String?) {
        fallbackChain.remove(modelId)
    }

    /** 获取所有降级模型列表  */
    fun getFallbackModels(): MutableList<String?> {
        return ArrayList(fallbackChain)
    }
}