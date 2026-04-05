package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.appversion.AppVersionControl
import org.springframework.data.jpa.repository.JpaRepository

interface AppVersionControlRepository : JpaRepository<AppVersionControl, Long> {
    fun findByPlatform(platform: String): AppVersionControl?
}
