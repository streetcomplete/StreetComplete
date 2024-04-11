package de.westnordost.streetcomplete.screens.user.profile

import kotlin.math.max
import kotlin.math.min

fun getScaledGlobalRank(rank: Int): Int {
    // note that global rank merges multiple people with the same score
    // in case that 1000 people made 11 edits all will have the same rank (say, 3814)
    // in case that 1000 people made 10 edits all will have the same rank (in this case - 3815)
    return getScaledRank(rank, 1000, 3800)
}

fun getScaledLocalRank(rank: Int): Int {
    // very tricky as area may have thousands of users or just few
    // lets say that being one of two active people in a given area is also praiseworthy
    return getScaledRank(rank, 10, 100)
}

/** Translate the user's actual rank to a value from 0 (bad) to 10000 (the best) */
private fun getScaledRank(rank: Int, rankEnoughForFullMarks: Int, rankEnoughToStartGrowingReward: Int): Int {
    val ranksAboveThreshold = max(rankEnoughToStartGrowingReward - rank, 0)
    return min(10000, (ranksAboveThreshold * 10000.0 / (rankEnoughToStartGrowingReward - rankEnoughForFullMarks)).toInt())
}
