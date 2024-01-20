package de.westnordost.streetcomplete.screens.main.map.components

import android.content.res.Configuration
import android.content.res.Resources
import com.mapzen.tangram.SceneUpdate
import de.westnordost.streetcomplete.screens.main.map.VectorTileProvider
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.util.ktx.isApril1st
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

/** Takes care of loading the base map with the right parameters (localization, api key, night mode
 *  etc, custom scene updates, etc ...) */
class SceneMapComponent(
    private val resources: Resources,
    private val ctrl: KtMapController,
    private val vectorTileProvider: VectorTileProvider
) {
    private var sceneUpdates: MutableList<List<Pair<String, String>>> = mutableListOf()

    private var loadedSceneFilePath: String? = null
    private var loadedSceneUpdates: List<String>? = null

    var isAerialView: Boolean = false
        set(value) {
            field = value
            aerialViewChanged = true
        }
    private var aerialViewChanged: Boolean = false

    private val mutex = Mutex()

    /** Add the given scene updates. They will overwrite previous scene updates with the same keys.
     *
     *  It does NOT reload the scene, you need to call loadScene yourself to reload. Why? Because
     *  you might want to bundle scene updates before you triggere a (re)load of the scene. */
    fun addSceneUpdates(updates: List<Pair<String, String>>) {
        sceneUpdates.add(updates)
    }

    /** Remove the given scene updates */
    fun removeSceneUpdates(updates: List<Pair<String, String>>) {
        val idx = sceneUpdates.indexOfLast { updates == it }
        if (idx != -1) sceneUpdates.removeAt(idx)
    }

    /** (Re)load the scene.
     *
     *  Should be called again if the locale changed, the system font size changed, the UI mode
     *  (=night mode) changed or a custom scene update changed
     *
     *  The scene will not actually be reloaded if everything stayed the same. */
    suspend fun loadScene() = mutex.withLock {
        val sceneFilePath = getSceneFilePath()
        val sceneUpdates = getAllSceneUpdates()
        val strSceneUpdates = sceneUpdates.map { it.toString() }
        if (loadedSceneFilePath == sceneFilePath
            && loadedSceneUpdates == strSceneUpdates
            && !aerialViewChanged
        ) {
            return
        }
        ctrl.loadSceneFile(sceneFilePath, sceneUpdates)
        loadedSceneFilePath = sceneFilePath
        loadedSceneUpdates = sceneUpdates.map { it.toString() }
        aerialViewChanged = false
    }

    private fun getAllSceneUpdates(): List<SceneUpdate> =
        getBaseSceneUpdates() + sceneUpdates.flatten().map { SceneUpdate(it.first, it.second) }

    private fun getBaseSceneUpdates(): List<SceneUpdate> = listOf(
        SceneUpdate("global.language", Locale.getDefault().language),
        SceneUpdate("global.text_size_scaling", "${resources.configuration.fontScale}"),
        SceneUpdate("global.api_key", vectorTileProvider.apiKey),
        SceneUpdate("global.language_script", Locale.getDefault().script)
    )

    private fun getSceneFilePath(): String {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val april1 = if (isApril1st()) "wonky-" else ""
        val scene = april1 + when {
            isAerialView -> "scene-satellite.yaml"
            isNightMode -> "scene-dark.yaml"
            else -> "scene-light.yaml"
        }
        return "${vectorTileProvider.sceneFilePath}/$scene"
    }
}
