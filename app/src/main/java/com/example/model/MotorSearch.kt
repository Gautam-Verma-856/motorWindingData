package com.example.model

import kotlin.math.abs

object ThreePhaseCategories {
    const val MONOBLOCK = "Monoblock"
    const val SUBMERSIBLE = "Submersible"
    const val PANDUBI = "Pandubi"
    const val GENERAL_OTHER = "General / Other Motors"

    val ALL_OPTIONS = listOf(
        MONOBLOCK,
        SUBMERSIBLE,
        PANDUBI,
        GENERAL_OTHER
    )

    private val PRIMARY_OPTIONS = listOf(
        MONOBLOCK,
        SUBMERSIBLE,
        PANDUBI
    )

    fun matchesCategory(motorCategory: String, selectedCategory: String): Boolean {
        val normalized = motorCategory.trim()
        return if (selectedCategory.equals(GENERAL_OTHER, ignoreCase = true) ||
            selectedCategory.equals(SinglePhaseCategories.OTHER, ignoreCase = true)
        ) {
            normalized.isEmpty() ||
                normalized.equals(GENERAL_OTHER, ignoreCase = true) ||
                normalized.equals(SinglePhaseCategories.OTHER, ignoreCase = true) ||
                PRIMARY_OPTIONS.none { it.equals(normalized, ignoreCase = true) }
        } else {
            normalized.equals(selectedCategory, ignoreCase = true)
        }
    }
}

enum class SearchMotorType(val label: String) {
    ALL("All"),
    SINGLE_PHASE("Single Phase"),
    THREE_PHASE("Three Phase")
}

enum class SearchRecentlyAdded(val label: String, val durationMillis: Long?) {
    ANY_TIME("Any Time", null),
    LAST_24_HOURS("Last 24 Hours", 24L * 60L * 60L * 1000L),
    LAST_7_DAYS("Last 7 Days", 7L * 24L * 60L * 60L * 1000L),
    LAST_30_DAYS("Last 30 Days", 30L * 24L * 60L * 60L * 1000L)
}

enum class SearchSortOption(val label: String) {
    RECENTLY_ADDED("Recently Added"),
    COMPANY_NAME("Company Name"),
    HP("HP"),
    RPM("RPM")
}

enum class SearchApprovalStatus(val label: String) {
    ALL("All Statuses"),
    APPROVED("Approved"),
    PENDING("Pending"),
    REJECTED("Rejected")
}

data class NumericFilterSpec(
    val exact: String = "",
    val min: String = "",
    val max: String = ""
) {
    fun isActive(): Boolean = exact.isNotBlank() || min.isNotBlank() || max.isNotBlank()

    fun activeSummary(unit: String): String? {
        if (!isActive()) return null
        val exactClean = exact.trim()
        if (exactClean.isNotEmpty()) return "$unit: $exactClean"
        val minClean = min.trim()
        val maxClean = max.trim()
        return when {
            minClean.isNotEmpty() && maxClean.isNotEmpty() -> "$unit: $minClean-$maxClean"
            minClean.isNotEmpty() -> "$unit >= $minClean"
            maxClean.isNotEmpty() -> "$unit <= $maxClean"
            else -> null
        }
    }
}

data class MotorSearchFilter(
    val query: String = "",
    val motorType: SearchMotorType = SearchMotorType.ALL,
    val category: String = "All",
    val hp: NumericFilterSpec = NumericFilterSpec(),
    val rpm: NumericFilterSpec = NumericFilterSpec(),
    val amps: NumericFilterSpec = NumericFilterSpec(),
    val slot: NumericFilterSpec = NumericFilterSpec(),
    val swg: String = "",
    val coilSize: String = "",
    val connection: String = "All",
    val recentlyAdded: SearchRecentlyAdded = SearchRecentlyAdded.ANY_TIME,
    val approvalStatus: SearchApprovalStatus = SearchApprovalStatus.ALL,
    val sortBy: SearchSortOption = SearchSortOption.RECENTLY_ADDED
) {
    fun activeCriteriaCount(isAdmin: Boolean): Int {
        var count = 0
        if (query.isNotBlank()) count++
        if (motorType != SearchMotorType.ALL) count++
        if (category != "All" && category.isNotBlank()) count++
        if (hp.isActive()) count++
        if (rpm.isActive()) count++
        if (amps.isActive()) count++
        if (slot.isActive()) count++
        if (swg.isNotBlank()) count++
        if (coilSize.isNotBlank()) count++
        if (connection != "All" && connection.isNotBlank()) count++
        if (recentlyAdded != SearchRecentlyAdded.ANY_TIME) count++
        if (isAdmin && approvalStatus != SearchApprovalStatus.ALL) count++
        return count
    }

    fun hasAnyActiveFilter(isAdmin: Boolean): Boolean = activeCriteriaCount(isAdmin) > 0
}

