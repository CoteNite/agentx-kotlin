package cn.cotenite.infrastructure.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.util.Date
import javax.crypto.SecretKey

/**
 * JWT 工具类
 */
object JwtUtils {

    private const val JWT_SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437"
    private const val EXPIRATION_TIME = 24 * 60 * 60 * 1000L

    private fun signingKey(): SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET))

    fun generateToken(userId: String): String {
        val now = Date()
        val expiry = Date(now.time + EXPIRATION_TIME)
        return Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey())
            .compact()
    }

    fun getUserIdFromToken(token: String): String =
        parseClaims(token).subject

    fun validateToken(token: String): Boolean =
        runCatching {
            parseClaims(token)
        }.isSuccess

    private fun parseClaims(token: String): Claims =
        try {
            Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: JwtException) {
            throw e
        } catch (e: IllegalArgumentException) {
            throw e
        }
}
