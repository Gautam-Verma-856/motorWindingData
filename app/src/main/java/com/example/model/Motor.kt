package com.example.model

object MotorConnectionOptions {
    val PRESET_OPTIONS = listOf("Star", "Delta", "Star-Delta", "Series", "Parallel")
    const val OPTION_OTHER = "Other"
    val DROPDOWN_OPTIONS = PRESET_OPTIONS + OPTION_OTHER

    fun resolveSavedSelection(savedConnection: String): Pair<String, String> {
        val trimmed = savedConnection.trim()
        if (trimmed.isEmpty()) return "" to ""
        val matchedPreset = PRESET_OPTIONS.firstOrNull { it.equals(trimmed, ignoreCase = true) }
        return if (matchedPreset != null) {
            matchedPreset to ""
        } else {
            OPTION_OTHER to trimmed
        }
    }

    fun resolveForSave(selectedOption: String, customConnection: String): String {
        return when {
            selectedOption == OPTION_OTHER -> customConnection.trim()
            else -> selectedOption.trim()
        }
    }

    fun matchesConnectionFilter(motorConnection: String, filterOption: String): Boolean {
        if (filterOption == "All") return true
        val trimmed = motorConnection.trim()
        if (filterOption == OPTION_OTHER) {
            return trimmed.isNotEmpty() && PRESET_OPTIONS.none { it.equals(trimmed, ignoreCase = true) }
        }
        return trimmed.equals(filterOption, ignoreCase = true)
    }
}

data class SinglePhaseMotor(
    val id: String = "",
    val photoUrl: String = "", // Kept for backward compatibility
    val photoUrls: List<String> = emptyList(),
    val category: String = "", // "Self Priming (Tullu Pump)", "Submersible Motor", "Pandubi", or ""
    val companyName: String = "",
    val hp: String = "",
    val capacitor: String = "",
    val rpm: String = "",
    val amps: String = "",
    val coilSize: String = "",
    val connection: String = "",
    val runningPitch: String = "",
    val runningTurn: String = "",
    val runningSwg: String = "",
    val runningWeight: String = "",
    val startingPitch: String = "",
    val startingTurn: String = "",
    val startingSwg: String = "",
    val startingWeight: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val createdByName: String = "",
    val status: String = "", // Explicitly approved records will have "approved"
    val adminSeen: Boolean = false,
    val seen: Boolean = false,
    val rejectionReason: String = "",
    val reviewedAt: Long = 0L,
    val reviewedBy: String = "",
    val reviewedByName: String = ""
) {
    fun isExplicitlyApproved(): Boolean = status.trim().equals("approved", ignoreCase = true)
    fun isPending(): Boolean = status.trim().equals("pending", ignoreCase = true)
    fun isRejected(): Boolean = status.trim().equals("rejected", ignoreCase = true)
    fun isUnseen(): Boolean = isPending() && !adminSeen && !seen

    fun matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true
        return companyName.contains(query, ignoreCase = true) ||
            hp.contains(query, ignoreCase = true) ||
            category.contains(query, ignoreCase = true) ||
            rpm.contains(query, ignoreCase = true) ||
            amps.contains(query, ignoreCase = true) ||
            coilSize.contains(query, ignoreCase = true) ||
            connection.contains(query, ignoreCase = true)
    }
}

data class ThreePhaseMotor(
    val id: String = "",
    val photoUrl: String = "", // Kept for backward compatibility
    val photoUrls: List<String> = emptyList(),
    val category: String = "", // "Monoblock", "Submersible", "Pandubi", or ""
    val name: String = "", // Company/Motor Name
    val slot: String = "",
    val hp: String = "",
    val rpm: String = "",
    val amps: String = "",
    val coilSize: String = "",
    val connection: String = "",
    val pitch: String = "",
    val turn: String = "",
    val swg: String = "",
    val weight: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val createdByName: String = "",
    val status: String = "", // Explicitly approved records will have "approved"
    val adminSeen: Boolean = false,
    val seen: Boolean = false,
    val rejectionReason: String = "",
    val reviewedAt: Long = 0L,
    val reviewedBy: String = "",
    val reviewedByName: String = ""
) {
    fun isExplicitlyApproved(): Boolean = status.trim().equals("approved", ignoreCase = true)
    fun isPending(): Boolean = status.trim().equals("pending", ignoreCase = true)
    fun isRejected(): Boolean = status.trim().equals("rejected", ignoreCase = true)
    fun isUnseen(): Boolean = isPending() && !adminSeen && !seen

    fun matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true
        return name.contains(query, ignoreCase = true) ||
            hp.contains(query, ignoreCase = true) ||
            category.contains(query, ignoreCase = true) ||
            slot.contains(query, ignoreCase = true) ||
            rpm.contains(query, ignoreCase = true) ||
            amps.contains(query, ignoreCase = true) ||
            coilSize.contains(query, ignoreCase = true) ||
            connection.contains(query, ignoreCase = true)
    }
}

data class CustomCategoryField(
    val id: String = "",
    val fieldName: String = "", // e.g. "RPM", "Wire Gauge", "Turns", "Weight", "Capacitor"
    val fieldType: String = "text", // "text" or "number"
    val isRequired: Boolean = false,
    val order: Int = 0
)

data class CustomCategory(
    val id: String = "",
    val name: String = "", // e.g. "Cooler Motor", "Farata Motor", "Exhaust Fan Motor"
    val description: String = "",
    val iconName: String = "Build",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val allowPublicSubmissions: Boolean = true,
    val fields: List<CustomCategoryField> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
)

data class CustomMotor(
    val id: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val companyName: String = "", // Mandatory
    val motorName: String = "", // Model / Motor Name
    val hp: String = "",
    val photoUrl: String = "", // Kept for backward compatibility
    val photoUrls: List<String> = emptyList(),
    val customFields: Map<String, String> = emptyMap(), // fieldId or fieldName -> value string
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val createdByName: String = "",
    val status: String = "", // "approved", "pending", "rejected"
    val adminSeen: Boolean = false,
    val seen: Boolean = false,
    val rejectionReason: String = "",
    val reviewedAt: Long = 0L,
    val reviewedBy: String = "",
    val reviewedByName: String = ""
) {
    fun isExplicitlyApproved(): Boolean = status.trim().equals("approved", ignoreCase = true)
    fun isPending(): Boolean = status.trim().equals("pending", ignoreCase = true)
    fun isRejected(): Boolean = status.trim().equals("rejected", ignoreCase = true)
    fun isUnseen(): Boolean = isPending() && !adminSeen && !seen
}
