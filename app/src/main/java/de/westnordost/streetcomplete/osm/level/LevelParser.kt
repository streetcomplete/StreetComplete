package de.westnordost.streetcomplete.osm.level

/** get for which level(s) the element with the given tags is defined, if any.
 *  repeat_on is interpreted the same way as level */
fun parseLevelsOrNull(tags: Map<String, String>, allowed: List<LevelTypes> = defaultTypes): List<Level>? {
    val resultDelegate = lazy { mutableListOf<Level>() }
    val result by resultDelegate
    allowed.forEach {
        tags[it.tag]?.toLevelsOrNull()?.let { result.addAll(it) }
    }
    return if (resultDelegate.isInitialized()) result
    else null
}

/** get levels that would appear on level filter buttons like in JOSM for the elements with the
 *  given tags */
fun parseSelectableLevels(tagsList: Iterable<Map<String, String>>, allowed: List<LevelTypes> = defaultTypes): List<Double> {
    val allLevels = mutableSetOf<Double>()
    for (tags in tagsList) {
        val levels = parseLevelsOrNull(tags, allowed) ?: continue
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

enum class LevelTypes(val tag: String) {
    LEVEL("level"), REPEAT_ON("repeat_on"), LEVEL_REF("level:ref"), ADDR_FLOOR("addr:floor)")
}

private val defaultTypes = listOf(LevelTypes.LEVEL, LevelTypes.REPEAT_ON)
