with open("app/src/main/java/com/example/repository/AuthRepository.kt", "r") as f:
    content = f.read()

target = """                if (user.accountStatus == "blocked") {
                    auth.signOut()
                    Result.failure(Exception("Account is blocked by Admin"))
                } else {
                    Result.success(user)
                }"""

replacement = """                when (user.accountStatus) {
                    "pending" -> {
                        auth.signOut()
                        Result.failure(Exception("Account Pending Approval: Your account has been created but is waiting for Admin approval."))
                    }
                    "rejected" -> {
                        auth.signOut()
                        Result.failure(Exception("Account Not Approved: Your account registration was not approved by Admin."))
                    }
                    "suspended", "blocked" -> {
                        auth.signOut()
                        Result.failure(Exception("Account Suspended: Please contact the administrator."))
                    }
                    else -> {
                        Result.success(user)
                    }
                }"""

content = content.replace(target, replacement)
with open("app/src/main/java/com/example/repository/AuthRepository.kt", "w") as f:
    f.write(content)
