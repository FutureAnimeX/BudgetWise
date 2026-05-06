package com.budgetwise.utils

import org.mindrot.jbcrypt.BCrypt

object SecurityUtils {
    fun hashPassword(plain: String): String = BCrypt.hashpw(plain, BCrypt.gensalt(12))
    fun verifyPassword(plain: String, hash: String): Boolean =
        runCatching { BCrypt.checkpw(plain, hash) }.getOrDefault(false)
}
