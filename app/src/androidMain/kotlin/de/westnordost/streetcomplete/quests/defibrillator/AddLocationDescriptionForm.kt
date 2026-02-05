package de.westnordost.streetcomplete.quests.defibrillator

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.create_new_note_hint
import de.westnordost.streetcomplete.resources.quest_defibrillator_location_description
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.TextFieldStyle
import de.westnordost.streetcomplete.ui.util.content
import org.jetbrains.compose.resources.stringResource

class AddLocationDescriptionForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private var description: MutableState<String> = mutableStateOf("")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_defibrillator_location_description))
                    Text(stringResource(Res.string.create_new_note_hint))
                }
                TextField2(
                    value = description.value,
                    onValueChange = {
                        description.value = it
                        checkIsFormComplete()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = TextFieldStyle.Outlined,
                )
            }
        } }
    }

    override fun onClickOk() {
        applyAnswer(description.value)
    }

    override fun isFormComplete() = description.value.isNotEmpty()
}
