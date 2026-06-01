package de.westnordost.streetcomplete.overlays.surface

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.ui.common.overlay.ItemPairSelectOverlayForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.overlay.ItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable fun SurfaceOverlayForm(
    on: (OverlayAction) -> Unit,
    element: Element,
    preferences: Preferences = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
) {
    val originalItem = remember(element) { parseSurfaceOverlayAnswer(element.tags) }

    var selectedItem by rememberSerializable(originalItem) { mutableStateOf(originalItem) }

    val items = Surface.selectableValuesForWays

    @Composable fun createOtherAnswers(): List<AnswerItem> {
        val result = ArrayList<AnswerItem>()

        when (selectedItem) {
            is SegregatedSurface -> {
                /*
                No option to switch back to single surface. Removing info about separate cycleway is
                too complicated.

                Typically, it requires editing not only surface info but also an access info as it
                happens in cases where bicycle access is gone. May require also removal of
                cycleway=separate, bicycle=use_sidepath from the road.

                And in cases where there is a segregated cycleway with the same surface as footway
                then StreetComplete will anyway ask for cycleway:surface and footway:surface.

                Fortunately need for this change are really rare. Notes can be left as usual.
                */
            }
            is SingleSurface -> {
                /*
                Only where bicycle access is already present because adding bicycle access typically
                requires adding proper access tags, interconnections with roads and often also other
                geometry changes.

                In case where path is not clearly marked as carrying both foot and bicycle traffic
                mapper can leave a note
                */
                if (isBothFootAndBicycleTrafficFilter.matches(element)) {
                    result.add(AnswerItem(stringResource(Res.string.overlay_path_surface_segregated)) {
                        selectedItem = SegregatedSurface(null, null)
                    })
                }
            }
        }

        if (element.couldBeSteps()) {
            result.add(AnswerItem(stringResource(Res.string.quest_generic_answer_is_actually_steps)) {
                val tagChanges = StringMapChangesBuilder(element.tags)
                tagChanges.changeToSteps()
                on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
            })
        }

        return result
    }

    when (val item = selectedItem) {
        is SingleSurface -> {
            ItemSelectOverlayForm(
                itemsPerRow = 3,
                items = items,
                initialSelectedItem = item.value,
                itemContent = { SurfaceItemContent(it) },
                lastPickedItemContent = { LastPickedSurfaceContent(it) },
                onClickOk = { selectedItem ->
                    val changesBuilder = StringMapChangesBuilder(element.tags)
                    SingleSurface(selectedItem).applyTo(changesBuilder)
                    on(Edit(UpdateElementTagsAction(element, changesBuilder.create())))
                },
                prefs = preferences,
                favoriteKey = "SurfaceOverlayForm.Single",
                on = on,
                otherAnswers = ::createOtherAnswers
            )
        }
        is SegregatedSurface -> {
            val labels = remember {
                val languages = getLanguagesForFeatureDictionary()
                val footwayLabel = featureDictionary.getById("highway/footway", languages)?.name.orEmpty()
                val cyclewayLabel = featureDictionary.getById("highway/cycleway", languages)?.name.orEmpty()
                Pair(footwayLabel, cyclewayLabel)
            }

            ItemPairSelectOverlayForm(
                itemsPerRow = 3,
                items = items,
                initialSelectedItemPair = Pair(item.footway, item.cycleway),
                itemContent =  { SurfaceItemContent(it) },
                lastPickedItemPairContent = { LastPickedSurfacePairContent(it) },
                onClickOk = { (footway, cycleway) ->
                    val changesBuilder = StringMapChangesBuilder(element.tags)
                    SegregatedSurface(footway = footway, cycleway = cycleway).applyTo(changesBuilder)
                    on(Edit(UpdateElementTagsAction(element, changesBuilder.create())))
                },
                labels = labels,
                prefs = preferences,
                favoriteKey = "SurfaceOverlayForm.Pair",
                on = on,
                otherAnswers = ::createOtherAnswers
            )
        }
    }
}

private val isBothFootAndBicycleTrafficFilter by lazy { """
    ways, relations with
      highway = footway and bicycle ~ yes|designated
      or highway = cycleway and foot ~ yes|designated
      or highway = path and foot != no and bicycle != no
""".toElementFilterExpression() }

@Composable
private fun SurfaceItemContent(item: Surface) {
    ImageWithLabel(item.icon?.let { painterResource(it) }, stringResource(item.title))
}

@Composable
private fun LastPickedSurfaceContent(item: Surface) {
    val icon = item.icon
    if (icon != null) {
        Image(painterResource(icon), stringResource(item.title), Modifier.height(32.dp))
    }
}

@Composable
private fun LastPickedSurfacePairContent(item: Pair<Surface, Surface>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        LastPickedSurfaceContent(item.first)
        LastPickedSurfaceContent(item.second)
    }
}
