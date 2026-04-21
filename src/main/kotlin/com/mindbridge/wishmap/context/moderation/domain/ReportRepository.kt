package com.mindbridge.wishmap.context.moderation.domain

import com.mindbridge.wishmap.context.moderation.domain.Report
import com.mindbridge.wishmap.context.moderation.domain.ReportTargetType
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<Report, Long> {
    fun existsByReporterIdAndTargetTypeAndTargetId(reporterId: Long, targetType: ReportTargetType, targetId: Long): Boolean
    fun deleteAllByReporterId(reporterId: Long)
}
