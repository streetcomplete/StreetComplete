package de.westnordost.streetcomplete.screens.main.bottom_sheet.quest

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.common.quest.MapOverlayContent
import de.westnordost.streetcomplete.ui.common.quest.Marker

/**
 * Shows a list of quest forms for an element, allowing the user to swipe left/right to switch between them.
 */
@Composable
fun QuestFormList(
    quests: List<OsmQuest>,
    element: Element,
    onDismiss: () -> Unit,
    onEdit: (quest: OsmQuest, action: ElementEditAction, onAnswered: () -> Unit) -> Unit,
    onLeaveNote: (quest: OsmQuest, noteText: String, noteImagePaths: List<String>) -> Unit,
    onHideQuest: (quest: OsmQuest) -> Unit,
    mapPosition: LatLon?,
    mapRotation: Float,
    mapTilt: Float,
    mapMetersPerDp: Double,
    onSetMapMarkers: (Iterable<Marker>?) -> Unit,
    onSetMapOverlay: (MapOverlayContent?) -> Unit,
    lastMapClick: MapClick?,
    modifier: Modifier = Modifier,
) {
    var activeQuests by remember(quests) { mutableStateOf(quests) }

    fun removeQuest(quest: OsmQuest) {
        activeQuests = activeQuests.filter { it != quest }
    }

    LaunchedEffect(activeQuests) {
        if (activeQuests.isEmpty()) {
            onDismiss()
        }
    }

    if (activeQuests.isEmpty()) return

    val pagerState = rememberPagerState(initialPage = 0) { activeQuests.size }

    LaunchedEffect(pagerState.currentPage, activeQuests) {
        if (pagerState.currentPage >= activeQuests.size) {
            pagerState.scrollToPage((activeQuests.size - 1).coerceAtLeast(0))
        } else {
            onSetMapMarkers(null)
            onSetMapOverlay(null)
        }
    }

    HorizontalPager(
        state = pagerState,
        key = { page -> if (page < activeQuests.size) activeQuests[page].type.name else page.toString() },
        modifier = modifier,
    ) { page ->
        if (page < activeQuests.size) {
            val quest = activeQuests[page]
            OsmQuestFormContainer(
                onDismiss = onDismiss,
                onEdit = { action ->
                    onEdit(quest, action) {
                        removeQuest(quest)
                    }
                },
                onLeaveNote = { noteText, noteImagePaths ->
                    onLeaveNote(quest, noteText, noteImagePaths)
                    removeQuest(quest)
                },
                onHideQuest = {
                    onHideQuest(quest)
                    removeQuest(quest)
                },
                questType = quest.type,
                element = element,
                geometry = quest.geometry,
                mapPosition = mapPosition,
                mapRotation = mapRotation,
                mapTilt = mapTilt,
                mapMetersPerDp = mapMetersPerDp,
                onSetMapMarkers = { markers ->
                    if (page == pagerState.currentPage) {
                        onSetMapMarkers(markers)
                    }
                },
                onSetMapOverlay = { overlay ->
                    if (page == pagerState.currentPage) {
                        onSetMapOverlay(overlay)
                    }
                },
                lastMapClick = lastMapClick,
            )
        }
    }
}
