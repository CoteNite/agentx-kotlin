package cn.cotenite.agentxkotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class AgentxKotlinApplication

fun main(args: Array<String>) {
	runApplication<AgentxKotlinApplication>(*args)
}
