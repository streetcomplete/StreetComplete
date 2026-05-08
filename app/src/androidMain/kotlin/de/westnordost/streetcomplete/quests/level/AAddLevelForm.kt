package de.westnordost.streetcomplete.quests.level

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.osm.level.Level
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.osm.level.parseLevelsOrNull
import de.westnordost.streetcomplete.osm.level.parseSelectableLevels
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.screens.main.map.Marker
import de.westnordost.streetcomplete.screens.main.map.ShowsGeometryMarkers
import de.westnordost.streetcomplete.screens.main.map.getIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.ui.common.quest.Form
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.ktx.toShortString
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

abstract class AAddLevelForm : AbstractOsmQuestForm<String>() {

    abstract fun filter(mapData: MapDataWithGeometry): List<Element>

    private val mapDataSource: MapDataWithEditsSource by inject()

    private val showsGeometryMarkersListener: ShowsGeometryMarkers? get() =
        parentFragment as? ShowsGeometryMarkers ?: activity as? ShowsGeometryMarkers

    private var elementsAndGeometry: List<Pair<Element, ElementGeometry>> = listOf()

    private val level: MutableState<Double?> = mutableStateOf(null)
    private val selectableLevels: MutableState<List<Double>> = mutableStateOf(emptyList())

    @Composable
    override fun Content() {
        QuestForm(
            answers = Form(
                isComplete = level.value != null,
                onClickOk = { applyAnswer(level.value!!.toShortString()) }
            )
        ) {
            LevelForm(
                level = level.value,
                onLevelChange = {
                    level.value = it
                    updateMarkers(level.value)
                },
                selectableLevels = selectableLevels.value,
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleScope.launch {
            val bbox = geometry.center.enclosingBoundingBox(50.0)
            val mapData = withContext(Dispatchers.IO) { mapDataSource.getMapDataWithGeometry(bbox) }

            val elementsWithLevels = filter(mapData)

            elementsAndGeometry = elementsWithLevels.mapNotNull { e ->
                mapData.getGeometry(e.type, e.id)?.let { geometry -> e to geometry }
            }
            updateMarkers(level.value)

            selectableLevels.value = parseSelectableLevels(elementsWithLevels.map { it.tags })
        }
    }

    private fun updateMarkers(level: Double?) {
        showsGeometryMarkersListener?.clearMarkersForCurrentHighlighting()
        if (level == null) return
        val levels = listOf(Level.Single(level))
        val markers = elementsAndGeometry.mapNotNull { (element, geometry) ->
            if (!parseLevelsOrNull(element.tags).levelsIntersect(levels)) return@mapNotNull null
            val icon = getIcon(featureDictionary, element)
            val title = getTitle(element.tags)
            Marker(geometry, icon, title)
        }
        showsGeometryMarkersListener?.putMarkersForCurrentHighlighting(markers)
    }
}
