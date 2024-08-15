package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import androidx.annotation.UiThread
import de.westnordost.streetcomplete.screens.main.map.maplibre.awaitSetStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.TransitionOptions
import java.util.Locale

/** Takes care of loading the base map with the right parameters (localization, night mode, style
 *  updates etc... */
class SceneMapComponent(
    private val context: Context,
    private val map: MapLibreMap,
) {
    /** Load the scene */
    suspend fun loadStyle(): Style {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val mapFile =
            if (isNightMode) "map_theme/streetcomplete-night.json"
            else "map_theme/streetcomplete.json"

        val styleJsonString = context.resources.assets.open(mapFile)
            .bufferedReader()
            .use { it.readText() }

        val styleBuilder = Style.Builder().fromJson(styleJsonString)
        val style = map.awaitSetStyle(styleBuilder)
        withContext(Dispatchers.Main) { updateStyle() }
        return style
    }

    /** Updates part of the style depending on the user settings:
     *  Language, animator duration scale, font scale */
    @UiThread fun updateStyle() {
        val style = map.style ?: return

        // apply global animator duration scale
        val animatorDurationScale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        style.transition = TransitionOptions(
            (300 * animatorDurationScale).toLong(),
            0,
            true
        )

        // apply localization
        val nameLayers = listOf(
            "labels-country",
            "labels-localities",
            "labels-road",
            "labels-rivers",
            "labels-streams",
        )

        val language = Locale.getDefault().language
        nameLayers.forEach { layer ->
            style.getLayerAs<SymbolLayer>(layer)?.setProperties(
                textFont(arrayOf("Roboto Regular")),
                textField(localizedName(language))
            )
        }

        // apply Android font scaling
        val textLayers = nameLayers + listOf(
            "labels-housenumbers"
        )

        textLayers.forEach { layer ->
            style.getLayerAs<SymbolLayer>(layer)?.setProperties(
                textSize(16 * context.resources.configuration.fontScale)
            )
        }
    }
}

private fun localizedName(language: String): Expression {
    // as defined in https://www.jawg.io/docs/apidocs/maps/streets-v2/
    val localizedName = listOf("name_$language", "name_ltn")
    val getLocalizedName = coalesce(*localizedName.map { get(it) }.toTypedArray())
    return switchCase(
        // localized name set and different as main name: show both
        all(toBool(getLocalizedName), neq(get("name"), getLocalizedName)),
            concat(get("name"), literal("\n"), getLocalizedName),
        // otherwise just show the name
        get("name"),
    )
}
