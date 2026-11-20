package com.example.model

enum class UserStatusFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    REJECTED("Rejected");

    companion object {
        fun fromInitialFilter(raw: String?): UserStatusFilter {
            val normalized = raw?.trim()?.lowercase().orEmpty()
            return when (normalized) {
                "pending" -> PENDING
                "active", "approved" -> ACTIVE
                "suspended", "blocked" -> SUSPENDED
                "rejected" -> REJECTED
                else -> ALL
            }
        }
    }
}

enum class UserSortOption(val label: String) {
    NEWEST("Newest"),
    OLDEST("Oldest")
}

enum class UserAdminAction {
    APPROVE,
    REJECT,
    ACTIVATE,
    SUSPEND
}

data class PublicUserOverviewStats(
    val totalPublicUsers: Int = 0,
    val activeUsers: Int = 0,
    val pendingUsers: Int = 0,
    val suspendedUsers: Int = 0,
    val rejectedUsers: Int = 0
)

data class UserSubmissionStats(
    val totalSubmissions: Int = 0,
    val singlePhaseCount: Int = 0,
    val threePhaseCount: Int = 0,
    val approvedCount: Int = 0,
    val pendingCount: Int = 0,
    val rejectedCount: Int = 0
)

object UserManagementEngine {

    fun computeOverviewStats(users: List<User>): PublicUserOverviewStats {
        val publicUsers = users.filter { it.isPublic() }
        return PublicUserOverviewStats(
            totalPublicUsers = publicUsers.size,
            activeUsers = publicUsers.count { it.isActive() },
            pendingUsers = publicUsers.count { it.isPending() },
            suspendedUsers = publicUsers.count { it.isBlockedOrSuspended() },
            rejectedUsers = publicUsers.count { it.accountStatus.trim().equals("rejected", ignoreCase = true) }
        )
    }

    fun computeUserSubmissions(
        user: User,
        singlePhaseMotors: List<SinglePhaseMotor>,
        threePhaseMotors: List<ThreePhaseMotor>
    ): UserSubmissionStats {
        val matchedSingle = singlePhaseMotors.filter { motorMatchesUser(it.createdBy, it.createdByName, user) }
        val matchedThree = threePhaseMotors.filter { motorMatchesUser(it.createdBy, it.createdByName, user) }

        val approved = matchedSingle.count { it.isExplicitlyApproved() } +
            matchedThree.count { it.isExplicitlyApproved() }
        val pending = matchedSingle.count { it.isPending() } +
            matchedThree.count { it.isPending() }
        val rejected = matchedSingle.count { it.isRejected() } +
            matchedThree.count { it.isRejected() }

        return UserSubmissionStats(
            totalSubmissions = matchedSingle.size + matchedThree.size,
            singlePhaseCount = matchedSingle.size,
            threePhaseCount = matchedThree.size,
            approvedCount = approved,
            pendingCount = pending,
            rejectedCount = rejected
        )
    }

    fun motorMatchesUser(createdBy: String, createdByName: String, user: User): Boolean {
        if (user.id.isNotBlank() && createdBy.isNotBlank() && createdBy == user.id) {
            return true
        }
        return createdBy.isBlank() &&
            createdByName.isNotBlank() &&
            user.name.isNotBlank() &&
            createdByName.equals(user.name, ignoreCase = true)
    }

    fun filterAndSortUsers(
        users: List<User>,
        query: String,
        statusFilter: UserStatusFilter,
        sortOption: UserSortOption
    ): List<User> {
        val trimmedQuery = query.trim()
        val filtered = users.filter { user ->
            if (!user.isPublic()) return@filter false

            val matchesSearch = if (trimmedQuery.isEmpty()) {
                true
            } else {
                user.name.contains(trimmedQuery, ignoreCase = true) ||
                    user.email.contains(trimmedQuery, ignoreCase = true) ||
                    user.mobile.contains(trimmedQuery, ignoreCase = true) ||
                    user.id.contains(trimmedQuery, ignoreCase = true)
            }

            val matchesStatus = when (statusFilter) {
                UserStatusFilter.ALL -> true
                UserStatusFilter.PENDING -> user.isPending()
                UserStatusFilter.ACTIVE -> user.isActive()
                UserStatusFilter.SUSPENDED -> user.isBlockedOrSuspended()
                UserStatusFilter.REJECTED -> user.accountStatus.trim().equals("rejected", ignoreCase = true)
            }

            matchesSearch && matchesStatus
        }

        return when (sortOption) {
            UserSortOption.NEWEST -> filtered.sortedByDescending { it.createdAt }
            UserSortOption.OLDEST -> filtered.sortedBy { it.createdAt }
        }
    }

    fun allowedActionsForStatus(accountStatus: String): List<UserAdminAction> {
        return when (accountStatus.trim().lowercase()) {
            "pending" -> listOf(UserAdminAction.APPROVE, UserAdminAction.REJECT)
            "active" -> listOf(UserAdminAction.SUSPEND)
            "blocked", "suspended", "rejected" -> listOf(UserAdminAction.ACTIVATE)
            else -> listOf(UserAdminAction.ACTIVATE)
        }
    }
}
