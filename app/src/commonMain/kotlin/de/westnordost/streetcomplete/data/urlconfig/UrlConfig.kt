package de.westnordost.streetcomplete.data.urlconfig

import de.westnordost.streetcomplete.data.ObjectTypeRegistry
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLQueryComponent

data class UrlConfig(
    val presetName: String?,
    val questTypes: Collection<QuestType>,
    val questTypeOrders: List<Pair<QuestType, QuestType>>,
    val overlays: Collection<Overlay>,
    val selectedOverlay: Overlay?,
)

private const val URL = "https://streetcomplete.app/s"
private const val URL2 = "streetcomplete://s"

private const val PARAM_NAME = "n"
private const val PARAM_QUESTS = "q"
private const val PARAM_OVERLAYS = "os"
private const val PARAM_SELECTED_OVERLAY = "o"
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

    val questTypes = parameters[PARAM_QUESTS]
        ?.let { ordinalsStringToObjects(it, questTypeRegistry) }
        .orEmpty()

    val overlays = parameters[PARAM_OVERLAYS]
        ?.let { ordinalsStringToObjects(it, overlayRegistry) }
        .orEmpty()

    val selectedOverlay = parameters[PARAM_SELECTED_OVERLAY]
        ?.toIntOrNull(ORDINAL_RADIX)
        ?.let { overlayRegistry.getByOrdinal(it) }

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
        }
        .orEmpty()

    return UrlConfig(name, questTypes, questTypeOrders, overlays, selectedOverlay)
}

fun createConfigUrl(
    urlConfig: UrlConfig,
    questTypeRegistry: QuestTypeRegistry,
    overlayRegistry: OverlayRegistry
): String {
    val parameters = mutableMapOf<String, String>()

    val name = urlConfig.presetName
    if (name != null) {
        val shortenedName = if (name.length > 60) name.substring(0, 57) + "..." else name
        parameters[PARAM_NAME] = shortenedName.encodeURLQueryComponent(spaceToPlus = true)
    }

    if (urlConfig.questTypes.isNotEmpty()) {
        parameters[PARAM_QUESTS] = objectsToOrdinalsString(urlConfig.questTypes, questTypeRegistry)
    }

    // Limiting to 100 quest type reorderings and omitting them completely if that limit is exceeded
    // Reading the QR code that long becomes more and more difficult the bigger it gets, the limit
    // needs to be somewhere and 100 reorderings are quite a lofty limit anyway
    val questTypeOrders = urlConfig.questTypeOrders
    if (questTypeOrders.isNotEmpty() && questTypeOrders.size <= 100) {
        val sortOrders = urlConfig.questTypeOrders
            .mapNotNull { (first, second) ->
                val ordinal1 = questTypeRegistry.getOrdinalOf(first)?.toString(ORDINAL_RADIX)
                val ordinal2 = questTypeRegistry.getOrdinalOf(second)?.toString(ORDINAL_RADIX)
                if (ordinal1 != null && ordinal2 != null) ordinal1 to ordinal2 else null
            }
            .joinToString("-") { (first, second) -> "$first.$second" }

        parameters[PARAM_QUEST_ORDER] = sortOrders
    }

    if (urlConfig.overlays.isNotEmpty()) {
        parameters[PARAM_OVERLAYS] = objectsToOrdinalsString(urlConfig.overlays, overlayRegistry)
    }

    if (urlConfig.selectedOverlay != null) {
        val ordinal = overlayRegistry.getOrdinalOf(urlConfig.selectedOverlay)?.toString(ORDINAL_RADIX)
        if (ordinal != null) {
            parameters[PARAM_SELECTED_OVERLAY] = ordinal
        }
    }
    val parameterString = parameters.entries.joinToString("&") { (key, value) ->
        "$key=$value"
    }
    return "$URL?$parameterString"
}

private fun <T> objectsToOrdinalsString(
    questTypes: Collection<T>,
    registry: ObjectTypeRegistry<T>,
): String =
    Ordinals(questTypes.mapNotNull { registry.getOrdinalOf(it) }.toSet())
        .toBooleanArray()
        .toBigInteger()
        .toString(ORDINAL_RADIX)

private fun <T> ordinalsStringToObjects(
    string: String,
    registry: ObjectTypeRegistry<T>,
): Collection<T>? =
    string.toBigIntegerOrNull(ORDINAL_RADIX)
        ?.toBooleanArray()
        ?.toOrdinals()
        ?.mapNotNull { registry.getByOrdinal(it) }
