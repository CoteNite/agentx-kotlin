package cn.cotenite.infrastructure.llm.protocol.enums

/**
 * LLM服务商协议
 */
enum class ProviderProtocol(
    val code: String
) {
    OPENAI("openai"),
    ANTHROPIC("anthropic"),
    AZURE_OPENAI("azure_openai"),
    OTHER("other");

    companion object {
        fun fromCode(code: String?): ProviderProtocol =
            entries.firstOrNull { it.code.equals(code, true) || it.name.equals(code, true) } ?: OTHER
    }
}
