package de.westnordost.streetcomplete.osm

sealed class Level
data class SingleLevel(val level: Double) : Level()
data class LevelRange(val start: Double, val end: Double) : Level()

private val levelRegex = Regex("([+-]?\\d+(?:\\.\\d+)?)(?:-([+-]?\\d+(?:\\.\\d+)?))?")

fun String.toLevelOrNull(): Level? {
    val matchResult = levelRegex.matchEntire(this) ?: return null
    val level1 = matchResult.groupValues[1].toDoubleOrNull() ?: return null
    val level2 = matchResult.groupValues[2].toDoubleOrNull()
    return when {
        level2 == null -> SingleLevel(level1)
        level1 < level2 -> LevelRange(level1, level2)
        else -> LevelRange(level2, level1)
    }
}

/** Parse a level-string. A level-string could be any of the following format:
 *  1 or -1 or +1 or 0-5 or 0;1;-1 or -2;0-2 or even 1--1 */
fun String.toLevelsOrNull(): List<Level>? =
    split(';').mapNotNull { it.toLevelOrNull() }.takeIf { it.isNotEmpty() }

fun Level.intersects(other: Level): Boolean = when (this) {
    is SingleLevel -> {
        when(other) {
            is SingleLevel -> level == other.level
            is LevelRange -> level in other.start..other.end
        }
    }
    is LevelRange -> {
        when(other) {
            is SingleLevel -> other.level in start..end
            is LevelRange -> start <= other.end && end >= other.start
        }
    }
}

fun List<Level>?.levelsIntersect(other: List<Level>?): Boolean {
    if (this == null && other == null) return true
    if (this == null) return false
    if (other == null) return false

    return any { level ->
        other.any { otherLevel ->
            level.intersects(otherLevel)
        }
    }
}
