package de.westnordost.streetcomplete.quests.shop_type

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.databinding.DialogShopGoneBinding
import de.westnordost.streetcomplete.databinding.ViewShopTypeBinding
import de.westnordost.streetcomplete.osm.POPULAR_PLACE_FEATURE_IDS
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.util.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.view.controller.FeatureViewController
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog

class ShopGoneDialog(
    context: Context,
    private val element: Element,
    private val countryCode: String?,
    private val featureDictionary: FeatureDictionary,
    private val onSelectedFeatureFn: (Feature) -> Unit,
    private val onLeaveNoteFn: () -> Unit
) : AlertDialog(context) {

    private val binding: ViewShopTypeBinding
    private val radioButtons: List<RadioButton>
    private val featureCtrl: FeatureViewController
    private var selectedRadioButtonId: Int = 0

    init {
        val dialogBinding = DialogShopGoneBinding.inflate(LayoutInflater.from(context))
        binding = dialogBinding.viewShopTypeLayout

        radioButtons = listOf(binding.vacantRadioButton, binding.replaceRadioButton, binding.leaveNoteRadioButton)
        for (radioButton in radioButtons) {
            radioButton.setOnClickListener { selectRadioButton(it) }
        }

        featureCtrl = FeatureViewController(featureDictionary, binding.featureView.textView, binding.featureView.iconView)
        featureCtrl.countryOrSubdivisionCode = countryCode

        binding.featureView.root.background = null
        binding.featureContainer.setOnClickListener {
            selectRadioButton(binding.replaceRadioButton)

            SearchFeaturesDialog(
                context,
                featureDictionary,
                element.geometryType,
                countryCode,
                featureCtrl.feature?.name,
                { it.toElement().isPlace() },
                ::onSelectedFeature,
                POPULAR_PLACE_FEATURE_IDS,
                true
            ).show()
        }

        setButton(
            BUTTON_POSITIVE,
            context.resources.getText(android.R.string.ok),
            null as DialogInterface.OnClickListener?
        )

        setTitle(context.getString(R.string.quest_shop_gone_title))
        setView(dialogBinding.root)

        updateOkButtonEnablement()
    }

    private fun onSelectedFeature(feature: Feature) {
        featureCtrl.feature = feature
        updateOkButtonEnablement()
    }

    override fun show() {
        super.show()
        // to override the default OK=dismiss() behavior
        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            when (selectedRadioButtonId) {
                R.id.vacantRadioButton -> {
                    val languages = getLanguagesForFeatureDictionary(context.resources.configuration)
                    val vacantShop = featureDictionary
                        .getByTags(element.tags)
                        .firstOrNull { it.toElement().isPlace() }
                        ?.toPrefixedFeature("disused")
                        ?: featureDictionary.getById("shop/vacant", languages)!!
                    onSelectedFeatureFn(vacantShop)
                }
                R.id.replaceRadioButton -> {
                    onSelectedFeatureFn(featureCtrl.feature!!)
                }
                R.id.leaveNoteRadioButton -> {
                    onLeaveNoteFn()
                }
            }
            dismiss()
        }
    }

    private fun updateOkButtonEnablement() {
        getButton(BUTTON_POSITIVE)?.isEnabled =
            when (selectedRadioButtonId) {
                R.id.vacantRadioButton,
                R.id.leaveNoteRadioButton -> true
                R.id.replaceRadioButton ->   featureCtrl.feature != null
                else ->                      false
            }
    }

    private fun selectRadioButton(radioButton: View) {
        selectedRadioButtonId = radioButton.id
        for (b in radioButtons) {
            b.isChecked = selectedRadioButtonId == b.id
        }
        updateOkButtonEnablement()
    }
}
