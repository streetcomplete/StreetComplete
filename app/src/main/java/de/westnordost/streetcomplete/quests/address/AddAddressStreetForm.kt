package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.text.parseAsHtml
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.road_name.RoadNameSuggestionsSource
import de.westnordost.streetcomplete.util.getNameAndLocationLabelString
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import org.koin.android.ext.android.inject
import java.util.Locale

class AddAddressStreetForm : AbstractOsmQuestForm<AddressStreetAnswer>() {
    private val abbreviationsByLocale: AbbreviationsByLocale by inject()
    private val roadNameSuggestionsSource: RoadNameSuggestionsSource by inject()

    private var streetNameInput: EditText? = null
    private var placeNameInput: EditText? = null

    private var isPlaceName = false
    private var selectedStreetName: String? = null

    private val streetName: String? get() = streetNameInput?.nonBlankTextOrNull
    private val placeName: String? get() = placeNameInput?.nonBlankTextOrNull

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_address_street_no_named_streets) { switchToPlaceNameLayout() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitleHintLabel(getNameAndLocationLabelString(element.tags, resources, featureDictionary, alwaysShowHouseNumber = true))

        isPlaceName = savedInstanceState?.getBoolean(IS_PLACENAME) ?: false
        setLayout(if (isPlaceName) R.layout.quest_housenumber_place else R.layout.quest_housenumber_street)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_PLACENAME, isPlaceName)
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        if (isPlaceName) return super.onClickMapAt(position, clickAreaSizeInMeters)

        val dist = clickAreaSizeInMeters + 5
        val namesByLocale = roadNameSuggestionsSource.getNames(listOf(position), dist).firstOrNull()
        if (namesByLocale != null) {
            // why using .keys.firstOrNull { Locale(it).language == XXX } instead of .containsKey(XXX):
            // ISO 639 is an unstable standard. For example, id == in. If the comparisons are made
            // with the Locale class, that takes care of it

            val countryLanguage = countryInfo.locale.language
            val defaultName = namesByLocale[""]
            if (defaultName != null) {
                // name=A -> name=A, name:de=A (in Germany)
                if (namesByLocale.keys.firstOrNull { Locale(it).language == countryLanguage } == null) {
                    namesByLocale[countryLanguage] = defaultName
                }
            }

            // if available, display the selected street name in the user's locale
            val userLanguage = Locale.getDefault().language
            val lang = namesByLocale.keys.firstOrNull { Locale(it).language == userLanguage }
            if (lang != null) {
                streetNameInput?.setText(namesByLocale[lang])
            } else {
                streetNameInput?.setText(namesByLocale[""])
            }
            selectedStreetName = namesByLocale[""]
        }

        return true
    }

    override fun onClickOk() {
        if (isPlaceName) {
            applyAnswer(PlaceName(placeName!!))
        } else {
            if (selectedStreetName != null) {
                applyAnswer(StreetName(selectedStreetName!!))
            } else {
                // only for user-input, check for possible abbreviations
                val abbr = abbreviationsByLocale.get(countryInfo.locale)
                val name = streetName!!
                val containsAbbreviations = abbr?.containsAbbreviations(name) == true

                if (name.contains(".") || containsAbbreviations) {
                    confirmPossibleAbbreviation(name) { applyAnswer(StreetName(name)) }
                } else {
                    applyAnswer(StreetName(name))
                }
            }
        }
    }

    private fun confirmPossibleAbbreviation(name: String, onConfirmed: () -> Unit) {
        val title = resources.getString(
            R.string.quest_streetName_nameWithAbbreviations_confirmation_title_name,
            "<i>" + Html.escapeHtml(name) + "</i>"
        ).parseAsHtml()

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(R.string.quest_streetName_nameWithAbbreviations_confirmation_description)
            .setPositiveButton(R.string.quest_streetName_nameWithAbbreviations_confirmation_positive) { _, _ -> onConfirmed() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete(): Boolean =
        if (isPlaceName) placeName != null else streetName != null

    private fun setLayout(layoutResourceId: Int) {
        val view = setContentView(layoutResourceId)

        val onChanged = {
            checkIsFormComplete()
            // if the user changed the text, it is now his custom input
            selectedStreetName = null
        }
        streetNameInput = view.findViewById(R.id.streetNameInput)
        placeNameInput = view.findViewById(R.id.placeNameInput)
        streetNameInput?.doAfterTextChanged { onChanged() }
        placeNameInput?.doAfterTextChanged { onChanged() }
    }

    private fun switchToPlaceNameLayout() {
        isPlaceName = true
        setLayout(R.layout.quest_housenumber_place)
        placeNameInput?.requestFocus()
    }

    companion object {
        private const val IS_PLACENAME = "is_placename"
    }
}
