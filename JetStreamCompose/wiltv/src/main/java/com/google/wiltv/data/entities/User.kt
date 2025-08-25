package com.google.wiltv.data.entities

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val identifier: String,
    val name: String,
    val email: String,
    val password: String? = null,
    val clientIp: String,
    val deviceName: String,
    val deviceMacAddress: String,
    val profilePhotoPath: String?,
    val profilePhotoUrl: String?,
    val token: String? = null,
)
