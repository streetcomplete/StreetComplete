package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Dialog in which the user is asked whether the given [buildingType] is correct, in the context
 *  of that he answered before that the building doesn't have a house number. So, either it
 *  actually doesn't have a house number, or maybe the building type was mistagged instead. */
@Composable
fun NoHouseNumberDialog(
    onDismissRequest: () -> Unit,
    onNoHouseNumber: () -> Unit,
    onWrongBuildingType: () -> Unit,
    buildingType: BuildingType,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onNoHouseNumber()
                onDismissRequest()
            }) { Text(stringResource(Res.string.quest_generic_hasFeature_yes)) }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = {
                onWrongBuildingType()
                onDismissRequest()
            }) { Text(stringResource(Res.string.quest_generic_hasFeature_no)) }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(Res.string.quest_address_answer_no_housenumber_message1))
                ImageWithDescription(
                    painter = painterResource(buildingType.icon),
                    title = stringResource(buildingType.title),
                    description = buildingType.description?.let { stringResource(it) },
                    imageSize = DpSize(48.dp, 48.dp),
                )
                Text(stringResource(Res.string.quest_address_answer_no_housenumber_message2b))
            }
        }
    )
}
