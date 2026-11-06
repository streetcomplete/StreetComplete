package de.westnordost.streetcomplete.screens.main.bottom_sheet.quest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.ui.common.quest.LocalFormTopYCallback
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

    var formTopY by remember { mutableFloatStateOf(Float.POSITIVE_INFINITY) }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            key = { page -> if (page < activeQuests.size) activeQuests[page].type.name else page.toString() },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = true
                    shape = BottomSheetClipShape(formTopY)
                },
        ) { page ->
            if (page < activeQuests.size) {
                val quest = activeQuests[page]
                CompositionLocalProvider(
                    LocalFormTopYCallback provides { y ->
                        if (page == pagerState.currentPage) {
                            formTopY = y
                        }
                    }
                ) {
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
    }
}

private class BottomSheetClipShape(private val topYPx: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val top = if (topYPx.isFinite()) topYPx.coerceIn(0f, size.height) else 0f
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = top,
                right = size.width,
                bottom = size.height
            )
        )
    }
}
