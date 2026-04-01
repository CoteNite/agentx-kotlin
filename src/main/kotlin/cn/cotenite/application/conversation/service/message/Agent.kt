package cn.cotenite.application.conversation.service.message

import dev.langchain4j.service.TokenStream

/**
 * @author  yhk
 * Description  
 * Date  2026/3/28 22:44
 */
interface Agent {
    fun chat(message: String): TokenStream
}