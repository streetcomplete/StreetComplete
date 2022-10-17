package de.westnordost.streetcomplete.data.urlconfig

import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.overlays.Overlay
import java.net.URLDecoder
import java.net.URLEncoder

data class UrlConfig(
    val presetName: String,
    val questTypes: Collection<QuestType>,
    val questTypeOrders: List<Pair<QuestType, QuestType>>,
    val overlay: Overlay?,
)

private const val DOMAIN_NAME = "https://streetcomplete.app/"

private const val PARAM_NAME = "n"
private const val PARAM_QUESTS = "q"
private const val PARAM_OVERLAY = "o"
private const val PARAM_SORT_QUESTS = "s"

private const val ORDINAL_RADIX = 36

fun parseConfigUrl(
    url: String,
    questTypeRegistry: QuestTypeRegistry,
    overlayRegistry: OverlayRegistry
): UrlConfig? {
    val prefix = "$DOMAIN_NAME?"
    if (!url.startsWith(prefix, ignoreCase = true)) return null

    val parameters: Map<String, String> = url
        .substring(prefix.length)
        .split('&')
        .associate {
            val keyValue = it.split('=')
            if (keyValue.size != 2) return null
            keyValue[0].lowercase() to keyValue[1]
        }

    val name = parameters[PARAM_NAME]?.let { URLDecoder.decode(it, "UTF-8") } ?: return null

    val questTypesString = parameters[PARAM_QUESTS] ?: return null
    val questTypes = stringToQuestTypes(questTypesString, questTypeRegistry) ?: return null

    val overlayOrdinal = parameters[PARAM_OVERLAY]?.toIntOrNull(ORDINAL_RADIX)
    val overlay = overlayOrdinal?.let { overlayRegistry.getByOrdinal(it) }

    val questTypeOrders = parameters[PARAM_SORT_QUESTS]
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
    val name = urlConfig.presetName
    val shortenedName = if (name.length > 40) name.substring(0, 39) + "…" else name

    val parameters = mutableListOf(
        PARAM_NAME to URLEncoder.encode(shortenedName, "UTF-8"),
        PARAM_QUESTS to questTypesToString(urlConfig.questTypes, questTypeRegistry)
    )
    // TODO limit quest type orders length?!
    if (urlConfig.questTypeOrders.isNotEmpty()) {
        val sortOrders = urlConfig.questTypeOrders.mapNotNull { (first, second) ->
            val ordinal1 = questTypeRegistry.getOrdinalOf(first)?.toString(ORDINAL_RADIX)
            val ordinal2 = questTypeRegistry.getOrdinalOf(second)?.toString(ORDINAL_RADIX)
            if (ordinal1 != null && ordinal2 != null) ordinal1 to ordinal2 else null
        }.joinToString("-") { (first, second) -> "${first}.${second}" }

        parameters.add(PARAM_SORT_QUESTS to sortOrders)
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
    return "$DOMAIN_NAME?$parameterString"
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
