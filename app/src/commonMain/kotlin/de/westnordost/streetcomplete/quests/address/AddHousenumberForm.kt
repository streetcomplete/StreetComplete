package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.address.AddressNumberAndNameForm
import de.westnordost.streetcomplete.osm.address.BlockAndHouseNumber
import de.westnordost.streetcomplete.osm.address.HouseNumber
import de.westnordost.streetcomplete.osm.address.looksInvalid
import de.westnordost.streetcomplete.osm.address.streetHouseNumber
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddHousenumberForm(
    on: (QuestAction<HouseNumberAnswer>) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
) {
    var addressNumberAndName by rememberSerializable {
        val number = if (lastWasBlock) BlockAndHouseNumber("", "") else HouseNumber("")
        mutableStateOf(AddressNumberAndName(number, null))
    }

    var showNoHouseNumberDialogForBuildingType by remember { mutableStateOf<BuildingType?>(null) }
    var showMultipleNumbersHint by remember { mutableStateOf(false) }
    var confirmUnusualHouseNumber by remember { mutableStateOf(false) }

    fun onNoHouseNumber() {
        val buildingType = createBuildingType(element.tags)
        if (buildingType != null) {
            showNoHouseNumberDialogForBuildingType = buildingType
        } else {
            // fallback in case the type of building is known by Housenumber quest but not by
            // building type quest
            on(Action.LeaveNote)
        }
    }

    fun showHouseName() {
        addressNumberAndName = AddressNumberAndName(
            name = "",
            number = addressNumberAndName.number?.takeIf { !it.isEmpty() }
        )
    }

    fun applyHousenumberAnswer() {
        val number = addressNumberAndName.number?.takeIf { !it.isEmpty() }
        lastBlock = (number as? BlockAndHouseNumber)?.block
        lastWasBlock = number is BlockAndHouseNumber
        number?.streetHouseNumber?.let { lastHouseNumber = it }
        on(Answer(addressNumberAndName))
    }

    QuestForm(
        on = on,
        isComplete = addressNumberAndName.isComplete(),
        onClickOk = {
            val isUnusual = addressNumberAndName
                .number?.takeIf { !it.isEmpty() }
                ?.looksInvalid(countryInfo.additionalValidHousenumberRegex) == true

            if (isUnusual) {
                confirmUnusualHouseNumber = true
            } else {
                applyHousenumberAnswer()
            }
        },
        hasChanges = !addressNumberAndName.isEmpty(),
        otherAnswers = {
            val switchBlockAnswer = if (countryInfo.countryCode !in listOf("JP", "CZ", "SK")) {
                when (addressNumberAndName.number) {
                    is BlockAndHouseNumber ->
                        AnswerItem(stringResource(Res.string.quest_address_answer_no_block2)) {
                            addressNumberAndName = addressNumberAndName.copy(number = HouseNumber(""))
                        }
                    else ->
                        AnswerItem(stringResource(Res.string.quest_address_answer_block2)) {
                            addressNumberAndName = addressNumberAndName.copy(number = BlockAndHouseNumber("", ""))
                        }
                }
            } else null

            listOfNotNull(
                AnswerItem(stringResource(Res.string.quest_address_answer_no_housenumber)) { onNoHouseNumber() },
                AnswerItem(stringResource(Res.string.quest_address_answer_house_name2)) { showHouseName() },
                switchBlockAnswer,
                AnswerItem(stringResource(Res.string.quest_housenumber_multiple_numbers)) { showMultipleNumbersHint = true }
            )
        }
    ) {
        AddressNumberAndNameForm(
            number = addressNumberAndName.number,
            name = addressNumberAndName.name,
            onNumberChange = { addressNumberAndName = addressNumberAndName.copy(number = it) },
            onNameChange = { addressNumberAndName = addressNumberAndName.copy(name = it) },
            countryCode = countryInfo.countryCode,
            modifier = Modifier.fillMaxWidth(),
            houseNumberSuggestion = lastHouseNumber,
            blockSuggestion = lastBlock,
        )
    }

    showNoHouseNumberDialogForBuildingType?.let { buildingType ->
        NoHouseNumberDialog(
            onDismissRequest = { showNoHouseNumberDialogForBuildingType = null },
            onNoHouseNumber = { on(Answer(AddressNumberAndName(null, null))) },
            onWrongBuildingType = { on(Answer(HouseNumberAnswer.WrongBuildingType)) },
            buildingType = buildingType
        )
    }

    if (showMultipleNumbersHint) {
        InfoDialog(
            onDismissRequest = { showMultipleNumbersHint = false },
            text = {
                Text(stringResource(Res.string.quest_housenumber_multiple_numbers_description2))
            }
        )
    }

    if (confirmUnusualHouseNumber) {
        AreYouSureDialog(
            onDismissRequest = { confirmUnusualHouseNumber = false },
            onConfirmed = { applyHousenumberAnswer() },
            text = { Text(stringResource(Res.string.quest_address_unusualHousenumber_confirmation_description)) }
        )
    }
}

private var lastBlock: String? = null
private var lastHouseNumber: String? = null
private var lastWasBlock: Boolean = false
