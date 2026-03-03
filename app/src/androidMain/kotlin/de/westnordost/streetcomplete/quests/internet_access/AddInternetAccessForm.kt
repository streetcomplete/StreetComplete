package de.westnordost.streetcomplete.quests.internet_access

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.ACheckboxGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddInternetAccessForm : ACheckboxGroupQuestForm<InternetAccess, Set<InternetAccess>>() {

    override val items = InternetAccess.entries

    @Composable override fun BoxScope.ItemContent(item: InternetAccess) {
        Text(stringResource(item.text))
    }

    override fun onClickOk(items: Set<InternetAccess>) {
        applyAnswer(items)
    }
}
