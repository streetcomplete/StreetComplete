package de.westnordost.streetcomplete.quests.shop_type

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.core.os.ConfigurationCompat
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.ViewShopTypeBinding
import de.westnordost.streetcomplete.osm.IS_SHOP_EXPRESSION
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.toList
import de.westnordost.streetcomplete.view.controller.PresetSelectViewController

class ShopTypeForm : AbstractOsmQuestForm<ShopTypeAnswer>() {

    override val contentLayoutResId = R.layout.view_shop_type
    private val binding by contentViewBinding(ViewShopTypeBinding::bind)

    private lateinit var radioButtons: List<RadioButton>
    private var selectedRadioButtonId: Int = 0

    private lateinit var presetSelectCtrl: PresetSelectViewController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presetSelectCtrl = PresetSelectViewController(featureDictionary, binding.presetEditText, binding.presetText)
        presetSelectCtrl.countryOrSubdivisionCode = countryOrSubdivisionCode
        presetSelectCtrl.locales = ConfigurationCompat.getLocales(requireContext().resources.configuration).toList()
        presetSelectCtrl.geometryType = element.geometryType
        presetSelectCtrl.onInputChanged = { checkIsFormComplete() }
        presetSelectCtrl.filter = { feature ->
            val fakeElement = Node(-1L, LatLon(0.0, 0.0), feature.tags, 0)
            IS_SHOP_EXPRESSION.matches(fakeElement)
        }

        radioButtons = listOf(binding.vacantRadioButton, binding.replaceRadioButton, binding.leaveNoteRadioButton)
        for (radioButton in radioButtons) {
            radioButton.setOnClickListener { selectRadioButton(it) }
        }
        binding.presetEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) selectRadioButton(binding.replaceRadioButton)
        }
    }

    override fun onClickOk() {
        when (selectedRadioButtonId) {
            R.id.vacantRadioButton    -> applyAnswer(IsShopVacant)
            R.id.leaveNoteRadioButton -> composeNote()
            R.id.replaceRadioButton   -> applyAnswer(ShopType(presetSelectCtrl.feature!!.addTags))
        }
    }

    override fun isFormComplete() = when (selectedRadioButtonId) {
        R.id.vacantRadioButton    -> true
        R.id.leaveNoteRadioButton -> true
        R.id.replaceRadioButton   -> presetSelectCtrl.feature != null
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
