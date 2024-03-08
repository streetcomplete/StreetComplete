package de.westnordost.streetcomplete.overlays.things

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.FragmentOverlayThingsBinding
import de.westnordost.streetcomplete.osm.POPULAR_THING_FEATURE_IDS
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.isThing
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.util.DummyFeature
import de.westnordost.streetcomplete.util.getLocalesForFeatureDictionary
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
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
        val feature = getFeatureDictionaryFeature(element)
        if (feature != null) return feature

        val disusedElement = element.asIfItWasnt("disused")
        if (disusedElement != null) {
            val disusedFeature = getFeatureDictionaryFeature(disusedElement)
            if (disusedFeature != null) {
                return DummyFeature(
                    disusedFeature.id + "/disused",
                    "${disusedFeature.name} (${resources.getString(R.string.disused).uppercase()})",
                    disusedFeature.icon,
                    disusedFeature.addTags.mapKeys { "disused:${it.key}" }
                )
            }
        }

        return DummyFeature(
            "thing/unknown",
            requireContext().getString(R.string.unknown_object),
            "ic_preset_maki_marker_stroked",
            element.tags
        )
    }

    private fun getFeatureDictionaryFeature(element: Element): Feature? {
        val locales = getLocalesForFeatureDictionary(resources.configuration)
        val geometryType = if (element.type == ElementType.NODE) null else element.geometryType

        return featureDictionary
            .byTags(element.tags)
            .forLocale(*locales)
            .forGeometry(geometryType)
            .inCountry(countryOrSubdivisionCode)
            .find()
            .firstOrNull()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // title hint label with name is a duplication, it is displayed in the UI already
        setTitleHintLabel(element?.let { getNameAndLocationLabel(it, resources, null) })
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
            ::filterOnlyThings,
            ::onSelectedFeature,
            POPULAR_THING_FEATURE_IDS
        ).show()
    }

    private fun filterOnlyThings(feature: Feature): Boolean {
        val fakeElement = Node(-1L, LatLon(0.0, 0.0), feature.tags, 0)
        return fakeElement.isThing()
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
            applyEdit(CreateNodeAction(geometry.center, feature.addTags))
        }
    }
}
