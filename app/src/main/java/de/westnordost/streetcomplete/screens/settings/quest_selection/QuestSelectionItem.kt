package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.screens.settings.genericQuestTitle
import de.westnordost.streetcomplete.ui.theme.disabled
import de.westnordost.streetcomplete.ui.theme.surfaceContainer

@Composable
fun QuestSelectionItem(
    questType: QuestType,
    isSelected: Boolean,
    onToggleSelection: (isSelected: Boolean) -> Unit,
    isInteractionEnabled: Boolean,
    displayCountry: String,
    isEnabledInCurrentCountry: Boolean,
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        if (isInteractionEnabled) MaterialTheme.colors.surface
        else MaterialTheme.colors.disabled

    val iconTint = if (!isSelected) ColorFilter.tint(MaterialTheme.colors.disabled, BlendMode.DstIn) else null
    val textColor = if (!isSelected) MaterialTheme.colors.disabled else Color.Unspecified

    Row(
        modifier = modifier
            .background(backgroundColor)
            .padding(vertical = 8.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(questType.icon),
            contentDescription = questType.name,
            modifier = Modifier.padding(start = 16.dp).size(iconSize),
            colorFilter = iconTint
        )
        Column(
            Modifier
                .padding(start = 16.dp)
                .weight(0.1f)) {
            Text(
                text = genericQuestTitle(questType),
                style = MaterialTheme.typography.body1,
                color = textColor
            )
            if (!isEnabledInCurrentCountry) {
                Text(
                    text = stringResource(R.string.questList_disabled_in_country, displayCountry),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .background(MaterialTheme.colors.surfaceContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.body2,
                    fontStyle = FontStyle.Italic
                )
            }
        }
        Box(
            modifier = Modifier.width(64.dp).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onToggleSelection,
                enabled = isInteractionEnabled
            )
        }
    }
}

@Preview
@Composable
private fun QuestSelectionItemPreview() {
    val selected = remember { mutableStateOf(true) }

    QuestSelectionItem(
        questType = AddRoadSurface(),
        isSelected = selected.value,
        onToggleSelection = { selected.value = !selected.value },
        isInteractionEnabled = true,
        displayCountry = "Atlantis",
        isEnabledInCurrentCountry = false,
        iconSize = 64.dp,
    )
}
