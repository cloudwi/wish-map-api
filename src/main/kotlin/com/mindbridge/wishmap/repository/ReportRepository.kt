package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.moderation.Report
import com.mindbridge.wishmap.domain.moderation.ReportTargetType
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<Report, Long> {
    fun existsByReporterIdAndTargetTypeAndTargetId(reporterId: Long, targetType: ReportTargetType, targetId: Long): Boolean
    fun deleteAllByReporterId(reporterId: Long)
}
