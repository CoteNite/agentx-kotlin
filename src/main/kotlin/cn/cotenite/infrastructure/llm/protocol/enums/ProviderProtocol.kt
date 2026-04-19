package cn.cotenite.infrastructure.llm.protocol.enums

enum class ProviderProtocol {
    OPENAI,
    ANTHROPIC;

    companion object {
        fun fromCode(code: String): ProviderProtocol {
            for (protocol in entries) {
                if (protocol.name.equals(code, ignoreCase = true)) {
                    return protocol
                }
            }
            throw IllegalArgumentException("Unknown model type code: $code")
        }
    }
}
