package com.mindbridge.wishmap.context.appversion.domain

import com.mindbridge.wishmap.context.appversion.domain.AppVersionControl
import org.springframework.data.jpa.repository.JpaRepository

interface AppVersionControlRepository : JpaRepository<AppVersionControl, Long> {
    fun findByPlatform(platform: String): AppVersionControl?
}
