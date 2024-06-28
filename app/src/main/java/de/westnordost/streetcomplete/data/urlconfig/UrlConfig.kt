package de.westnordost.streetcomplete.data.urlconfig

import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.overlays.Overlay
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLQueryComponent

data class UrlConfig(
    val presetName: String?,
    val questTypes: Collection<QuestType>,
    val questTypeOrders: List<Pair<QuestType, QuestType>>,
    val overlay: Overlay?,
)

private const val URL = "https://streetcomplete.app/s"
private const val URL2 = "streetcomplete://s"

private const val PARAM_NAME = "n"
private const val PARAM_QUESTS = "q"
private const val PARAM_OVERLAY = "o"
private const val PARAM_QUEST_ORDER = "qo"
private const val PARAM_OVERLAY_MAX_AGE_IN_DAYS = "od"

private const val ORDINAL_RADIX = 36

fun parseConfigUrl(
    url: String,
    questTypeRegistry: QuestTypeRegistry,
    overlayRegistry: OverlayRegistry
): UrlConfig? {
    val length = when {
        url.startsWith("$URL?", ignoreCase = true) -> URL.length + 1
        url.startsWith("$URL2?", ignoreCase = true) -> URL2.length + 1
        else -> return null
    }

    val parameters: Map<String, String> = url
        .substring(length)
        .split('&')
        .associate {
            val keyValue = it.split('=')
            if (keyValue.size != 2) return null
            keyValue[0].lowercase() to keyValue[1]
        }

    val name = parameters[PARAM_NAME]?.decodeURLQueryComponent(plusIsSpace = true)

    val questTypesString = parameters[PARAM_QUESTS] ?: return null
    val questTypes = stringToQuestTypes(questTypesString, questTypeRegistry) ?: return null

    val overlayOrdinal = parameters[PARAM_OVERLAY]?.toIntOrNull(ORDINAL_RADIX)
    val overlay = overlayOrdinal?.let { overlayRegistry.getByOrdinal(it) }

    val questTypeOrders = parameters[PARAM_QUEST_ORDER]
        ?.split('-')
        ?.mapNotNull {
            val pair = it.split('.')
            if (pair.size != 2) return null
            val firstOrdinal = pair[0].toIntOrNull(ORDINAL_RADIX) ?: return null
            val secondOrdinal = pair[1].toIntOrNull(ORDINAL_RADIX) ?: return null
            if (firstOrdinal == secondOrdinal) return null
            val first = questTypeRegistry.getByOrdinal(firstOrdinal)
            val second = questTypeRegistry.getByOrdinal(secondOrdinal)
            if (first != null && second != null) first to second else null
        }.orEmpty()

    return UrlConfig(name, questTypes, questTypeOrders, overlay)
}

fun createConfigUrl(
    urlConfig: UrlConfig,
    questTypeRegistry: QuestTypeRegistry,
    overlayRegistry: OverlayRegistry
): String {
    val parameters = mutableListOf<Pair<String, String>>()

    val name = urlConfig.presetName
    if (name != null) {
        val shortenedName = if (name.length > 60) name.substring(0, 57) + "..." else name
        parameters.add(PARAM_NAME to shortenedName.encodeURLQueryComponent(spaceToPlus = true))
    }
    parameters.add(PARAM_QUESTS to questTypesToString(urlConfig.questTypes, questTypeRegistry))

    // Limiting to 100 quest type reorderings and omitting them completely if that limit is exceeded
    // Reading the QR code that long becomes more and more difficult the bigger it gets, the limit
    // needs to be somewhere and 100 reorderings are quite a lofty limit anyway
    val questTypeOrders = urlConfig.questTypeOrders
    if (questTypeOrders.isNotEmpty() && questTypeOrders.size <= 100) {
        val sortOrders = urlConfig.questTypeOrders.mapNotNull { (first, second) ->
            val ordinal1 = questTypeRegistry.getOrdinalOf(first)?.toString(ORDINAL_RADIX)
            val ordinal2 = questTypeRegistry.getOrdinalOf(second)?.toString(ORDINAL_RADIX)
            if (ordinal1 != null && ordinal2 != null) ordinal1 to ordinal2 else null
        }.joinToString("-") { (first, second) -> "$first.$second" }

        parameters.add(PARAM_QUEST_ORDER to sortOrders)
    }
    if (urlConfig.overlay != null) {
        val ordinal = overlayRegistry.getOrdinalOf(urlConfig.overlay)?.toString(ORDINAL_RADIX)
        if (ordinal != null) {
            parameters.add(PARAM_OVERLAY to ordinal)
        }
    }
    val parameterString = parameters.joinToString("&") { (key, value) ->
        "$key=$value"
    }
    return "$URL?$parameterString"
}

private fun questTypesToString(
    questTypes: Collection<QuestType>,
    questTypeRegistry: QuestTypeRegistry,
): String =
    Ordinals(questTypes.mapNotNull { questTypeRegistry.getOrdinalOf(it) }.toSet())
        .toBooleanArray()
        .toBigInteger()
        .toString(ORDINAL_RADIX)

private fun stringToQuestTypes(
    string: String,
    questTypeRegistry: QuestTypeRegistry,
): Collection<QuestType>? =
    string.toBigIntegerOrNull(ORDINAL_RADIX)
        ?.toBooleanArray()
        ?.toOrdinals()
        ?.mapNotNull { questTypeRegistry.getByOrdinal(it) }
