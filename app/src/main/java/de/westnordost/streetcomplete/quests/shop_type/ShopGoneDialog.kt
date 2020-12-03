package de.westnordost.streetcomplete.quests.shop_type

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.os.ConfigurationCompat
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.osmfeatures.StringUtils
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.isSomeKindOfShop
import de.westnordost.streetcomplete.ktx.toTypedArray
import kotlinx.android.synthetic.main.view_shop_type.view.*

class ShopGoneDialog(
    context: Context,
    private val geometryType: GeometryType?,
    private val countryCode: String?,
    private val featureDictionary: FeatureDictionary,
    private val onSelectedFeature: (Map<String,String>) -> Unit,
    private val onLeaveNote: () -> Unit
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    private val presetsEditText: AutoCompleteTextView
    private val vacantRadioButton: RadioButton
    private val replaceRadioButton: RadioButton
    private val leaveNoteRadioButton: RadioButton
    private val radioButtons: List<RadioButton>
    private var selectedRadioButtonId: Int = 0

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_shop_gone, null)

        presetsEditText = view.presetsEditText
        vacantRadioButton = view.vacantRadioButton
        replaceRadioButton = view.replaceRadioButton
        leaveNoteRadioButton = view.leaveNoteRadioButton
        radioButtons = listOf(vacantRadioButton, replaceRadioButton, leaveNoteRadioButton)
        for (radioButton in radioButtons) {
            radioButton.setOnClickListener { selectRadioButton(it) }
        }

        presetsEditText.setAdapter(SearchAdapter(context, { term -> getFeatures(term) }, { it.name }))
        presetsEditText.setOnClickListener { selectRadioButton(replaceRadioButton) }
        presetsEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) selectRadioButton(replaceRadioButton)
        }

        setButton(
            BUTTON_POSITIVE,
            context.resources.getText(android.R.string.ok),
            null as DialogInterface.OnClickListener?
        )

        setTitle(context.getString(R.string.quest_shop_gone_title))
        setView(view)
    }

    override fun show() {
        super.show()
        // to override the default OK=dismiss() behavior
        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            when (selectedRadioButtonId) {
                R.id.vacantRadioButton -> {
                    onSelectedFeature(mapOf("shop" to "vacant"))
                    dismiss()
                }
                R.id.replaceRadioButton -> {
                    val feature = getSelectedFeature()
                    if (feature == null) {
                        presetsEditText.error = context.resources.getText(R.string.quest_shop_gone_replaced_answer_error)
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

    private fun selectRadioButton(radioButton : View) {
        selectedRadioButtonId = radioButton.id
        for (b in radioButtons) {
            b.isChecked = selectedRadioButtonId == b.id
        }
    }

    private fun getSelectedFeature(): Feature? {
        val input = presetsEditText.text.toString().trim()
        return getFeatures(input).firstOrNull()?.takeIf { it.canonicalName == StringUtils.canonicalize(input) }
    }

    private fun getFeatures(startsWith: String) : List<Feature> {
        val localeList = ConfigurationCompat.getLocales(context.resources.configuration)
        return featureDictionary
            .byTerm(startsWith)
            .forGeometry(geometryType)
            .inCountry(countryCode)
            .forLocale(*localeList.toTypedArray())
            .find()
            .filter { feature ->
                val fakeElement = OsmNode(-1L, 0, 0.0, 0.0, feature.tags)
                fakeElement.isSomeKindOfShop()
            }
    }
}
