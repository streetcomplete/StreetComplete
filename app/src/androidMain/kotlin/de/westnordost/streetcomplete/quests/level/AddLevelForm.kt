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
import de.westnordost.streetcomplete.osm.level.Level
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.osm.level.parseLevelsOrNull
import de.westnordost.streetcomplete.osm.level.parseSelectableLevels
import de.westnordost.streetcomplete.screens.main.map.Marker
import de.westnordost.streetcomplete.screens.main.map.getIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.ktx.toShortString
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun AddLevelForm(
    onAnswer: (String) -> Unit,
    filterPredicate: (element: Element) -> Boolean,
    geometry: ElementGeometry,
    mapDataSource: MapDataWithEditsSource = koinInject(),
    featureDictionary: Lazy<FeatureDictionary> = koinInject(named("FeatureDictionaryLazy")),
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

    // TODO compose-quest-form this is not called anywhere actually
    fun getMarkers(lvl: Double?): List<Marker> {
        if (lvl == null) return emptyList()
        val levels = listOf(Level.Single(lvl))
        return elementsAndGeometry.mapNotNull { (element, geometry) ->
            if (!parseLevelsOrNull(element.tags).levelsIntersect(levels)) return@mapNotNull null
            val icon = getIcon(featureDictionary.value, element)
            val title = getTitle(element.tags)
            Marker(geometry, icon, title)
        }
    }

    QuestForm(
        isComplete = level != null,
        onClickOk = { onAnswer(level!!.toShortString()) }
    ) {
        LevelForm(
            level = level,
            onLevelChange = { level = it },
            selectableLevels = selectableLevels,
        )
    }
}
