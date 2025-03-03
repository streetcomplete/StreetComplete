package de.westnordost.streetcomplete.overlays.things

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
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
import de.westnordost.streetcomplete.databinding.FragmentOverlayThingsBinding
import de.westnordost.streetcomplete.osm.POPULAR_THING_FEATURE_IDS
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.isThing
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.util.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.getNameAndLocationSpanned
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog

class ThingsOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_things
    private val binding by contentViewBinding(FragmentOverlayThingsBinding::bind)

    private var originalFeature: Feature? = null

    private lateinit var featureCtrl: FeatureViewController

    override val otherAnswers get() = listOfNotNull(
        createDeletePoiAnswer()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalFeature = getOriginalFeature()
    }

    private fun getOriginalFeature(): Feature? {
        val element = element ?: return null

        return getFeatureDictionaryFeature(element)
            ?: getDisusedFeatureDictionaryFeature(element)
            ?: BaseFeature(
                id = "thing/unknown",
                names = listOf(requireContext().getString(R.string.unknown_object)),
                icon = "ic_preset_maki_marker_stroked",
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
        val languages = getLanguagesForFeatureDictionary(resources.configuration)
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

        // title hint label with name is a duplication, it is displayed in the UI already
        setTitleHintLabel(element?.let { getNameAndLocationSpanned(it, resources, null) })
        setMarkerIcon(R.drawable.ic_quest_dot)

        featureCtrl = FeatureViewController(featureDictionary, binding.featureTextView, binding.featureIconView)
        featureCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode
        featureCtrl.feature = originalFeature

        // editing an existing feature is disabled because unlike shops, they don't just change
        // (e.g. a photo booth rarely transforms into a fountain). If something doesn't exist, it
        // should simply be deleted

        if (element == null) {
            binding.featureView.setOnClickListener { showFeatureSelectionDialog() }
        } else {
            binding.featureDropdownButton.isGone = true
            binding.featureView.background = null
        }
    }

    private fun showFeatureSelectionDialog() {
        SearchFeaturesDialog(
            requireContext(),
            featureDictionary,
            element?.geometryType ?: GeometryType.POINT, // for new features: always POINT
            countryOrSubdivisionCode,
            featureCtrl.feature?.name,
            { it.toElement().isThing() },
            ::onSelectedFeature,
            POPULAR_THING_FEATURE_IDS
        ).show()
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        checkIsFormComplete()
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

    override fun hasChanges(): Boolean = originalFeature != featureCtrl.feature

    override fun isFormComplete(): Boolean = featureCtrl.feature != null

    override fun onClickOk() {
        if (element == null) {
            val feature = featureCtrl.feature!!
            val tags = HashMap<String, String>()
            val builder = StringMapChangesBuilder(tags)
            feature.applyTo(builder)
            builder.create().applyTo(tags)
            applyEdit(CreateNodeAction(geometry.center, tags))
        }
    }
}
