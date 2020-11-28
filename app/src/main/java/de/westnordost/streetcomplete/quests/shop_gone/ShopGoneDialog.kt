package de.westnordost.streetcomplete.quests.shop_gone

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.osmfeatures.Match
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.dialog_shop_gone.view.*

class ShopGoneDialog(
    context: Context,
    private val geometryType: GeometryType?,
    private val countryCode: String?,
    private val featureDictionary: FeatureDictionary,
    private val onAnswered: (ShopGoneAnswer) -> Unit
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

        val adapter = SearchAdapter(context) { term -> getFeatures(term).map { it.name }}
        presetsEditText.setAdapter(adapter)
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
                R.id.vacantRadioButton -> applyAndDismiss(ShopVacant)
                R.id.replaceRadioButton -> applyReplaceFeature()
                R.id.leaveNoteRadioButton -> applyAndDismiss(LeaveNote)
            }
        }
    }

    private fun selectRadioButton(radioButton : View) {
        selectedRadioButtonId = radioButton.id
        for (b in radioButtons) {
            b.isChecked = selectedRadioButtonId == b.id
        }
    }

    private fun getSelectedFeature(): Match? {
        val input = presetsEditText.text.toString()
        // TODO canonicalize?
        return getFeatures(input).firstOrNull()?.takeIf { it.name == input }
    }

    private fun applyReplaceFeature() {
        val feature = getSelectedFeature()
        if (feature == null) {
            presetsEditText.error = context.resources.getText(R.string.quest_shop_gone_replaced_answer_error)
        } else {
            applyAndDismiss(ShopReplaced(feature.tags))
        }
    }

    private fun applyAndDismiss(answer: ShopGoneAnswer) {
        onAnswered(answer)
        dismiss()
    }

    private fun getFeatures(startsWith: String) : List<Match> {
        return featureDictionary
            .byTerm(startsWith)
            .forGeometry(geometryType)
            .inCountry(countryCode)
            .forLocale(context.resources.configuration.locale) // TODO
            .find()
    }
}
