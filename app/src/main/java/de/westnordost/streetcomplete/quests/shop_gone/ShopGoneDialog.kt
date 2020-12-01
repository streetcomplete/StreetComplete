package de.westnordost.streetcomplete.quests.shop_gone

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.os.ConfigurationCompat
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.osmfeatures.StringUtils
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.isSomeKindOfShop
import de.westnordost.streetcomplete.ktx.toTypedArray
import kotlinx.android.synthetic.main.dialog_shop_gone.view.*

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
        val input = presetsEditText.text.toString()
        return getFeatures(input).firstOrNull()?.takeIf { it.canonicalName == StringUtils.canonicalize(input) }
    }

    private fun getFeatures(startsWith: String) : List<Feature> {
        val localeList = ConfigurationCompat.getLocales(context.resources.configuration)
        val fakeElement = FakeElement()
        return featureDictionary
            .byTerm(startsWith)
            .forGeometry(geometryType)
            .inCountry(countryCode)
            .forLocale(*localeList.toTypedArray())
            .find()
            .filter { feature ->
                fakeElement.setTags(feature.tags)
                fakeElement.isSomeKindOfShop()
            }
    }
}

private class FakeElement : Element {
    private var tags: Map<String, String>? = null
    override fun isNew() = false
    override fun isModified() = false
    override fun isDeleted() = false
    override fun getId() = 0L
    override fun getVersion() = 0
    override fun getChangeset() = null
    override fun getDateEdited() = null
    override fun getTags() = tags
    fun setTags(map: Map<String, String>) { tags = map }
    override fun getType() = Element.Type.NODE
}
