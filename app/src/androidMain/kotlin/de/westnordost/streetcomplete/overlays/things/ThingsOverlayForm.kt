package de.westnordost.streetcomplete.overlays.things

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.BaseFeature
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.POPULAR_THING_FEATURE_IDS
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.isThing
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.feature.FeatureIcon
import de.westnordost.streetcomplete.ui.common.feature.FeatureItem
import de.westnordost.streetcomplete.ui.common.feature.FeatureSelect
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ConfirmDeleteDialog
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class ThingsOverlayForm : AbstractOverlayForm() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val unknownThingString = stringResource(Res.string.unknown_object)
        val originalFeature = remember {
            element?.let { element ->
                getFeatureDictionaryFeature(element)
                ?: getDisusedFeatureDictionaryFeature(element)
                ?: BaseFeature(
                    id = "thing/unknown",
                    names = listOf(unknownThingString),
                    icon = "preset_maki_marker_stroked",
                    tags = element.tags,
                    geometry = GeometryType.entries.toList()
                )
            }
        }
        var selectedFeature by remember { mutableStateOf(originalFeature) }

        // editing an existing feature is disabled because unlike shops, they don't just change
        // (e.g. a photo booth rarely transforms into a fountain). If something doesn't exist, it
        // should simply be deleted
        val isEnabled = element == null

        var confirmDeleteNode by remember { mutableStateOf<Node?>(null) }

        OverlayForm(
            isComplete = selectedFeature != null,
            hasChanges = selectedFeature != originalFeature,
            onClickOk = {
                if (element == null) {
                    val feature = selectedFeature!!
                    if (!feature.isSuggestion) {
                        prefs.addLastPicked(this::class.simpleName!!, feature.id)
                    }

                    val tags = HashMap<String, String>()
                    val builder = StringMapChangesBuilder(tags)
                    feature.applyTo(builder)
                    builder.create().applyTo(tags)
                    applyEdit(CreateNodeAction(geometry.center, tags))
                }
            },
            label =
                // title hint label with name is a duplication, it is displayed in the UI already
                element?.let { nameAndLocationLabel(it, featureDictionary = null) },
            otherAnswers = listOfNotNull(
                (element as? Node)?.let { node ->
                    Answer(stringResource(Res.string.quest_generic_answer_does_not_exist)) {
                        confirmDeleteNode = node
                    }
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 96.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                if (isEnabled) {
                    FeatureSelect(
                        feature = selectedFeature,
                        onSelectedFeature = { selectedFeature = it },
                        featureDictionary = featureDictionary,
                        geometryType = element?.geometryType ?: GeometryType.POINT, // for new features: always POINT
                        countryCode = countryOrSubdivisionCode,
                        filterFn = { it.toElement().isThing() },
                        codesOfDefaultFeatures = POPULAR_THING_FEATURE_IDS,
                    )
                    if (lastPickedFeatures.isNotEmpty()) {
                        LastPickedChipsRow(
                            items = lastPickedFeatures,
                            onClick = { selectedFeature = it },
                            modifier = Modifier.padding(start = 48.dp, end = 56.dp),
                            itemContent = {
                                FeatureIcon(
                                    feature = it,
                                    modifier = Modifier.size(22.5.dp)
                                )
                            }
                        )
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }
                } else {
                    selectedFeature?.let { item ->
                        FeatureItem(
                            feature = item,
                            featureDictionary = featureDictionary,
                            countryCode = countryOrSubdivisionCode,
                        )
                    }
                }
            }
        }

        confirmDeleteNode?.let { node ->
            ConfirmDeleteDialog(
                onDismissRequest = { confirmDeleteNode = null },
                onConfirmDelete = { applyEdit(DeletePoiNodeAction(node)) },
                onLeaveNote = { composeNote(node) }
            )
        }
    }

    private val lastPickedFeatures: List<Feature> by lazy {
        val languages = getLanguagesForFeatureDictionary()
        prefs.getLastPicked<String>(this::class.simpleName!!)
            .takeFavorites(n = 5, first = 1)
            .mapNotNull { featureId ->
                featureDictionary.getById(
                    id = featureId,
                    languages = languages,
                    country = countryOrSubdivisionCode,
                )
            }
    }

    private fun getDisusedFeatureDictionaryFeature(element: Element): Feature? {
        val disusedElement = element.asIfItWasnt("disused") ?: return null
        val disusedFeature = getFeatureDictionaryFeature(disusedElement) ?: return null
        val disusedLabel = resources.getString(R.string.disused).uppercase()
        return disusedFeature.toPrefixedFeature("disused", disusedLabel)
    }

    private fun getFeatureDictionaryFeature(element: Element): Feature? {
        val languages = getLanguagesForFeatureDictionary()
        val geometryType = if (element.type == ElementType.NODE) null else element.geometryType

        return featureDictionary.getByTags(
            tags = element.tags,
            languages = languages,
            country = countryOrSubdivisionCode,
            geometry = geometryType
        ).firstOrNull { it.toElement().isThing() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMarkerIcon(R.drawable.quest_dot)
    }
}
