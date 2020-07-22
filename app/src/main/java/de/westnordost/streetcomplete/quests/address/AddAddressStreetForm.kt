package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.util.TextChangedWatcher
import java.util.*
import javax.inject.Inject

class AddAddressStreetForm : AbstractQuestFormAnswerFragment<AddressStreetAnswer>() {
    @Inject internal lateinit var abbreviationsByLocale: AbbreviationsByLocale
    @Inject internal lateinit var roadNameSuggestionsDao: RoadNameSuggestionsDao

    private var streetNameInput: EditText? = null
    private var placeNameInput: EditText? = null

    private var isPlaceName = false
    private var selectedStreetName: String? = null

    private val streetName: String get() = streetNameInput?.text?.toString().orEmpty().trim()
    private val placeName: String get() = placeNameInput?.text?.toString().orEmpty().trim()

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_address_street_no_named_streets) { switchToPlaceNameLayout() }
    )

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        isPlaceName = savedInstanceState?.getBoolean(IS_PLACENAME) ?: false
        setLayout(if (isPlaceName) R.layout.quest_housenumber_place else R.layout.quest_housenumber_street)

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_PLACENAME, isPlaceName)
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        if (isPlaceName) return super.onClickMapAt(position, clickAreaSizeInMeters)

        val dist = clickAreaSizeInMeters + 5
        val namesByLocale = roadNameSuggestionsDao.getNames(listOf(position), dist).firstOrNull()
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
        if(isPlaceName) {
            applyAnswer(PlaceName(placeName))
        } else {
            if (selectedStreetName != null) {
                applyAnswer(StreetName(selectedStreetName!!))
            } else {
                // only for user-input, check for possible abbreviations
                val abbr = abbreviationsByLocale.get(countryInfo.locale)
                val name = streetName
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
        val title = Html.fromHtml(
            resources.getString(
                R.string.quest_streetName_nameWithAbbreviations_confirmation_title_name,
                "<i>" + Html.escapeHtml(name) + "</i>"
            )
        )

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(R.string.quest_streetName_nameWithAbbreviations_confirmation_description)
            .setPositiveButton(R.string.quest_streetName_nameWithAbbreviations_confirmation_positive) { _, _ -> onConfirmed() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete(): Boolean =
        if (isPlaceName) placeName.isNotEmpty() else streetName.isNotEmpty()

    private fun setLayout(layoutResourceId: Int) {
        val view = setContentView(layoutResourceId)

        val onChanged = TextChangedWatcher {
            checkIsFormComplete()
            // if the user changed the text, it is now his custom input
            selectedStreetName = null
        }
        streetNameInput = view.findViewById(R.id.streetNameInput)
        placeNameInput = view.findViewById(R.id.placeNameInput)
        streetNameInput?.addTextChangedListener(onChanged)
        placeNameInput?.addTextChangedListener(onChanged)
    }

    private fun switchToPlaceNameLayout() {
        isPlaceName = true
        setLayout(R.layout.quest_housenumber_place)
    }

    companion object {
        private const val IS_PLACENAME = "is_placename"
    }
}
