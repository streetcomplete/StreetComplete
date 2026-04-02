package de.westnordost.streetcomplete.overlays.things

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.POPULAR_THING_FEATURE_IDS
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.isThing
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.ui.common.feature.FeatureIcon
import de.westnordost.streetcomplete.ui.common.feature.FeatureItem
import de.westnordost.streetcomplete.ui.common.feature.FeatureSelect
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.takeFavorites
import org.koin.android.ext.android.inject

class ThingsOverlayForm : AbstractOverlayForm() {

    private val prefs: Preferences by inject()

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private var originalFeature: Feature? = null
    private var selectedFeature: MutableState<Feature?> = mutableStateOf(null)

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

    override val otherAnswers get() = listOfNotNull(
        createDeletePoiAnswer()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalFeature = getOriginalFeature()
        selectedFeature.value = originalFeature
    }

    private fun getOriginalFeature(): Feature? {
        val element = element ?: return null

        return getFeatureDictionaryFeature(element)
            ?: getDisusedFeatureDictionaryFeature(element)
            ?: BaseFeature(
                id = "thing/unknown",
                names = listOf(requireContext().getString(R.string.unknown_object)),
                icon = "preset_maki_marker_stroked",
                tags = element.tags,
                geometry = GeometryType.entries.toList()
            )
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

    @Composable
    override fun getSubtitle(): AnnotatedString? =
        // title hint label with name is a duplication, it is displayed in the UI already
        element?.let { nameAndLocationLabel(it, featureDictionary = null) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setMarkerIcon(R.drawable.quest_dot)

        // editing an existing feature is disabled because unlike shops, they don't just change
        // (e.g. a photo booth rarely transforms into a fountain). If something doesn't exist, it
        // should simply be deleted
        val isEnabled = element == null

        binding.composeViewBase.content { Surface {
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 96.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                if (isEnabled) {
                    FeatureSelect(
                        feature = selectedFeature.value,
                        onSelectedFeature = {
                            selectedFeature.value = it
                            checkIsFormComplete()
                        },
                        featureDictionary = featureDictionary,
                        geometryType = element?.geometryType ?: GeometryType.POINT, // for new features: always POINT
                        countryCode = countryOrSubdivisionCode,
                        filterFn = { it.toElement().isThing() },
                        codesOfDefaultFeatures = POPULAR_THING_FEATURE_IDS,
                    )
                    if (lastPickedFeatures.isNotEmpty()) {
                        LastPickedChipsRow(
                            items = lastPickedFeatures,
                            onClick = {
                                selectedFeature.value = it
                                checkIsFormComplete()
                            },
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
                    selectedFeature.value?.let { item ->
                        FeatureItem(
                            feature = item,
                            featureDictionary = featureDictionary,
                            countryCode = countryOrSubdivisionCode,
                        )
                    }
                }
            }
        } }
    }

    private fun createDeletePoiAnswer(): IAnswerItem? {
        val node = element as? Node ?: return null
        return AnswerItem(R.string.quest_generic_answer_does_not_exist) { confirmDelete(node) }
    }

    private fun confirmDelete(node: Node) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.osm_element_gone_description)
            .setPositiveButton(R.string.osm_element_gone_confirmation) { _, _ -> applyEdit(DeletePoiNodeAction(node)) }
            .setNeutralButton(R.string.leave_note) { _, _ -> composeNote(node) }
            .show()
    }

    override fun hasChanges(): Boolean = originalFeature != selectedFeature.value

    override fun isFormComplete(): Boolean = selectedFeature.value != null

    override fun onClickOk() {
        if (element == null) {
            val feature = selectedFeature.value!!
            if (!feature.isSuggestion) {
                prefs.addLastPicked(this::class.simpleName!!, feature.id)
            }

            val tags = HashMap<String, String>()
            val builder = StringMapChangesBuilder(tags)
            feature.applyTo(builder)
            builder.create().applyTo(tags)
            applyEdit(CreateNodeAction(geometry.center, tags))
        }
    }
}
