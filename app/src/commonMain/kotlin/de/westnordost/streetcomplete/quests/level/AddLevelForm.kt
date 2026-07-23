package de.westnordost.streetcomplete.quests.level

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.level.Level
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.osm.level.parseLevelsOrNull
import de.westnordost.streetcomplete.osm.level.parseSelectableLevels
import de.westnordost.streetcomplete.screens.main.map.getIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMarkersCallback
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.ktx.toShortString
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun AddLevelForm(
    on: (QuestAction<String>) -> Unit,
    filterPredicate: (element: Element) -> Boolean,
    geometry: ElementGeometry,
    mapDataSource: MapDataWithEditsSource = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
) {

    var level by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectableLevels by rememberSerializable { mutableStateOf<List<Double>>(emptyList()) }
    var elementsAndGeometry by rememberSerializable { mutableStateOf<List<Pair<Element, ElementGeometry>>>(emptyList()) }

    val position = geometry.center
    LaunchedEffect(position) {
        val bbox = position.enclosingBoundingBox(50.0)
        val mapData = withContext(Dispatchers.IO) { mapDataSource.getMapDataWithGeometry(bbox) }
        val elementsWithLevels = mapData.filter(filterPredicate)

        val elementsAndGeometry = elementsWithLevels.mapNotNull { e ->
            mapData.getGeometry(e.type, e.id)?.let { geometry -> e to geometry }
        }

        selectableLevels = parseSelectableLevels(elementsWithLevels.map { it.tags })

    }

    fun getMarkers(lvl: Double?): List<Marker> {
        if (lvl == null) return emptyList()
        val levels = listOf(Level.Single(lvl))
        return elementsAndGeometry
            .filter { (element, geometry) ->
                parseLevelsOrNull(element.tags).levelsIntersect(levels)
            }
            .map { (element, geometry) ->
                Marker(
                    geometry = geometry,
                    icon = getIcon(featureDictionary, element),
                    title = getTitle(element.tags)
                )
        }
    }

    val mapMarkersCallback = LocalMapMarkersCallback.current
    LaunchedEffect(level) {
        mapMarkersCallback?.invoke(getMarkers(level))
    }

    QuestForm(
        on = on,
        isComplete = level != null,
        onClickOk = { on(Answer(level!!.toShortString())) },
    ) {
        LevelForm(
            level = level,
            onLevelChange = { level = it },
            selectableLevels = selectableLevels,
        )
    }
}
