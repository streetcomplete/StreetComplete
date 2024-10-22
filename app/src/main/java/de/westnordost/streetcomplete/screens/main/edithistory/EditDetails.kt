package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.ui.util.toAnnotatedString
import de.westnordost.streetcomplete.util.getNameAndLocationHtml
import de.westnordost.streetcomplete.util.html.parseHtml
import java.text.DateFormat

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

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditImage(
                edit = edit,
                modifier = Modifier.size(64.dp)
            )
            Column {
                Text(
                    text = DateFormat
                        .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(edit.createdTimestamp),
                    style = MaterialTheme.typography.body2,
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                )
                Text(
                    text = edit.getTitle(element?.tags),
                    style = MaterialTheme.typography.body1,
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.high),
                )
                if (element != null) {
                    val nameAndLocation = remember(element, context.resources) {
                        getNameAndLocationHtml(element, context.resources, featureDictionaryLazy.value)
                            ?.let { parseHtml(it) }
                    }
                    if (nameAndLocation != null) {
                        Text(
                            text = nameAndLocation.toAnnotatedString(),
                            style = MaterialTheme.typography.body2,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                        )
                    }
                }
            }
        }
        Divider()
        SelectionContainer {
            EditDescription(edit)
        }
    }
}
