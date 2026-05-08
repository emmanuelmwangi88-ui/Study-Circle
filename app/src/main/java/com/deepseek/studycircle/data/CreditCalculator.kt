package com.deepseek.studycircle.data

import java.text.DecimalFormat

/**
 * CreditCalculator handles the logic for credit usage and gains.
 * It serves as the central point for defining credit-related constants, 
 * calculating balances, and managing user ranks.
 */
object CreditCalculator {
    // Credit Constants
    const val DOWNLOAD_COST = 100L
    const val UPLOAD_REWARD = 50L
    const val TUTOR_SESSION_COST = 500L
    const val PEER_HELP_REWARD = 20L
    const val WELCOME_BONUS = 2000L
    const val REFERRAL_BONUS = 150L
    const val DAILY_LOGIN_BONUS = 10L
    const val STREAK_BONUS_MULTIPLIER = 5L // Bonus per day of streak

    enum class TransactionType(val description: String, val isExpense: Boolean) {
        DOWNLOAD("Resource Download", true),
        UPLOAD("Resource Upload", false),
        TUTOR_SESSION("Tutor Session", true),
        PEER_HELP("Peer Help Reward", false),
        SIGNUP_BONUS("Welcome Bonus", false),
        REFERRAL("Referral Bonus", false),
        DAILY_LOGIN("Daily Login", false),
        STREAK_BONUS("Streak Bonus", false),
        OTHER("Adjustment", false)
    }

    /**
     * Returns the credit change for a specific transaction type.
     * Gains are positive, costs are negative.
     */
    fun getAmountForType(type: TransactionType, streakDays: Int = 0): Long {
        return when (type) {
            TransactionType.DOWNLOAD -> -DOWNLOAD_COST
            TransactionType.UPLOAD -> UPLOAD_REWARD
            TransactionType.TUTOR_SESSION -> -TUTOR_SESSION_COST
            TransactionType.PEER_HELP -> PEER_HELP_REWARD
            TransactionType.SIGNUP_BONUS -> WELCOME_BONUS
            TransactionType.REFERRAL -> REFERRAL_BONUS
            TransactionType.DAILY_LOGIN -> DAILY_LOGIN_BONUS
            TransactionType.STREAK_BONUS -> streakDays.toLong() * STREAK_BONUS_MULTIPLIER
            TransactionType.OTHER -> 0L
        }
    }

    /**
     * Formats the credit amount for display (e.g., 1200 -> "1.2k")
     */
    fun formatCredits(amount: Long): String {
        return if (amount >= 1000) {
            val df = DecimalFormat("#.#")
            df.format(amount / 1000.0) + "k"
        } else {
            amount.toString()
        }
    }

    /**
     * Determines the user's rank based on their total credits.
     */
    fun getRank(credits: Long): String {
        return when {
            credits >= 10000 -> "Legendary Sage"
            credits >= 5000 -> "Master Scholar"
            credits >= 2000 -> "Elite Learner"
            credits >= 1000 -> "Active Academic"
            credits >= 500 -> "Steady Student"
            else -> "Aspiring Learner"
        }
    }

    /**
     * Calculates progress toward the next rank (0.0 to 1.0).
     */
    fun getProgressToNextRank(credits: Long): Float {
        val thresholds = listOf(0L, 500L, 1000L, 2000L, 5000L, 10000L)
        
        for (i in 0 until thresholds.size - 1) {
            if (credits < thresholds[i+1]) {
                val range = thresholds[i+1] - thresholds[i]
                val progress = credits - thresholds[i]
                return progress.toFloat() / range.toFloat()
            }
        }
        return 1.0f // Max rank reached
    }
}
