package de.westnordost.streetcomplete.quests.shop_type

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.core.os.ConfigurationCompat
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.StringUtils
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.ktx.geometryType
import de.westnordost.streetcomplete.ktx.isSomeKindOfShop
import de.westnordost.streetcomplete.ktx.toTypedArray
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher
import kotlinx.android.synthetic.main.view_shop_type.*

class ShopTypeForm : AbstractQuestFormAnswerFragment<ShopTypeAnswer>() {

    override val contentLayoutResId = R.layout.view_shop_type

    private lateinit var radioButtons: List<RadioButton>
    private var selectedRadioButtonId: Int = 0

    private val shopTypeText get() = presetsEditText.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioButtons = listOf(vacantRadioButton, replaceRadioButton, leaveNoteRadioButton)
        for (radioButton in radioButtons) {
            radioButton.setOnClickListener { selectRadioButton(it) }
        }
        presetsEditText.setAdapter(SearchAdapter(requireContext(), { term -> getFeatures(term) }, { it.name }))
        presetsEditText.setOnClickListener { selectRadioButton(replaceRadioButton) }
        presetsEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) selectRadioButton(replaceRadioButton)
        }
        presetsEditText.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    override fun onClickOk() {
        when(selectedRadioButtonId) {
            R.id.vacantRadioButton    -> applyAnswer(IsShopVacant)
            R.id.leaveNoteRadioButton -> composeNote()
            R.id.replaceRadioButton   -> {
                val feature = getSelectedFeature()
                if (feature == null) {
                    presetsEditText.error = context?.resources?.getText(R.string.quest_shop_gone_replaced_answer_error)
                } else {
                    applyAnswer(ShopType(feature.addTags))
                }
            }
        }
    }

    override fun isFormComplete() = when(selectedRadioButtonId) {
        R.id.vacantRadioButton    -> true
        R.id.leaveNoteRadioButton -> true
        R.id.replaceRadioButton   -> shopTypeText.isNotEmpty()
        else                      -> false
    }

    private fun selectRadioButton(radioButton : View) {
        selectedRadioButtonId = radioButton.id
        for (b in radioButtons) {
            b.isChecked = selectedRadioButtonId == b.id
        }
        checkIsFormComplete()
    }

    private fun getSelectedFeature(): Feature? {
        val input = presetsEditText.text.toString()
        return getFeatures(input).firstOrNull()?.takeIf { it.canonicalName == StringUtils.canonicalize(input) }
    }

    private fun getFeatures(startsWith: String) : List<Feature> {
        val context = context ?: return emptyList()
        val localeList = ConfigurationCompat.getLocales(context.resources.configuration)
        return featureDictionary
            .byTerm(startsWith.trim())
            .forGeometry(osmElement!!.geometryType)
            .inCountry(countryInfo.countryCode)
            .forLocale(*localeList.toTypedArray())
            .find()
            .filter { feature ->
                val fakeElement = Node(-1L, LatLon(0.0, 0.0), feature.tags, 0)
                fakeElement.isSomeKindOfShop()
            }
    }
}
