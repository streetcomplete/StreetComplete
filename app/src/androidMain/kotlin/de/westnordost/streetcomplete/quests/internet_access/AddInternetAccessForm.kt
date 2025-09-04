package de.westnordost.streetcomplete.quests.internet_access

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AListQuestForm
import org.jetbrains.compose.resources.stringResource

class AddInternetAccessForm : AListQuestForm<InternetAccess, InternetAccess>() {

    override val items = InternetAccess.entries

    @Composable override fun BoxScope.ItemContent(item: InternetAccess) {
        Text(stringResource(item.text))
    }
}
