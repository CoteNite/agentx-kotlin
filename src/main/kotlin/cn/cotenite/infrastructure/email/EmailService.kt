package cn.cotenite.infrastructure.email

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Properties

/**
 * 邮件服务
 */
@Service
class EmailService(
    @Value("\${mail.smtp.host}") private val host: String,
    @Value("\${mail.smtp.port}") private val port: Int,
    @Value("\${mail.smtp.username}") private val username: String,
    @Value("\${mail.smtp.password}") private val password: String,
    @Value("\${mail.verification.subject}") private val verificationSubject: String,
    @Value("\${mail.verification.template}") private val verificationTemplate: String
) {

    fun sendVerificationCode(to: String, code: String) {
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port.toString())
            put("mail.smtp.auth", "true")
            put("mail.smtp.ssl.enable","true")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication =
                PasswordAuthentication(username, password)
        })

        runCatching {
            MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                subject = verificationSubject
                setText(verificationTemplate.format(code))
            }.also(Transport::send)
        }.getOrElse {
            throw RuntimeException("发送邮件失败: ${it.message}", it)
        }
    }
}
