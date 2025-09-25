package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.overlays.ItemPairSelectOverlayForm
import de.westnordost.streetcomplete.overlays.ItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class SurfaceOverlayForm : AbstractOverlayForm() {
    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()

    private val selectableItems: List<Surface> get() = Surface.selectableValuesForWays

    private val lastPickedItems: List<SurfaceOverlayAnswer> get() =
        prefs.getLastPicked(this::class.simpleName!!)
            .mapNotNull { SurfaceOverlayAnswer.deserializeFromString(it) }

    private val lastPickedSingleSurfaces: List<Surface> get() =
        lastPickedItems
            .filterIsInstance<SingleSurface>()
            .takeFavorites(n = 5, first = 1)
            .map { it.value!! }

    private val lastPickedSegregatedSurfaces: List<Pair<Surface, Surface>> get() =
        lastPickedItems
            .filterIsInstance<SegregatedSurface>()
            .takeFavorites(n = 3, first = 1)
            .map { Pair(it.footway!!, it.cycleway!!) }

    private lateinit var originalItem: SurfaceOverlayAnswer
    private lateinit var selectedItem: MutableState<SurfaceOverlayAnswer>

    override val otherAnswers: List<IAnswerItem> get() = listOfNotNull(
        createSegregatedAnswer(),
        createConvertToStepsAnswer()
    )

    private val isBothFootAndBicycleTrafficFilter by lazy { """
        ways, relations with
          highway = footway and bicycle ~ yes|designated
          or highway = cycleway and foot ~ yes|designated
          or highway = path and foot != no and bicycle != no
    """.toElementFilterExpression() }

    private fun isBothFootAndBicycleTraffic(element: Element): Boolean =
        isBothFootAndBicycleTrafficFilter.matches(element)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tags = element!!.tags
        val originalSurface = parseSurface(tags["surface"])
        val originalCyclewaySurface = parseSurface(tags["cycleway:surface"])
        val originalFootwaySurface = parseSurface(tags["footway:surface"])
        val isSegregated =
            tags["highway"] in ALL_PATHS &&
                (tags["segregated"] == "yes" || originalCyclewaySurface != null || originalFootwaySurface != null)

        originalItem = if (isSegregated) {
            SegregatedSurface(footway = originalFootwaySurface, cycleway = originalCyclewaySurface)
        } else {
            SingleSurface(originalSurface)
        }
        selectedItem = mutableStateOf(originalItem)
    }

    private fun updateSelectedCell(cellBinding: ViewImageSelectBinding, item: Surface?) {
        cellBinding.selectTextView.isGone = item != null
        cellBinding.selectedCellView.isGone = item == null
        if (item != null) {
            ItemViewHolder(cellBinding.selectedCellView).bind(item.asItem())
        }
    }

    @Composable private fun LastPickedItemContent(item: Surface) {
        val icon = item.icon
        if (icon != null) {
            Image(painterResource(icon), stringResource(item.title), Modifier.height(32.dp))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val languages = getLanguagesForFeatureDictionary(resources.configuration)
        val footwayLabel = featureDictionary.getById("highway/footway", languages)?.name.orEmpty()
        val cyclewayLabel = featureDictionary.getById("highway/cycleway", languages)?.name.orEmpty()

        binding.composeViewBase.content { Surface {
            val item = selectedItem.value

            when (item) {
                is SingleSurface -> {
                    val lastPickedSingleSurfaces = remember { lastPickedSingleSurfaces }
                    ItemSelectOverlayForm(
                        itemsPerRow = 3,
                        items = selectableItems,
                        itemContent = { ItemContent(it) },
                        selectedItem = item.value,
                        lastPickedItems = lastPickedSingleSurfaces,
                        lastPickedItemContent = { LastPickedItemContent(it) },
                        onSelectItem = {
                            selectedItem.value = SingleSurface(it)
                            checkIsFormComplete()
                        },
                    )
                }
                is SegregatedSurface -> {
                    val lastPickedSegregatedSurfaces = remember { lastPickedSegregatedSurfaces }
                    ItemPairSelectOverlayForm(
                        itemsPerRow = 3,
                        items = selectableItems,
                        itemContent = { ItemContent(it) },
                        selectedItems = Pair(item.footway, item.cycleway),
                        lastPickedItemPair = lastPickedSegregatedSurfaces,
                        lastPickedItemPairContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                LastPickedItemContent(it.first)
                                LastPickedItemContent(it.second)
                            }
                        },
                        onSelectItem = {
                            selectedItem.value = SegregatedSurface(footway = it.first, cycleway = it.second)
                            checkIsFormComplete()
                        },
                        labels = Pair(footwayLabel, cyclewayLabel),
                    )
                }
            }
        } }

        checkIsFormComplete()
    }

    override fun isFormComplete(): Boolean =
        selectedItem.value.isComplete()

    override fun hasChanges(): Boolean =
        selectedItem.value != originalItem

    override fun onClickOk() {
        val changesBuilder = StringMapChangesBuilder(element!!.tags)
        selectedItem.value.serializeToString()?.let { prefs.addLastPicked(this::class.simpleName!!, it) }
        selectedItem.value.applyTo(changesBuilder)
        applyEdit(UpdateElementTagsAction(element!!, changesBuilder.create()))
    }

    private fun createSegregatedAnswer(): AnswerItem? =
        if (selectedItem.value is SegregatedSurface) {
            /*
                No option to switch back to single surface. Removing info about separate cycleway is
                too complicated.

                Typically it requires editing not only surface info but also an access info as it
                happens in cases where bicycle access is gone. May require also removal of
                cycleway=separate, bicycle=use_sidepath from the road.

                And in cases where there is a segregated cycleway with the same surface as footway
                then StreetComplete will anyway ask for cycleway:surface and footway:surface.

                Fortunately need for this change are really rare. Notes can be left as usual.
             */
            null
        } else if (isBothFootAndBicycleTraffic(element!!)) {
            /*
                Only where bicycle access is already present because adding bicycle access typically
                requires adding proper access tags, interconnections with roads and often also other
                geometry changes.

                In case where path is not clearly marked as carrying both foot and bicycle traffic
                mapper can leave a note
             */
            AnswerItem(R.string.overlay_path_surface_segregated) {
                selectedItem.value = SegregatedSurface(null, null)
            }
        } else {
            null
        }

    private fun createConvertToStepsAnswer(): AnswerItem? =
        if (element!!.couldBeSteps()) {
            AnswerItem(R.string.quest_generic_answer_is_actually_steps) { changeToSteps() }
        } else {
            null
        }

    private fun changeToSteps() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        tagChanges.changeToSteps()
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
