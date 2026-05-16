package com.gramaUrja.model

import com.google.firebase.database.PropertyName

data class PowerStatus(
    val zoneId: String = "",
    val zoneName: String = "",
    @get:PropertyName("isOn")
    @set:PropertyName("isOn")
    var isOn: Boolean = false,
    val lastUpdatedTimestamp: Long = 0L,
    val updatedBy: String = "Community"
) {
    // No-arg constructor required for Firebase
    constructor() : this("", "", false, 0L, "Community")
}

data class Zone(
    val id: String = "",
    val name: String = "",
    val district: String = ""
) {
    constructor() : this("", "", "")
}

data class CropType(
    val name: String,
    val minMinutes: Int,
    val maxMinutes: Int,
    val description: String
)
