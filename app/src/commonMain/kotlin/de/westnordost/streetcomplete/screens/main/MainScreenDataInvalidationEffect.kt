package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Keeps an open form valid as its backing quest or map element changes. */
@Composable
internal fun MainScreenDataInvalidationEffect(
    viewModel: MainBottomSheetViewModel,
    visibleQuestsSource: VisibleQuestsSource,
    mapDataSource: MapDataWithEditsSource,
) {
    val scope = rememberCoroutineScope()

    DisposableEffect(viewModel, visibleQuestsSource, mapDataSource, scope) {
        fun requestCloseIf(isStillInvalid: () -> Boolean) {
            scope.launch {
                if (isStillInvalid()) viewModel.closeBottomSheet()
            }
        }

        val questsListener = object : VisibleQuestsSource.Listener {
            override fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>) {
                val openQuestKey = viewModel.shownBottomSheet.value.openQuestKey ?: return
                if (openQuestKey in removed) {
                    requestCloseIf {
                        viewModel.shownBottomSheet.value.openQuestKey == openQuestKey
                    }
                }
            }

            override fun onInvalidated() {
                val openQuestKey = viewModel.shownBottomSheet.value.openQuestKey ?: return
                scope.launch {
                    val openQuest = withContext(Dispatchers.IO) {
                        visibleQuestsSource.get(openQuestKey)
                    }
                    if (
                        openQuest == null &&
                        viewModel.shownBottomSheet.value.openQuestKey == openQuestKey
                    ) {
                        viewModel.closeBottomSheet()
                    }
                }
            }
        }

        val mapDataListener = object : MapDataWithEditsSource.Listener {
            override fun onUpdated(
                updated: MapDataWithGeometry,
                deleted: Collection<ElementKey>,
            ) {
                val openElementKey = viewModel.shownBottomSheet.value.openElementKey ?: return
                if (openElementKey in deleted) {
                    requestCloseIf {
                        viewModel.shownBottomSheet.value.openElementKey == openElementKey
                    }
                }
            }

            override fun onReplacedForBBox(
                bbox: BoundingBox,
                mapDataWithGeometry: MapDataWithGeometry,
            ) {
                val openElementKey = viewModel.shownBottomSheet.value.openElementKey ?: return
                scope.launch {
                    val openElement = withContext(Dispatchers.IO) {
                        mapDataSource.get(openElementKey.type, openElementKey.id)
                    }
                    if (
                        openElement == null &&
                        viewModel.shownBottomSheet.value.openElementKey == openElementKey
                    ) {
                        viewModel.closeBottomSheet()
                    }
                }
            }

            override fun onCleared() {
                val openElementKey = viewModel.shownBottomSheet.value.openElementKey ?: return
                requestCloseIf {
                    viewModel.shownBottomSheet.value.openElementKey == openElementKey
                }
            }
        }

        visibleQuestsSource.addListener(questsListener)
        mapDataSource.addListener(mapDataListener)
        onDispose {
            visibleQuestsSource.removeListener(questsListener)
            mapDataSource.removeListener(mapDataListener)
        }
    }
}

private val ShownBottomSheet?.openQuestKey: QuestKey?
    get() = when (this) {
        is ShownBottomSheet.OsmNoteQuest -> quest.key
        is ShownBottomSheet.OsmQuest -> quest.key
        else -> null
    }

private val ShownBottomSheet?.openElementKey: ElementKey?
    get() = (this as? ShownBottomSheet.Overlay)?.element?.key
