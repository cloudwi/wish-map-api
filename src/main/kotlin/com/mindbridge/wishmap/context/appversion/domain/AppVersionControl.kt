package com.mindbridge.wishmap.context.appversion.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "app_version_control")
class AppVersionControl(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val platform: String,

    @Column(name = "min_version", nullable = false)
    var minVersion: String,

    @Column(name = "latest_version", nullable = false)
    var latestVersion: String,

    @Column(name = "force_update", nullable = false)
    var forceUpdate: Boolean = false,

    @Column(name = "store_url")
    var storeUrl: String? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
