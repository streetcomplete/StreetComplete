package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.CountInput
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.util.content
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

abstract class AAddCountInput : AbstractOsmQuestForm<Int>() {

    abstract val icon: DrawableResource

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private lateinit var count: MutableState<Int?>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content {
            Surface {
                count = rememberSaveable { mutableStateOf(null) }
                ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
                    CountInput(
                        count = count.value,
                        onCountChange = {
                            count.value = it
                            checkIsFormComplete()
                        },
                        iconPainter = painterResource(icon),
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }

    override fun isFormComplete() = count.value?.let { it > 0 } == true

    override fun onClickOk() {
        applyAnswer(count.value!!)
    }
}
