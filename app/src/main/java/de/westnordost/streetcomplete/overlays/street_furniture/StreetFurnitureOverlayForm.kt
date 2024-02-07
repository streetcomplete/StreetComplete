package de.westnordost.streetcomplete.overlays.street_furniture

import android.os.Bundle
import android.view.View
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.FragmentOverlayStreetFurnitureBinding
import de.westnordost.streetcomplete.osm.POPULAR_STREET_FURNITURE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.isStreetFurniture
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.util.DummyFeature
import de.westnordost.streetcomplete.util.getLocalesForFeatureDictionary
import de.westnordost.streetcomplete.util.getLocationLabel
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog

class StreetFurnitureOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_street_furniture
    private val binding by contentViewBinding(FragmentOverlayStreetFurnitureBinding::bind)

    private var originalFeature: Feature? = null

    private lateinit var featureCtrl: FeatureViewController

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
            "street_furniture/unknown",
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
        setTitleHintLabel(element?.tags?.let { getLocationLabel(it, resources) })
        setMarkerIcon(R.drawable.ic_quest_plus)

        featureCtrl = FeatureViewController(featureDictionary, binding.featureTextView, binding.featureIconView)
        featureCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode
        featureCtrl.feature = originalFeature

        binding.featureView.setOnClickListener {
            SearchFeaturesDialog(
                requireContext(),
                featureDictionary,
                element?.geometryType ?: GeometryType.POINT, // for new features: always POINT
                countryOrSubdivisionCode,
                featureCtrl.feature?.name,
                ::filterOnlyStreetFurniture,
                ::onSelectedFeature,
                POPULAR_STREET_FURNITURE_FEATURE_IDS
            ).show()
        }
    }

    private fun filterOnlyStreetFurniture(feature: Feature): Boolean {
        val fakeElement = Node(-1L, LatLon(0.0, 0.0), feature.tags, 0)
        return fakeElement.isStreetFurniture()
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        checkIsFormComplete()
    }

    override fun hasChanges(): Boolean =
        originalFeature != featureCtrl.feature

    override fun isFormComplete(): Boolean = featureCtrl.feature != null

    override fun onClickOk() {
        applyEdit(createEditAction(
            element, geometry,
            featureCtrl.feature!!, originalFeature,
        ))
    }
}

private fun createEditAction(
    element: Element?,
    geometry: ElementGeometry,
    newFeature: Feature,
    previousFeature: Feature?
): ElementEditAction {
    val tagChanges = StringMapChangesBuilder(element?.tags ?: emptyMap())

    for ((key, value) in previousFeature?.removeTags.orEmpty()) {
        tagChanges.remove(key)
    }
    for ((key, value) in newFeature.addTags) {
        tagChanges[key] = value
    }

    return if (element != null) {
        UpdateElementTagsAction(element, tagChanges.create())
    } else {
        CreateNodeAction(geometry.center, tagChanges)
    }
}
