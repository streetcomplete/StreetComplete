package de.westnordost.streetcomplete.quests.shop_type

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.os.ConfigurationCompat
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.osmfeatures.StringUtils
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.DialogShopGoneBinding
import de.westnordost.streetcomplete.databinding.ViewShopTypeBinding
import de.westnordost.streetcomplete.osm.IS_SHOP_EXPRESSION
import de.westnordost.streetcomplete.util.ktx.toTypedArray

class ShopGoneDialog(
    context: Context,
    private val geometryType: GeometryType?,
    private val countryCode: String?,
    private val featureDictionary: FeatureDictionary,
    private val onSelectedFeature: (Map<String, String>) -> Unit,
    private val onLeaveNote: () -> Unit
) : AlertDialog(context) {

    private val binding: ViewShopTypeBinding

    private val radioButtons: List<RadioButton>

    private var selectedRadioButtonId: Int = 0

    init {
        val dialogBinding = DialogShopGoneBinding.inflate(LayoutInflater.from(context))
        binding = dialogBinding.viewShopTypeLayout

        radioButtons = listOf(binding.vacantRadioButton, binding.replaceRadioButton, binding.leaveNoteRadioButton)
        for (radioButton in radioButtons) {
            radioButton.setOnClickListener {
                selectRadioButton(it)
                binding.presetsEditText.error = null
            }
        }

        binding.presetsEditText.setAdapter(SearchAdapter(context, { term -> getFeatures(term) }, { it.name }))
        binding.presetsEditText.setOnClickListener { selectRadioButton(binding.replaceRadioButton) }
        binding.presetsEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) selectRadioButton(binding.replaceRadioButton)
        }

        setButton(
            BUTTON_POSITIVE,
            context.resources.getText(android.R.string.ok),
            null as DialogInterface.OnClickListener?
        )

        setTitle(context.getString(R.string.quest_shop_gone_title))
        setView(dialogBinding.root)
    }

    override fun show() {
        super.show()
        // to override the default OK=dismiss() behavior
        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            when (selectedRadioButtonId) {
                R.id.vacantRadioButton -> {
                    onSelectedFeature(mapOf("disused:shop" to "yes"))
                    dismiss()
                }
                R.id.replaceRadioButton -> {
                    val feature = getSelectedFeature()
                    if (feature == null) {
                        binding.presetsEditText.error = context.resources.getText(R.string.quest_shop_gone_replaced_answer_error2)
                    } else {
                        onSelectedFeature(feature.addTags)
                        dismiss()
                    }
                }
                R.id.leaveNoteRadioButton -> {
                    onLeaveNote()
                    dismiss()
                }
            }
        }
    }

    private fun selectRadioButton(radioButton: View) {
        selectedRadioButtonId = radioButton.id
        for (b in radioButtons) {
            b.isChecked = selectedRadioButtonId == b.id
        }
    }

    private fun getSelectedFeature(): Feature? {
        val input = binding.presetsEditText.text.toString()
        return getFeatures(input).firstOrNull { it.canonicalNames.first() == StringUtils.canonicalize(input) }
    }

    private fun getFeatures(startsWith: String): List<Feature> {
        val localeList = ConfigurationCompat.getLocales(context.resources.configuration)
        return featureDictionary
            .byTerm(startsWith.trim())
            .forGeometry(geometryType)
            .inCountry(countryCode)
            .forLocale(*localeList.toTypedArray())
            .find()
            .filter { feature ->
                val fakeElement = Node(-1L, LatLon(0.0, 0.0), feature.tags, 0)
                IS_SHOP_EXPRESSION.matches(fakeElement)
            }
    }
}
