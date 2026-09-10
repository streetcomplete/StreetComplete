package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.interaction.ClickEvent
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.CameraConstraints
import org.maplibre.compose.map.MaplibreMap

/** Presents the shared map and forwards its selection events to the screen. */
@Composable
fun MainMap(
    onClickOverlayElement: (ElementKey) -> Unit,
    onClickQuest: (QuestKey) -> Unit,
    onClickEdit: (EditKey) -> Unit,
    modifier: Modifier = Modifier,
    onMapLongClick: (ClickEvent) -> ClickResult = { ClickResult.Pass },
    viewModel: MainMapViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel, onClickOverlayElement, onClickQuest, onClickEdit) {
        viewModel.clicks.collect { click ->
            when (click) {
                is MainMapClick.Element -> onClickOverlayElement(click.key)
                is MainMapClick.Quest -> onClickQuest(click.key)
                is MainMapClick.Edit -> onClickEdit(click.key)
            }
        }
    }
    MaplibreMap(
        modifier = modifier,
        state = viewModel.mapState,
        cameraConstraints = CameraConstraints(minZoom = 0.0, maxZoom = 22.0),
        interactions = MapInteractions { callbacks { longClick { onEvent(onMapLongClick) } } },
        overlay = {},
    )
}
