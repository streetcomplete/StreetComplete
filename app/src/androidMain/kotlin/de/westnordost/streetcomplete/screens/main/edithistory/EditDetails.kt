package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.util.ktx.toLocalDateTime
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalDateTimeFormatter
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/** Shows details for an edit. I.e. image, title, name and location of edited element (if any),
 *  edit description */
@Composable
fun EditDetails(
    edit: Edit,
    element: Element?,
    featureDictionaryLazy: Lazy<FeatureDictionary>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateTimeFormatter = LocalDateTimeFormatter(
        dateStyle = DateTimeFormatStyle.Short,
        timeStyle = DateTimeFormatStyle.Short
    )
    val createdTime = Instant.fromEpochMilliseconds(edit.createdTimestamp).toLocalDateTime()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = dateTimeFormatter.format(createdTime),
                style = MaterialTheme.typography.body2,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditImage(
                edit = edit,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = stringResource(edit.getTitle(element?.tags)),
                style = MaterialTheme.typography.body1,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.high),
            )
        }

        if (element != null) {
            val nameAndLocation = nameAndLocationLabel(element, featureDictionaryLazy.value)
            if (nameAndLocation != null) {
                Text(
                    text = nameAndLocation,
                    style = MaterialTheme.typography.body2,
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                )
            }
        }

        Divider()
        SelectionContainer {
            EditDescription(edit)
        }
    }
}
