package de.westnordost.streetcomplete.quests.shop_type

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ViewShopTypeBinding
import de.westnordost.streetcomplete.osm.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog

class ShopTypeForm : AbstractOsmQuestForm<ShopTypeAnswer>() {

    override val contentLayoutResId = R.layout.view_shop_type
    private val binding by contentViewBinding(ViewShopTypeBinding::bind)

    private lateinit var radioButtons: List<RadioButton>
    private var selectedRadioButtonId: Int = 0
    private lateinit var featureCtrl: FeatureViewController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioButtons = listOf(binding.vacantRadioButton, binding.replaceRadioButton, binding.leaveNoteRadioButton)
        for (radioButton in radioButtons) {
            radioButton.setOnClickListener { selectRadioButton(it) }
        }

        featureCtrl = FeatureViewController(featureDictionary, binding.featureView.textView, binding.featureView.iconView)
        featureCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode

        binding.featureView.root.background = null
        binding.featureContainer.setOnClickListener {
            selectRadioButton(binding.replaceRadioButton)

            SearchFeaturesDialog(
                requireContext(),
                featureDictionary,
                element.geometryType,
                countryOrSubdivisionCode,
                featureCtrl.feature?.name,
                { it.toElement().isPlace() },
                ::onSelectedFeature,
                POPULAR_PLACE_FEATURE_IDS,
            ).show()
        }
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        checkIsFormComplete()
    }

    override fun onClickOk() {
        when (selectedRadioButtonId) {
            R.id.vacantRadioButton    -> applyAnswer(IsShopVacant)
            R.id.leaveNoteRadioButton -> composeNote()
            R.id.replaceRadioButton   -> {
                // if the shop has **some** name (that is displayed to the user), we just want to
                // update the shop, not replace it. The train of thought is:
                // 1. when the user is asked about what kind of shop <named thing> is, but doesn't
                //    see any shop by that name, he will just answer that it doesn't exist via
                //    Uh.. -> Doesn't exist.
                // 2. When on the other hand he *does* see a shop by that name, it is quite clear
                //    that it is still the same shop, so we only update it, not replace it.
                // 3. On the other hand, if the place has no name, the user will also not be able
                //    to answer whether the place is now a different one than before, so we rather
                //    replace it. (#6675)
                val hasSomeName = getNameLabel(element.tags) != null
                applyAnswer(ShopType(featureCtrl.feature!!, hasSomeName))
            }
        }
    }

    override fun isFormComplete() = when (selectedRadioButtonId) {
        R.id.vacantRadioButton,
        R.id.leaveNoteRadioButton -> true
        R.id.replaceRadioButton   -> featureCtrl.feature != null
        else                      -> false
    }

    private fun selectRadioButton(radioButton: View) {
        selectedRadioButtonId = radioButton.id
        for (b in radioButtons) {
            b.isChecked = selectedRadioButtonId == b.id
        }
        checkIsFormComplete()
    }
}
