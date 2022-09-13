package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.text.parseAsHtml
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.databinding.QuestAddressStreetBinding
import de.westnordost.streetcomplete.osm.address.AddressStreetNameInputViewController
import de.westnordost.streetcomplete.osm.address.PlaceName
import de.westnordost.streetcomplete.osm.address.StreetName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceName
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.road_name.RoadNameSuggestionsSource
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.getNameAndLocationLabelString
import org.koin.android.ext.android.inject

class AddAddressStreetForm : AbstractOsmQuestForm<StreetOrPlaceName>() {
    override val contentLayoutResId = R.layout.quest_address_street
    private val binding by contentViewBinding(QuestAddressStreetBinding::bind)

    private val abbreviationsByLocale: AbbreviationsByLocale by inject()
    private val roadNameSuggestionsSource: RoadNameSuggestionsSource by inject()

    private lateinit var streetNameInputCtrl: AddressStreetNameInputViewController

    private var isPlaceName = false

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_address_street_no_named_streets) { switchToPlaceName() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitleHintLabel(getNameAndLocationLabelString(element.tags, resources, featureDictionary, alwaysShowHouseNumber = true))

        isPlaceName = savedInstanceState?.getBoolean(IS_PLACE_NAME) ?: false

        streetNameInputCtrl = AddressStreetNameInputViewController(
            streetNameInput = binding.streetNameInput,
            roadNameSuggestionsSource = roadNameSuggestionsSource,
            abbreviationsByLocale = abbreviationsByLocale,
            countryLocale = countryInfo.locale
        )
        streetNameInputCtrl.onInputChanged = { checkIsFormComplete() }
        binding.placeNameInput.doAfterTextChanged { checkIsFormComplete() }

        updateVisibilities()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_PLACE_NAME, isPlaceName)
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        if (isPlaceName) return super.onClickMapAt(position, clickAreaSizeInMeters)

        streetNameInputCtrl.selectStreetAt(position, clickAreaSizeInMeters)
        return true
    }

    override fun onClickOk() {
        if (isPlaceName) {
            applyAnswer(PlaceName(binding.placeNameInput.nonBlankTextOrNull!!))
        } else {
            val name = streetNameInputCtrl.streetName!!
            if (isAbbreviation(streetNameInputCtrl.streetName!!)) {
                confirmPossibleAbbreviation(name) { applyAnswer(StreetName(name)) }
            } else {
                applyAnswer(StreetName(name))
            }
        }
    }

    private fun isAbbreviation(name: String): Boolean =
        name.contains(".")
        || abbreviationsByLocale[countryInfo.locale]?.containsAbbreviations(name) == true

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

    private fun updateVisibilities() {
        binding.placeNameInput.isGone = !isPlaceName
        binding.streetNameInput.isGone = isPlaceName
    }

    override fun isFormComplete(): Boolean =
        if (isPlaceName) binding.placeNameInput.nonBlankTextOrNull != null
        else             streetNameInputCtrl.streetName != null


    private fun switchToPlaceName() {
        isPlaceName = true
        updateVisibilities()
        binding.placeNameInput.requestFocus()
    }

    companion object {
        private const val IS_PLACE_NAME = "is_place_name"
    }
}
