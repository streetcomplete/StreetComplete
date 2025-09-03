package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.address.PlaceName
import de.westnordost.streetcomplete.osm.address.StreetName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameForm
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.getNameAndLocationSpanned
import org.koin.android.ext.android.inject

class AddAddressStreetForm : AbstractOsmQuestForm<StreetOrPlaceName>() {
    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val nameSuggestionsSource: NameSuggestionsSource by inject()

    private val roadsWithNamesFilter =
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
            .toElementFilterExpression()

    private lateinit var streetOrPlaceName: MutableState<StreetOrPlaceName>
    private lateinit var showSelect: MutableState<Boolean>

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_address_street_no_named_streets) { showPlaceName() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitleHintLabel(getNameAndLocationSpanned(
            element, resources, featureDictionary,
            showHouseNumber = true
        ))

        binding.composeViewBase.content { Surface {
            streetOrPlaceName = rememberSerializable { mutableStateOf(
                if (lastWasPlaceName) PlaceName("") else StreetName("")
            ) }
            showSelect = rememberSaveable { mutableStateOf(lastWasPlaceName) }

            StreetOrPlaceNameForm(
                value = streetOrPlaceName.value,
                onValueChange = {
                    streetOrPlaceName.value = it
                    checkIsFormComplete()
                },
                modifier = Modifier.fillMaxWidth(),
                showSelect = showSelect.value
            )
        } }
    }


    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        if (streetOrPlaceName.value !is StreetName) return false

        val name = nameSuggestionsSource
            .getNames(position, clickAreaSizeInMeters, roadsWithNamesFilter)
            .firstOrNull()
            ?.find { it.languageTag.isEmpty() }
            ?.name
            ?: return false

        streetOrPlaceName.value = StreetName(name)
        checkIsFormComplete()
        return true
    }

    override fun onClickOk() {
        lastWasPlaceName = streetOrPlaceName.value is PlaceName
        applyAnswer(streetOrPlaceName.value)
    }

    override fun isFormComplete(): Boolean =
        streetOrPlaceName.value.name.isNotEmpty()

    private fun showPlaceName() {
        streetOrPlaceName.value = PlaceName("")
        showSelect.value = true
        checkIsFormComplete()
    }

    companion object {
        private var lastWasPlaceName = false
    }
}
