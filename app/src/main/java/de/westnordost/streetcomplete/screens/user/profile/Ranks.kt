package de.westnordost.streetcomplete.screens.user.profile

fun getRankProgress(rank: Int): Float =
    // 2024-05: rank 5000 is about top 50% of users (~200 edits), rank 1500 top 5% (~5000 edits)
    getRankProgress(rank, maxProgressAtRank = 1500, minProgressAtRank = 5000)

fun getLocalRankProgress(rank: Int): Float =
    // 2024-05: rank 850 is about top 50% of users (~20 edits), rank 200 top 5% (~1500 edits)
    //          in Italy, which is the top 5 country in terms of contributions
    getRankProgress(rank, maxProgressAtRank = 200, minProgressAtRank = 850)

fun getRankCurrentWeekProgress(rank: Int): Float =
    // 2024-05: rank 370 is about top 50% of users (~20 edits), rank 100 top 5% (~300 edits)
    getRankProgress(rank, maxProgressAtRank = 100, minProgressAtRank = 370)

fun getLocalRankCurrentWeekProgress(rank: Int): Float =
    // 2024-05: rank 50 is about top 50% of users (~20 edits), rank 10 top 10% (~250 edits)
    //          in Italy, which is the top 5 country in terms of contributions
    getRankProgress(rank, maxProgressAtRank = 10, minProgressAtRank = 50)

/** Translate the user's actual rank to a value from 0 (bad) to 1 (the best) */
private fun getRankProgress(rank: Int, maxProgressAtRank: Int, minProgressAtRank: Int): Float =
    ((minProgressAtRank - rank).toFloat() / (minProgressAtRank - maxProgressAtRank))
        .coerceIn(0f, 1f)
