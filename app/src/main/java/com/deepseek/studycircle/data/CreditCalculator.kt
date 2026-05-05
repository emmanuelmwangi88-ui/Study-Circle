package com.deepseek.studycircle.data

import java.text.DecimalFormat

/**
 * CreditCalculator handles the logic for credit usage and gains.
 * It serves as the central point for defining credit-related constants, 
 * calculating balances, and managing user ranks.
 */
object CreditCalculator {
    // Credit Constants
    const val DOWNLOAD_COST = 100
    const val UPLOAD_REWARD = 50
    const val TUTOR_SESSION_COST = 500
    const val PEER_HELP_REWARD = 20
    const val WELCOME_BONUS = 2000
    const val REFERRAL_BONUS = 150
    const val DAILY_LOGIN_BONUS = 10
    const val STREAK_BONUS_MULTIPLIER = 5 // Bonus per day of streak

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
    fun getAmountForType(type: TransactionType, streakDays: Int = 0): Int {
        return when (type) {
            TransactionType.DOWNLOAD -> -DOWNLOAD_COST
            TransactionType.UPLOAD -> UPLOAD_REWARD
            TransactionType.TUTOR_SESSION -> -TUTOR_SESSION_COST
            TransactionType.PEER_HELP -> PEER_HELP_REWARD
            TransactionType.SIGNUP_BONUS -> WELCOME_BONUS
            TransactionType.REFERRAL -> REFERRAL_BONUS
            TransactionType.DAILY_LOGIN -> DAILY_LOGIN_BONUS
            TransactionType.STREAK_BONUS -> streakDays * STREAK_BONUS_MULTIPLIER
            TransactionType.OTHER -> 0
        }
    }

    /**
     * Calculates the new balance after applying a transaction.
     * Returns the same balance if the transaction would result in a negative balance for costs.
     */
    fun calculateNewBalance(currentBalance: Int, type: TransactionType, customAmount: Int? = null): Int {
        val amount = customAmount ?: getAmountForType(type)
        val newBalance = currentBalance + amount
        return if (newBalance < 0) currentBalance else newBalance
    }

    /**
     * Checks if the user has enough credits for a specific cost or transaction.
     */
    fun canAfford(currentBalance: Int, type: TransactionType, customAmount: Int? = null): Boolean {
        val amount = customAmount ?: getAmountForType(type)
        if (amount >= 0) return true // It's a gain
        return currentBalance >= Math.abs(amount)
    }

    /**
     * Formats the credit amount for display (e.g., 1200 -> "1.2k")
     */
    fun formatCredits(amount: Int): String {
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
    fun getRank(credits: Int): String {
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
    fun getProgressToNextRank(credits: Int): Float {
        val thresholds = listOf(0, 500, 1000, 2000, 5000, 10000)
        
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
