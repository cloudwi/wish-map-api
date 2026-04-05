package com.mindbridge.wishmap.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class AppVersionFilter(
    @Value("\${app.min-version:1.0.0}") private val minVersion: String
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val appVersion = request.getHeader("X-App-Version")

        if (appVersion != null && isVersionLower(appVersion, minVersion)) {
            log.info("Force update required: app={} min={}", appVersion, minVersion)
            response.status = 426
            response.contentType = "application/json"
            response.writer.write("""{"error":"upgrade_required","message":"앱을 최신 버전으로 업데이트해주세요.","minVersion":"$minVersion"}""")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun isVersionLower(current: String, minimum: String): Boolean {
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val minimumParts = minimum.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(currentParts.size, minimumParts.size)
        for (i in 0 until maxLen) {
            val c = currentParts.getOrElse(i) { 0 }
            val m = minimumParts.getOrElse(i) { 0 }
            if (c < m) return true
            if (c > m) return false
        }
        return false
    }
}
