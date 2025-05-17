package de.westnordost.streetcomplete.osm.level

/** get for which level(s) the element with the given tags is defined, if any.
 *  repeat_on is interpreted the same way as level */
fun parseLevelsOrNull(tags: Map<String, String>): List<Level>? {
    val levels = tags["level"]?.toLevelsOrNull()
    val repeatOns = tags["repeat_on"]?.toLevelsOrNull()
    return if (levels == null) {
        if (repeatOns == null) null else repeatOns
    } else {
        if (repeatOns == null) levels else levels + repeatOns
    }
}

/** get levels that would appear on level filter buttons like in JOSM for the elements with the
 *  given tags */
fun parseSelectableLevels(tagsList: Iterable<Map<String, String>>): List<Double> {
    val allLevels = mutableSetOf<Double>()
    for (tags in tagsList) {
        val levels = parseLevelsOrNull(tags) ?: continue
        for (level in levels) {
            when (level) {
                is LevelRange -> allLevels.addAll(level.getSelectableLevels())
                is SingleLevel -> allLevels.add(level.level)
            }
        }
    }
    return allLevels.sorted()
}

/** Parse a level-string. A level-string could be any of the following format:
 *  1 or -1 or +1 or 0-5 or 0;1;-1 or -2;0-2 or even 1--1 */
fun String.toLevelsOrNull(): List<Level>? =
    split(';').mapNotNull { it.toLevelOrNull() }.takeIf { it.isNotEmpty() }

private fun String.toLevelOrNull(): Level? {
    val matchResult = levelRegex.matchEntire(this) ?: return null
    val level1 = matchResult.groupValues[1].toDoubleOrNull() ?: return null
    val level2 = matchResult.groupValues[2].toDoubleOrNull()
    return when {
        level2 == null -> SingleLevel(level1)
        level1 < level2 -> LevelRange(level1, level2)
        else -> LevelRange(level2, level1)
    }
}

private val levelRegex = Regex("([+-]?\\d+(?:\\.\\d+)?)(?:-([+-]?\\d+(?:\\.\\d+)?))?")
