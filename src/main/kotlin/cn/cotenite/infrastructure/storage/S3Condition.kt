package cn.cotenite.infrastructure.storage

import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.boot.autoconfigure.condition.SpringBootCondition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.util.StringUtils

/**
 * S3存储服务启用条件
 * 检查S3配置是否完整且有效
 */
class S3EnabledCondition : SpringBootCondition() {

    override fun getMatchOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val env = context.environment
        val accessKey = env.getProperty("s3.access-key")
        val secretKey = env.getProperty("s3.secret-key")
        val endpoint = env.getProperty("s3.endpoint")
        val bucketName = env.getProperty("s3.bucket-name")

        val hasValidConfig = StringUtils.hasText(accessKey)
            && StringUtils.hasText(secretKey)
            && StringUtils.hasText(endpoint)
            && StringUtils.hasText(bucketName)

        return if (hasValidConfig) {
            ConditionOutcome.match("S3配置完整，启用S3存储服务")
        } else {
            ConditionOutcome.noMatch("S3配置不完整，使用NoOp存储服务")
        }
    }
}

/**
 * S3存储服务禁用条件（S3EnabledCondition 的反向条件）
 */
class S3DisabledCondition : SpringBootCondition() {

    override fun getMatchOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val env = context.environment
        val accessKey = env.getProperty("s3.access-key")
        val secretKey = env.getProperty("s3.secret-key")
        val endpoint = env.getProperty("s3.endpoint")
        val bucketName = env.getProperty("s3.bucket-name")

        val hasValidConfig = StringUtils.hasText(accessKey)
            && StringUtils.hasText(secretKey)
            && StringUtils.hasText(endpoint)
            && StringUtils.hasText(bucketName)

        return if (!hasValidConfig) {
            ConditionOutcome.match("S3配置不完整，使用NoOp存储服务")
        } else {
            ConditionOutcome.noMatch("S3配置完整，不使用NoOp存储服务")
        }
    }
}
