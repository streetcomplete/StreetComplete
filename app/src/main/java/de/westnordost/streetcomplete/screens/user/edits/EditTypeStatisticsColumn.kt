package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.GrassGreen

/** Simple bar diagram of solved quests by quest type */
@Composable
fun EditTypeStatisticsColumn(
    editTypeObjStatistics: List<EditTypeObjStatistics>,
    modifier: Modifier = Modifier,
) {
    var showInfo by remember { mutableStateOf<EditTypeObjStatistics?>(null) }

    // list is sorted by largest count descending
    val maxCount = editTypeObjStatistics.firstOrNull()?.count ?: 0
    LazyColumn(modifier) {
        items(
            items = editTypeObjStatistics,
            key = { it.type.name }
        ) { item ->
            StatisticsRow(
                title = {
                    Image(
                        painter = painterResource(item.type.icon),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                },
                count = item.count,
                maxCount = maxCount,
                modifier = Modifier
                    .clickable { showInfo = item }
                    .padding(8.dp),
                color = GrassGreen,
            )
        }
    }
    showInfo?.let {
        EditTypeDialog(
            editType = it.type,
            onDismissRequest = { showInfo = null }
        )
    }
}
