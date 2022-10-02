package de.westnordost.streetcomplete.overlays.shops

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.widget.doAfterTextChanged
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.FragmentOverlayShopsBinding
import de.westnordost.streetcomplete.osm.IS_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.replaceShop
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.getLocalesForFeatureDictionary
import de.westnordost.streetcomplete.util.getLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog

class ShopsOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_shops
    private val binding by contentViewBinding(FragmentOverlayShopsBinding::bind)

    private lateinit var featureCtrl: FeatureViewController

    private var feature: Feature? = null
    private var name: String? = null

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_shop_gone_vacant_answer) { setVacant() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val element = element
        feature = element?.let {
            if (IS_DISUSED_SHOP_EXPRESSION.matches(element)) {
                createVacantShop(requireContext().resources)
            } else {
                featureDictionary
                    .byTags(element.tags)
                    .forLocale(*getLocalesForFeatureDictionary(resources.configuration))
                    .forGeometry(element.geometryType)
                    .inCountry(countryOrSubdivisionCode)
                    .find()
                    .firstOrNull()
                // if not found anything in the iD presets, it's a shop type unknown to iD presets
                ?: DummyFeature(
                    "shop/unknown",
                    requireContext().getString(R.string.unknown_shop_title),
                    "maki-shop",
                    element.tags
                )
            }
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

        binding.nameInput.setText(name)
        binding.nameInput.doAfterTextChanged { checkIsFormComplete() }

        updateNameInputVisibility()
    }

    private fun filterOnlyShops(feature: Feature): Boolean {
        val fakeElement = Node(-1L, LatLon(0.0, 0.0), feature.tags, 0)
        return IS_SHOP_OR_DISUSED_SHOP_EXPRESSION.matches(fakeElement)
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        binding.nameInput.setText(feature.addTags?.get("name"))

        updateNameInputVisibility()
        checkIsFormComplete()
    }

    private fun setVacant() {
        onSelectedFeature(createVacantShop(requireContext().resources))
    }

    private fun updateNameInputVisibility() {
        val selectedFeature = featureCtrl.feature
        /* the name input is only visible if the place is not vacant, if a feature has been selected
           and if that feature doesn't already set a name (i.e. is a brand)
         */
        binding.nameInputContainer.isInvisible =
            selectedFeature == null ||
            selectedFeature.isSuggestion && selectedFeature.addTags?.get("name") != null ||
            selectedFeature.id == "shop/vacant"
    }

    override fun hasChanges(): Boolean =
        feature != featureCtrl.feature || name != binding.nameInput.nonBlankTextOrNull

    override fun isFormComplete(): Boolean =
        featureCtrl.feature != null // name is not necessary

    override fun onClickOk() {
        val tagChanges = StringMapChangesBuilder(element?.tags ?: emptyMap())

        /* always replace the feature, even if just the name changed, because it could still be
           e.g. an amenity=cafe (no change) but a different owner, so a completely different
           café,
           EXCEPT if just the name was added and before there was not any name. Then, it is very
           likely that it is the same shop, only before the info was incomplete */
        val newName = binding.nameInput.nonBlankTextOrNull
        if (name == null && newName != null && featureCtrl.feature == feature) {
            tagChanges["name"] = newName
        } else {
            tagChanges.replaceShop(featureCtrl.feature!!.addTags)
            if (newName != null) tagChanges["name"] = newName
        }

        if (element != null) {
            applyEdit(UpdateElementTagsAction(tagChanges.create()))
        } else {
            applyEdit(CreateNodeAction(geometry.center, tagChanges))
        }
    }
}

private fun createVacantShop(resources: Resources) = DummyFeature(
    "shop/vacant",
    resources.getString(R.string.vacant_shop_title),
    "maki-shop",
    mapOf("disused:shop" to "yes")
)

// TODO update "KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED"? https://github.com/mnalis/StreetComplete-taginfo-categorize/issues/11

// TODO add "no name" option (analogous to AddPlaceName quest)?
// TODO add multi-language-name feature (analogous to AddPlaceName quest)?
// TODO not always replace all tags? -> ask user each time instead?