sealed class MotorSearchItem {
    abstract val id: String
    abstract val typeKey: String
    abstract val typeLabel: String
    abstract val displayName: String
    abstract val category: String
    abstract val hp: String
    abstract val rpm: String
    abstract val amps: String
    abstract val slot: String
    abstract val swgSummary: String
    abstract val swgSearchValues: List<String>
    abstract val coilSize: String
    abstract val connection: String
    abstract val photoUrls: List<String>
    abstract val status: String
    abstract val createdAt: Long
    abstract val capacitor: String

    val uniqueKey: String
        get() = "${typeKey}_$id"

    val primaryPhotoUrl: String?
        get() = photoUrls.firstOrNull { it.isNotBlank() }

    val isApproved: Boolean
        get() = status.trim().equals("approved", ignoreCase = true)

    val isPending: Boolean
        get() = status.trim().equals("pending", ignoreCase = true)

    val isRejected: Boolean
        get() = status.trim().equals("rejected", ignoreCase = true)

    data class SinglePhase(val motor: SinglePhaseMotor) : MotorSearchItem() {
        override val id: String get() = motor.id
        override val typeKey: String get() = "single"
        override val typeLabel: String get() = "Single Phase"
        override val displayName: String get() = motor.companyName
        override val category: String get() = motor.category
        override val hp: String get() = motor.hp
        override val rpm: String get() = motor.rpm
        override val amps: String get() = motor.amps
        override val slot: String get() = ""
        override val swgSummary: String
            get() = listOfNotNull(
                motor.runningSwg.trim().takeIf { it.isNotEmpty() }?.let { "R:$it" },
                motor.startingSwg.trim().takeIf { it.isNotEmpty() }?.let { "S:$it" }
            ).joinToString(" / ")
        override val swgSearchValues: List<String>
            get() = listOf(motor.runningSwg, motor.startingSwg).filter { it.isNotBlank() }
        override val coilSize: String get() = motor.coilSize
        override val connection: String get() = motor.connection
        override val photoUrls: List<String>
            get() = motor.photoUrls.ifEmpty {
                if (motor.photoUrl.isNotBlank()) listOf(motor.photoUrl) else emptyList()
            }
        override val status: String get() = motor.status
        override val createdAt: Long get() = motor.createdAt
        override val capacitor: String get() = motor.capacitor
    }

    data class ThreePhase(val motor: ThreePhaseMotor) : MotorSearchItem() {
        override val id: String get() = motor.id
        override val typeKey: String get() = "three"
        override val typeLabel: String get() = "Three Phase"
        override val displayName: String get() = motor.name
        override val category: String get() = motor.category
        override val hp: String get() = motor.hp
        override val rpm: String get() = motor.rpm
        override val amps: String get() = motor.amps
        override val slot: String get() = motor.slot
        override val swgSummary: String get() = motor.swg.trim()
        override val swgSearchValues: List<String>
            get() = listOf(motor.swg).filter { it.isNotBlank() }
        override val coilSize: String get() = motor.coilSize
        override val connection: String get() = motor.connection
        override val photoUrls: List<String>
            get() = motor.photoUrls.ifEmpty {
                if (motor.photoUrl.isNotBlank()) listOf(motor.photoUrl) else emptyList()
            }
        override val status: String get() = motor.status
        override val createdAt: Long get() = motor.createdAt
        override val capacitor: String get() = ""
    }
}

object MotorSearchEngine {
    private val MULTI_SPACE_REGEX = Regex("\\s+")
    private val NUMBER_REGEX = Regex("(?<!\\d)\\d+(?:\\.\\d+)?")
    private val SWG_WORD_REGEX = Regex("\\bswg\\b", RegexOption.IGNORE_CASE)

    fun normalizeText(input: String): String {
        return input.trim()
            .replace(",", "")
            .replace(MULTI_SPACE_REGEX, " ")
            .lowercase()
    }

