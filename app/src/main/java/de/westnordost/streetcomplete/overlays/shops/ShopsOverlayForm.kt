package de.westnordost.streetcomplete.overlays.shops

import android.os.Bundle
import android.view.View
import androidx.core.os.ConfigurationCompat
import androidx.core.widget.doAfterTextChanged
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.FragmentOverlayShopsBinding
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.replaceShop
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.util.getLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.ktx.toTypedArray
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog

class ShopsOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_shops
    private val binding by contentViewBinding(FragmentOverlayShopsBinding::bind)

    private lateinit var featureCtrl: FeatureViewController

    private var feature: Feature? = null
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val element = element
        if (element != null) {
            val locales = ConfigurationCompat.getLocales(resources.configuration).toTypedArray()

            feature = featureDictionary
                .byTags(element.tags)
                .forLocale(*locales)
                .forGeometry(element.geometryType)
                .inCountry(countryOrSubdivisionCode)
                .find()
                .firstOrNull()
        } else {
            feature = null
        }
        name = element?.tags?.get("name")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // title hint label with name is a duplication, it is displayed in the UI already
        setTitleHintLabel(element?.tags?.let { getLocationLabel(it, resources) })
        setMarkerIcon(R.drawable.ic_quest_shop)

        featureCtrl = FeatureViewController(featureDictionary, binding.featureTextView, binding.featureIconView)
        featureCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode
        featureCtrl.feature = feature

        binding.featureView.setOnClickListener {
            SearchFeaturesDialog(
                requireContext(),
                featureDictionary,
                element?.geometryType,
                countryOrSubdivisionCode,
                featureCtrl.feature?.name,
                ::filterOnlyShops,
                ::onSelectedFeature
            ).show()
        }

        updateNameInputFromFeature(feature)
        binding.nameInput.doAfterTextChanged { checkIsFormComplete() }
    }

    private fun filterOnlyShops(feature: Feature): Boolean {
        val fakeElement = Node(-1L, LatLon(0.0, 0.0), feature.tags, 0)
        return IS_SHOP_OR_DISUSED_SHOP_EXPRESSION.matches(fakeElement)
    }

    private fun onSelectedFeature(feature: Feature?) {
        featureCtrl.feature = feature
        updateNameInputFromFeature(feature)
        checkIsFormComplete()
    }

    private fun updateNameInputFromFeature(feature: Feature?) {
        val name = feature?.addTags?.get("name")
        binding.nameInput.setText(name)
        binding.nameInput.isEnabled = name == null
    }

    override fun hasChanges(): Boolean =
        feature != featureCtrl.feature || name != binding.nameInput.nonBlankTextOrNull

    override fun isFormComplete(): Boolean =
        featureCtrl.feature != null // name is not necessary

    override fun onClickOk() {
        val tagChanges = StringMapChangesBuilder(element?.tags ?: emptyMap())

        if (feature != featureCtrl.feature) {
            val tags = featureCtrl.feature!!.addTags
            tagChanges.replaceShop(tags)
        }

        val newName = binding.nameInput.nonBlankTextOrNull
        if (name != newName) {
            if (newName != null) tagChanges["name"] = newName
            else tagChanges.remove("name")
        }

        if (element != null) {
            applyEdit(UpdateElementTagsAction(tagChanges.create()))
        } else {
            applyEdit(CreateNodeAction(geometry.center, tagChanges))
        }
    }
}

// TODO need "is vacant" option
// TODO need "no name" option?
// TODO multi-language-name?
