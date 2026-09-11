import re

with open("app/src/main/java/com/example/repository/AuthRepository.kt", "r") as f:
    content = f.read()

old_catch = """        } catch (e: Exception) {
            Result.failure(e)
        }"""
        
new_catch = """        } catch (e: Exception) {
            val errorCode = if (e is FirebaseAuthException) e.errorCode else "UNKNOWN"
            if (errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                Result.failure(Exception("This email is already in use. Please log in instead."))
            } else if (errorCode == "ERROR_WEAK_PASSWORD") {
                Result.failure(Exception("Password is too weak. Please use at least 6 characters."))
            } else {
                Result.failure(e)
            }
        }"""
content = content.replace(old_catch, new_catch)

with open("app/src/main/java/com/example/repository/AuthRepository.kt", "w") as f:
    f.write(content)