    fun normalizeQueryText(input: String): String {
        return input.trim()
            .replace(MULTI_SPACE_REGEX, " ")
            .lowercase()
    }

    fun extractNumericValues(raw: String): List<Double> {
        val cleaned = raw.replace(",", "")
        return NUMBER_REGEX.findAll(cleaned)
            .mapNotNull { it.value.toDoubleOrNull() }
            .toList()
    }

    fun extractPrimaryNumber(raw: String): Double? {
        return extractNumericValues(raw).firstOrNull()
    }

    fun categoriesForMotorType(motorType: SearchMotorType): List<String> {
        return when (motorType) {
            SearchMotorType.SINGLE_PHASE -> listOf("All") + SinglePhaseCategories.ALL_OPTIONS
            SearchMotorType.THREE_PHASE -> listOf("All") + ThreePhaseCategories.ALL_OPTIONS
            SearchMotorType.ALL -> {
                val combined = (SinglePhaseCategories.ALL_OPTIONS + ThreePhaseCategories.ALL_OPTIONS)
                    .distinctBy { it.lowercase() }
                listOf("All") + combined
            }
        }
    }

    fun matchesNameQuery(displayName: String, query: String): Boolean {
        val normalizedQuery = normalizeQueryText(query)
        if (normalizedQuery.isEmpty()) return true
        val normalizedName = normalizeQueryText(displayName)
        if (normalizedName.contains(normalizedQuery)) return true
        val tokens = normalizedQuery.split(" ").filter { it.isNotBlank() }
        return tokens.isNotEmpty() && tokens.all { token -> normalizedName.contains(token) }
    }

    fun matchesNumericFilter(fieldValue: String, spec: NumericFilterSpec): Boolean {
        if (!spec.isActive()) return true
        val trimmedField = fieldValue.trim()
        if (trimmedField.isEmpty()) return false

        val fieldNumbers = extractNumericValues(trimmedField)

        val exactTrimmed = spec.exact.trim()
        if (exactTrimmed.isNotEmpty()) {
            val exactTarget = extractPrimaryNumber(exactTrimmed)
            return if (exactTarget != null && fieldNumbers.isNotEmpty()) {
                fieldNumbers.any { abs(it - exactTarget) < 0.0001 }
            } else {
                normalizeQueryText(trimmedField).contains(normalizeQueryText(exactTrimmed))
            }
        }

        val minVal = spec.min.trim().takeIf { it.isNotEmpty() }?.let { extractPrimaryNumber(it) }
        val maxVal = spec.max.trim().takeIf { it.isNotEmpty() }?.let { extractPrimaryNumber(it) }

        if (minVal == null && maxVal == null) {
            // Non-numeric text accidentally entered into min/max should fail closed rather than crash
            return false
        }
        if (fieldNumbers.isEmpty()) return false

        return fieldNumbers.any { number ->
            val minOk = minVal == null || number >= minVal - 0.0001
            val maxOk = maxVal == null || number <= maxVal + 0.0001
            minOk && maxOk
        }
    }

    fun matchesCategoryFilter(item: MotorSearchItem, selectedCategory: String): Boolean {
        if (selectedCategory.isBlank() || selectedCategory.equals("All", ignoreCase = true)) {
            return true
        }
        return when (item) {
            is MotorSearchItem.SinglePhase -> {
                SinglePhaseCategories.matchesCategory(item.category, selectedCategory)
            }
            is MotorSearchItem.ThreePhase -> {
                ThreePhaseCategories.matchesCategory(item.category, selectedCategory)
            }
        }
    }

    fun matchesSwgFilter(swgValues: List<String>, swgQuery: String): Boolean {
        val rawQuery = normalizeQueryText(swgQuery)
        if (rawQuery.isEmpty()) return true
        if (swgValues.isEmpty()) return false

        val strippedQuery = SWG_WORD_REGEX.replace(rawQuery, "").trim()
        val effectiveQuery = strippedQuery.ifEmpty { rawQuery }
        val queryNumbers = extractNumericValues(effectiveQuery)

        return swgValues.any { rawField ->
            val normalizedField = normalizeQueryText(rawField)
            val strippedField = SWG_WORD_REGEX.replace(normalizedField, "").trim()
            if (normalizedField.contains(rawQuery) || strippedField.contains(effectiveQuery)) {
                true
            } else if (queryNumbers.isNotEmpty()) {
                val fieldNumbers = extractNumericValues(strippedField)
                queryNumbers.all { qNum -> fieldNumbers.any { fNum -> abs(fNum - qNum) < 0.0001 } }
            } else {
                false
            }
        }
    }

