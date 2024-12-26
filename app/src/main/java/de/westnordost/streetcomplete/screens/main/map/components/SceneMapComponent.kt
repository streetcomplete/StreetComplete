package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import androidx.annotation.UiThread
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.screens.main.map.createMapStyle
import de.westnordost.streetcomplete.screens.main.map.maplibre.awaitSetStyle
import de.westnordost.streetcomplete.screens.main.map.rasterBackground
import de.westnordost.streetcomplete.screens.main.map.themeDarkContrast
import de.westnordost.streetcomplete.screens.main.map.themeLight
import de.westnordost.streetcomplete.util.logs.Log
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
    private val prefs: Preferences,
) {
    /** Load the scene */
    suspend fun loadStyle(): Style {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val token = context.resources.assets.open("map_theme/streetcomplete.json").bufferedReader()
            .use { it.readText() }.substringAfter("?access-token=").substringBefore("\"]")
        if (BuildConfig.DEBUG) {
            // make sure created file for light theme is same as map_theme/streetcomplete.json
            // this is to avoid overlooking style updates
            val lightTheme = context.resources.assets.open("map_theme/streetcomplete.json").bufferedReader().use { it.readText() }.lines()
            val createdLightTheme = createMapStyle("StreetComplete", token, emptyList(), themeLight).lines()
            for (i in lightTheme.indices) {
                if (lightTheme[i] != createdLightTheme[i]) {
                    Log.i("SceneMapComponent", "different-o: ${lightTheme[i]}")
                    Log.i("SceneMapComponent", "different-n: ${createdLightTheme[i]}")
                }
            }
            require(lightTheme == createdLightTheme) { "Created light theme is not the same as the file in assets. Please update MapStyles or MapStyleCreator." }
        }
        val styleJsonString = when {
            prefs.prefs.getString(Prefs.THEME_BACKGROUND, "MAP") != "MAP" ->
                createMapStyle("StreetComplete-Raster", token, emptyList(), rasterBackground(prefs.prefs.getBoolean(Prefs.NO_SATELLITE_LABEL, false)), prefs.prefs.getString(Prefs.RASTER_TILE_URL, "https://server.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"))
            prefs.theme == Theme.DARK_CONTRAST -> createMapStyle("StreetComplete-Dark_Contrast", token, emptyList(), themeDarkContrast)
            isNightMode -> context.resources.assets.open("map_theme/streetcomplete-night.json").bufferedReader().use { it.readText() }
            else -> context.resources.assets.open("map_theme/streetcomplete.json").bufferedReader().use { it.readText() }
        }

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
