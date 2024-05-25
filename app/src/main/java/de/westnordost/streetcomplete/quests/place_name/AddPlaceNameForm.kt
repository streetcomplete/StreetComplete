package de.westnordost.streetcomplete.quests.place_name

import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestLocalizednameBinding
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.SearchAdapter
import de.westnordost.streetcomplete.util.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.ktx.showKeyboard

class AddPlaceNameForm : AAddLocalizedNameForm<PlaceNameAnswer>() {

    override val contentLayoutResId = R.layout.quest_localizedname
    private val binding by contentViewBinding(QuestLocalizednameBinding::bind)

    override val addLanguageButton get() = binding.addLanguageButton
    override val namesList get() = binding.namesList

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_placeName_no_name_answer) { confirmNoName() },
        createBrandAnswer()
    )

    private fun createBrandAnswer(): AnswerItem? {
        val ctx = context ?: return null
        if (!element.tags.containsKey("shop") && !element.tags.containsKey("amenity")
            && !element.tags.containsKey("leisure") && !element.tags.containsKey("tourism")) return null
        return AnswerItem(R.string.quest_name_brand) {
            val languages = getLanguagesForFeatureDictionary(ctx.resources.configuration)
            val searchAdapter = SearchAdapter(ctx, { search ->
                featureDictionary.getByTerm(
                    search = search,
                    languages = languages,
                    country = countryOrSubdivisionCode,
                    geometry = GeometryType.POINT
                ).filter {
                    it.addTags.containsKey("brand") && when {
                        element.tags.containsKey("amenity") -> it.addTags["amenity"] == element.tags["amenity"]
                        element.tags.containsKey("shop") -> it.addTags["shop"] == element.tags["shop"]
                        element.tags.containsKey("leisure") -> it.addTags["leisure"] == element.tags["leisure"]
                        element.tags.containsKey("tourism") -> it.addTags["tourism"] == element.tags["tourism"]
                        else -> false
                    } }.toList()
            }, { it.name })
            var feature: Feature? = null
            var dialog: AlertDialog? = null
            val textField = layoutInflater.inflate(R.layout.quest_name_suggestion, null) as AutoCompleteTextView
            textField.setAdapter(searchAdapter)
            textField.doAfterTextChanged {
                dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = !it?.toString().isNullOrBlank()
            }
            textField.setOnItemClickListener { _, _, i, _ -> feature = searchAdapter.getItem(i) }
            dialog = AlertDialog.Builder(ctx)
                .setTitle(R.string.quest_name_brand)
                .setView(textField)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val f = feature
                    val text = textField.text.toString()
                    if (text == f?.name)
                        applyAnswer(FeatureName(f))
                    else
                        applyAnswer(BrandName(text))
                }
                .create()
            dialog.setOnShowListener {
                textField.requestFocus()
                textField.showKeyboard()
            }
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
    }

    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(PlaceName(names))
    }

    private fun confirmNoName() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoPlaceNameSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