    fun matchesCoilSizeFilter(coilSize: String, query: String): Boolean {
        val normalizedQuery = normalizeQueryText(query)
        if (normalizedQuery.isEmpty()) return true
        val normalizedField = normalizeQueryText(coilSize)
        if (normalizedField.isEmpty()) return false
        if (normalizedField.contains(normalizedQuery)) return true
        val tokens = normalizedQuery.split(" ").filter { it.isNotBlank() }
        return tokens.isNotEmpty() && tokens.all { normalizedField.contains(it) }
    }

    fun matchesRecentlyAdded(
        createdAt: Long,
        recentlyAdded: SearchRecentlyAdded,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val window = recentlyAdded.durationMillis ?: return true
        if (createdAt <= 0L) return false
        return (nowMillis - createdAt) <= window
    }

    fun buildUnifiedList(
        singlePhaseMotors: List<SinglePhaseMotor>,
        threePhaseMotors: List<ThreePhaseMotor>
    ): List<MotorSearchItem> {
        val singles = singlePhaseMotors.distinctBy { it.id }.map { MotorSearchItem.SinglePhase(it) }
        val threes = threePhaseMotors.distinctBy { it.id }.map { MotorSearchItem.ThreePhase(it) }
        return singles + threes
    }

    fun filterAndSort(
        items: List<MotorSearchItem>,
        filter: MotorSearchFilter,
        isAdmin: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): List<MotorSearchItem> {
        val filtered = items.filter { item ->
            // STRICT ROLE SECURITY GUARD: Public users NEVER see non-approved motors
            val roleAllowed = if (isAdmin) {
                when (filter.approvalStatus) {
                    SearchApprovalStatus.ALL -> true
                    SearchApprovalStatus.APPROVED -> item.isApproved
                    SearchApprovalStatus.PENDING -> item.isPending
                    SearchApprovalStatus.REJECTED -> item.isRejected
                }
            } else {
                item.isApproved
            }
            if (!roleAllowed) return@filter false

            val typeMatch = when (filter.motorType) {
                SearchMotorType.ALL -> true
                SearchMotorType.SINGLE_PHASE -> item is MotorSearchItem.SinglePhase
                SearchMotorType.THREE_PHASE -> item is MotorSearchItem.ThreePhase
            }
            if (!typeMatch) return@filter false

            if (!matchesCategoryFilter(item, filter.category)) return@filter false
            if (!matchesNameQuery(item.displayName, filter.query)) return@filter false
            if (!matchesNumericFilter(item.hp, filter.hp)) return@filter false
            if (!matchesNumericFilter(item.rpm, filter.rpm)) return@filter false
            if (!matchesNumericFilter(item.amps, filter.amps)) return@filter false
            if (!matchesNumericFilter(item.slot, filter.slot)) return@filter false
            if (!matchesSwgFilter(item.swgSearchValues, filter.swg)) return@filter false
            if (!matchesCoilSizeFilter(item.coilSize, filter.coilSize)) return@filter false
            if (!MotorConnectionOptions.matchesConnectionFilter(item.connection, filter.connection)) return@filter false
            if (!matchesRecentlyAdded(item.createdAt, filter.recentlyAdded, nowMillis)) return@filter false

            true
        }

        return when (filter.sortBy) {
            SearchSortOption.RECENTLY_ADDED -> filtered.sortedWith(
                compareByDescending<MotorSearchItem> { it.createdAt }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayName.trim() }
                    .thenBy { it.id }
            )
            SearchSortOption.COMPANY_NAME -> filtered.sortedWith(
                compareBy<MotorSearchItem> { it.displayName.isBlank() }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { normalizeQueryText(it.displayName) }
                    .thenByDescending { it.createdAt }
            )
            SearchSortOption.HP -> filtered.sortedWith(
                compareBy<MotorSearchItem> { extractPrimaryNumber(it.hp) ?: Double.MAX_VALUE }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayName.trim() }
                    .thenByDescending { it.createdAt }
            )
            SearchSortOption.RPM -> filtered.sortedWith(
                compareBy<MotorSearchItem> { extractPrimaryNumber(it.rpm) ?: Double.MAX_VALUE }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayName.trim() }
                    .thenByDescending { it.createdAt }
            )
        }
    }
}
