package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.screens.settings.genericQuestTitle

/** Single item the the quest selection list. Shows icon + title, whether it is enabled and whether
 *  it is disabled by default / disabled in the country one is in */
@Composable
fun QuestSelectionItem(
    item: QuestSelection,
    onToggleSelection: (isSelected: Boolean) -> Unit,
    displayCountry: String,
    modifier: Modifier = Modifier
) {
    val alpha = if (!item.selected) ContentAlpha.disabled else ContentAlpha.high

    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(item.questType.icon),
            contentDescription = item.questType.name,
            modifier = Modifier.padding(start = 16.dp).size(48.dp).alpha(alpha),
        )
        Column(
            modifier = Modifier.padding(start = 16.dp).weight(0.1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = genericQuestTitle(item.questType),
                modifier = Modifier.alpha(alpha),
                style = MaterialTheme.typography.body1,
            )
            if (!item.enabledInCurrentCountry) {
                DisabledHint(stringResource(R.string.questList_disabled_in_country, displayCountry))
            }
            if (item.questType.defaultDisabledMessage != 0) {
                DisabledHint(stringResource(R.string.questList_disabled_by_default))
            }
        }
        Box(
            modifier = Modifier.width(64.dp).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Checkbox(
                checked = item.selected,
                onCheckedChange = onToggleSelection,
                enabled = item.isInteractionEnabled
            )
        }
    }
}

@Composable
private fun DisabledHint(text: String) {
    Text(
        text = text,
        modifier = Modifier.alpha(ContentAlpha.medium),
        style = MaterialTheme.typography.body2,
        fontStyle = FontStyle.Italic,
    )
}

@Preview
@Composable
private fun QuestSelectionItemPreview() {
    var selected by remember { mutableStateOf(true) }

    QuestSelectionItem(
        item = QuestSelection(AddRoadSurface(), selected, false),
        onToggleSelection = { selected = !selected },
        displayCountry = "Atlantis",
    )
}
