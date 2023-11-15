package de.westnordost.streetcomplete.overlays.street_furniture

import android.os.Bundle
import android.view.View
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.FragmentOverlayStreetFurnitureBinding
import de.westnordost.streetcomplete.osm.IS_DISUSED_STREET_FURNITURE_EXPRESSION
import de.westnordost.streetcomplete.osm.IS_STREET_FURNITURE_INCLUDING_DISUSED_EXPRESSION
import de.westnordost.streetcomplete.osm.address.featureBehindPrefix
import de.westnordost.streetcomplete.osm.address.reconstructFeature
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.util.getLocalesForFeatureDictionary
import de.westnordost.streetcomplete.util.getLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog
import kotlinx.coroutines.launch

class StreetFurnitureOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_street_furniture
    private val binding by contentViewBinding(FragmentOverlayStreetFurnitureBinding::bind)

    private var originalFeature: Feature? = null

    private lateinit var featureCtrl: FeatureViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val element = element
        originalFeature = element?.let {
            featureDictionary
                .byTags(element.tags)
                .forLocale(*getLocalesForFeatureDictionary(resources.configuration))
                .forGeometry(element.geometryType)
                .inCountry(countryOrSubdivisionCode)
                .find()
                .firstOrNull()
                ?: if (IS_DISUSED_STREET_FURNITURE_EXPRESSION.matches(element)) {
                    reconstructFeature(requireContext(), element.tags, "disused:", featureDictionary)
                } else {
                    // if not found anything in the iD presets, then something weird happened
                    // amenity=bicycle_wash ?
                    DummyFeature(
                        "street_furniture/unknown",
                        requireContext().getString(R.string.unknown_object),
                        "ic_preset_maki_marker_stroked",
                        element.tags
                    )
                }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // title hint label with name is a duplication, it is displayed in the UI already
        setTitleHintLabel(element?.tags?.let { getLocationLabel(it, resources) })
        setMarkerIcon(R.drawable.ic_quest_apple)

        featureCtrl = FeatureViewController(featureDictionary, binding.featureTextView, binding.featureIconView)
        featureCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode
        featureCtrl.feature = originalFeature

        binding.featureView.setOnClickListener {
            SearchFeaturesDialog(
                requireContext(),
                featureDictionary,
                element?.geometryType,
                countryOrSubdivisionCode,
                featureCtrl.feature?.name,
                ::filterOnlyStreetFurniture,
                ::onSelectedFeature,
                listOf(
                    // ordered by popularity, skipping trees as there are multiple variants of them
                    "highway/street_lamp",
                    "amenity/bench",
                    "emergency/fire_hydrant",
                    "amenity/bicycle_parking",
                    "amenity/shelter",
                    "amenity/toilets",
                    // "amenity/post_box",
                    // blocked by https://github.com/streetcomplete/StreetComplete/issues/4916
                    // waiting for response in https://github.com/ideditor/schema-builder/issues/94
                    "amenity/drinking_water",
                    "leisure/picnic_table",

                    // popular, a bit less than some competing entries
                    // but interesting and worth promoting
                    "emergency/defibrillator",
                    )
            ).show()
        }
    }

    private fun filterOnlyStreetFurniture(feature: Feature): Boolean {
        val fakeElement = Node(-1L, LatLon(0.0, 0.0), feature.tags, 0)
        return IS_STREET_FURNITURE_INCLUDING_DISUSED_EXPRESSION.matches(fakeElement)
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        checkIsFormComplete()
    }

    override fun hasChanges(): Boolean =
        originalFeature != featureCtrl.feature

    override fun isFormComplete(): Boolean = featureCtrl.feature != null

    override fun onClickOk() {
        viewLifecycleScope.launch {
            applyEdit(createEditAction(
                element, geometry,
                featureCtrl.feature!!, originalFeature,
            ))
        }
    }
}

private suspend fun createEditAction(
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
