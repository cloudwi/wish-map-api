package com.mindbridge.wishmap.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val start = System.currentTimeMillis()

        try {
            filterChain.doFilter(request, response)
        } finally {
            val uri = request.requestURI
            if (uri == "/health" || uri == "/actuator/health") return

            val duration = System.currentTimeMillis() - start
            val method = request.method
            val query = request.queryString?.let { "?$it" } ?: ""
            val status = response.status

            if (status >= 500) {
                log.error("{} {}{} → {} ({}ms)", method, uri, query, status, duration)
            } else if (status >= 400) {
                log.warn("{} {}{} → {} ({}ms)", method, uri, query, status, duration)
            } else if (duration > 1000) {
                log.warn("{} {}{} → {} ({}ms) SLOW", method, uri, query, status, duration)
            } else {
                log.info("{} {}{} → {} ({}ms)", method, uri, query, status, duration)
            }
        }
    }
}
