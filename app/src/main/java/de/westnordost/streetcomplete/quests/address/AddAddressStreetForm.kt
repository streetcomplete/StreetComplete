package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.databinding.ViewStreetOrPlaceNameInputBinding
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameViewController
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.road_name.RoadNameSuggestionsSource
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import org.koin.android.ext.android.inject

class AddAddressStreetForm : AbstractOsmQuestForm<StreetOrPlaceName>() {
    override val contentLayoutResId = R.layout.view_street_or_place_name_input
    private val binding by contentViewBinding(ViewStreetOrPlaceNameInputBinding::bind)

    private val abbreviationsByLocale: AbbreviationsByLocale by inject()
    private val roadNameSuggestionsSource: RoadNameSuggestionsSource by inject()

    private lateinit var streetOrPlaceCtrl: StreetOrPlaceNameViewController

    private var isShowingPlaceName = false

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_address_street_no_named_streets) { showPlaceName() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isShowingPlaceName = savedInstanceState?.getBoolean(IS_PLACE_NAME) ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitleHintLabel(getNameAndLocationLabel(
            element.tags, resources, featureDictionary,
            showHouseNumber = true
        ))

        streetOrPlaceCtrl = StreetOrPlaceNameViewController(
            select = binding.streetOrPlaceSelect,
            placeNameInputContainer = binding.placeNameInputContainer,
            placeNameInput = binding.placeNameInput,
            streetNameInputContainer = binding.streetNameInputContainer,
            streetNameInput = binding.streetNameInput,
            roadNameSuggestionsSource = roadNameSuggestionsSource,
            abbreviationsByLocale = abbreviationsByLocale,
            countryLocale = countryInfo.locale
        )
        streetOrPlaceCtrl.onInputChanged = { checkIsFormComplete() }

        // initially do not show the select for place name
        if (!isShowingPlaceName) {
            binding.streetOrPlaceSelect.isGone = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_PLACE_NAME, isShowingPlaceName)
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        return streetOrPlaceCtrl.selectStreetAt(position, clickAreaSizeInMeters)
    }

    override fun onClickOk() {
        applyAnswer(streetOrPlaceCtrl.streetOrPlaceName!!)
    }

    override fun isFormComplete(): Boolean =
        streetOrPlaceCtrl.streetOrPlaceName != null

    private fun showPlaceName() {
        isShowingPlaceName = true
        binding.streetOrPlaceSelect.isGone = false
        streetOrPlaceCtrl.selectPlaceName()
    }

    companion object {
        private const val IS_PLACE_NAME = "is_place_name"
    }
}
