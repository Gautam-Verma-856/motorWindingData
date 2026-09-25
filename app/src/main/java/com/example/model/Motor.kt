package com.example.model

data class SinglePhaseMotor(
    val id: String = "",
    val photoUrl: String = "", // Kept for backward compatibility
    val photoUrls: List<String> = emptyList(),
    val category: String = "", // "Self Priming (Tullu Pump)", "Submersible Motor", "Pandubi", or ""
    val companyName: String = "",
    val hp: String = "",
    val capacitor: String = "",
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
    val status: String = "" // Explicitly approved records will have "approved"
) {
    fun isExplicitlyApproved(): Boolean = status.trim().equals("approved", ignoreCase = true)
    fun isPending(): Boolean = status.trim().equals("pending", ignoreCase = true)
    fun isRejected(): Boolean = status.trim().equals("rejected", ignoreCase = true)
}

data class ThreePhaseMotor(
    val id: String = "",
    val photoUrl: String = "", // Kept for backward compatibility
    val photoUrls: List<String> = emptyList(),
    val category: String = "", // "Monoblock", "Submersible", "Pandubi", or ""
    val name: String = "", // Company/Motor Name
    val slot: String = "",
    val hp: String = "",
    val pitch: String = "",
    val turn: String = "",
    val swg: String = "",
    val weight: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val createdByName: String = "",
    val status: String = "" // Explicitly approved records will have "approved"
) {
    fun isExplicitlyApproved(): Boolean = status.trim().equals("approved", ignoreCase = true)
    fun isPending(): Boolean = status.trim().equals("pending", ignoreCase = true)
    fun isRejected(): Boolean = status.trim().equals("rejected", ignoreCase = true)
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
    val status: String = "" // "approved", "pending", "rejected"
) {
    fun isExplicitlyApproved(): Boolean = status.trim().equals("approved", ignoreCase = true)
    fun isPending(): Boolean = status.trim().equals("pending", ignoreCase = true)
    fun isRejected(): Boolean = status.trim().equals("rejected", ignoreCase = true)
}
