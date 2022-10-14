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
    val overlay: Overlay?,
)

private const val DOMAIN_NAME = "https://streetcomplete.io/"

private const val PARAM_NAME = "n"
private const val PARAM_QUESTS = "q"
private const val PARAM_OVERLAY = "o"

// TODO questTypeOrders?

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
            keyValue[0].lowercase() to URLDecoder.decode(keyValue[1], "UTF-8")
        }

    val name = parameters[PARAM_NAME] ?: return null
    val questTypesString = parameters[PARAM_QUESTS] ?: return null
    val questTypes = stringToQuestTypes(questTypesString, questTypeRegistry) ?: return null
    val overlayOrdinal = parameters[PARAM_OVERLAY]?.toIntOrNull()
    val overlay = overlayOrdinal?.let { overlayRegistry.getByOrdinal(it) }

    return UrlConfig(name, questTypes, overlay)
}

fun createConfigUrl(
    urlConfig: UrlConfig,
    questTypeRegistry: QuestTypeRegistry,
    overlayRegistry: OverlayRegistry
): String {
    // TODO limit name length?!
    val parameters = mutableListOf(
        PARAM_NAME to urlConfig.presetName,
        PARAM_QUESTS to questTypesToString(urlConfig.questTypes, questTypeRegistry)
    )
    if (urlConfig.overlay != null) {
        parameters.add(PARAM_OVERLAY to overlayRegistry.getOrdinalOf(urlConfig.overlay).toString())
    }
    val parameterString = parameters.joinToString("&") { (key, value) ->
        "$key=${URLEncoder.encode(value, "UTF-8")}"
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
        .toString(10)

private fun stringToQuestTypes(
    string: String,
    questTypeRegistry: QuestTypeRegistry,
): Collection<QuestType>? =
    string.toBigIntegerOrNull(10)
        ?.toBooleanArray()
        ?.toOrdinals()
        ?.mapNotNull { questTypeRegistry.getByOrdinal(it) }
