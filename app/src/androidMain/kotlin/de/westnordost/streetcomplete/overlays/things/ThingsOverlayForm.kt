package de.westnordost.streetcomplete.overlays.things

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.BaseFeature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.POPULAR_THING_FEATURE_IDS
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.isThing
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.feature.FeatureIcon
import de.westnordost.streetcomplete.ui.common.feature.FeatureItem
import de.westnordost.streetcomplete.ui.common.feature.FeatureSelect
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ConfirmDeleteDialog
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.getDisusedFeature
import de.westnordost.streetcomplete.util.ktx.getFeature
import de.westnordost.streetcomplete.util.ktx.getFeatureOrDisusedFeature
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable fun ThingsOverlayForm(
    onEdit: (ElementEditAction) -> Unit,
    element: Element?,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    preferences: Preferences = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
) {
    val favKey = "ThingsOverlayForm"
    val lastPickedFeatures = remember {
        preferences.getLastPicked<String>(favKey)
            .takeFavorites(n = 5, first = 1)
            .mapNotNull { featureId ->
                featureDictionary.getById(
                    id = featureId,
                    languages = getLanguagesForFeatureDictionary(),
                    country = countryInfo.countryOrSubdivisionCode,
                )
            }
    }

    val unknownThingString = stringResource(Res.string.unknown_object)
    val disusedString = stringResource(Res.string.disused).uppercase()
    val originalFeature = remember(element) {
        if (element == null) return@remember null

        // either a regular thing-feature (or disused)
        featureDictionary.getFeatureOrDisusedFeature(
            disusedString = disusedString,
            element = element,
            country = countryInfo.countryOrSubdivisionCode,
        )?.takeIf { it.toElement().isThing() }
        // or unknown thing
        ?: BaseFeature(
            id = "thing/unknown",
            names = listOf(unknownThingString),
            icon = "preset_maki_marker_stroked",
            tags = element.tags,
            geometry = GeometryType.entries.toList()
        )
    }
    var selectedFeature by remember(originalFeature) { mutableStateOf(originalFeature) }

    var confirmDeleteNode by remember { mutableStateOf<Node?>(null) }

    OverlayForm(
        isComplete = selectedFeature != null,
        hasChanges = selectedFeature != originalFeature,
        onClickOk = {
            if (element == null) {
                val feature = selectedFeature!!
                if (!feature.isSuggestion) {
                    preferences.addLastPicked(favKey, feature.id)
                }

                val tags = HashMap<String, String>()
                val builder = StringMapChangesBuilder(tags)
                feature.applyTo(builder)
                builder.create().applyTo(tags)
                onEdit(CreateNodeAction(geometry.center, tags))
            }
        },
        label =
            // title hint label with name is a duplication, it is displayed in the UI already
            element?.let { nameAndLocationLabel(it, featureDictionary = null) },
        otherAnswers = listOfNotNull(
            if (element is Node) {
                Answer(stringResource(Res.string.quest_generic_answer_does_not_exist)) {
                    confirmDeleteNode = element
                }
            } else null
        )
    ) {
        ThingForm(
            selectedFeature = selectedFeature,
            onSelectedFeature = { selectedFeature = it },
            lastPickedFeatures = lastPickedFeatures,
            element = element,
            countryCode = countryInfo.countryOrSubdivisionCode,
            featureDictionary = featureDictionary,
            // editing an existing feature is disabled because unlike shops, they don't just change
            // (e.g. a photo booth rarely transforms into a fountain). If something doesn't exist,
            // it should simply be deleted
            isEnabled = element == null,
        )
    }

    confirmDeleteNode?.let { node ->
        ConfirmDeleteDialog(
            onDismissRequest = { confirmDeleteNode = null },
            onConfirmDelete = { onEdit(DeletePoiNodeAction(node)) },
            onLeaveNote = { composeNote(node) }
        )
    }
}

// TODO compose-quest-form need to set marker icon
// setMarkerIcon(R.drawable.quest_dot)
