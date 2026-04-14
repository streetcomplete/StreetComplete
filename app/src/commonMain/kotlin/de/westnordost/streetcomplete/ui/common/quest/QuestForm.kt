package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import de.westnordost.streetcomplete.resources.note_for_object
import de.westnordost.streetcomplete.resources.quest_generic_otherAnswers2
import de.westnordost.streetcomplete.resources.quest_maxweight_title
import de.westnordost.streetcomplete.resources.quest_streetName_hint
import de.westnordost.streetcomplete.resources.quest_streetName_title
import de.westnordost.streetcomplete.screens.main.messages.LoremIpsumLines
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubble
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubbleArrowDirection
import de.westnordost.streetcomplete.ui.theme.defaultTextLinkStyles
import de.westnordost.streetcomplete.ui.theme.titleSmall
import de.westnordost.streetcomplete.ui.util.annotateLinks
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A generic quest form, with a [title], [subtitle], [hintText] and [hintImages] in the
 *  header speech bubble, then an optional [note] by another mapper shown below as another speech
 *  bubble, then finally the speech bubble containing the center-aligned [content] padded with a
 *  [contentPadding] (if there is any content) and below a row oftext buttons showing different
 *  [answers]. At the very start of the text button row, there's a text button labeled "Uh…" that,
 *  when tapped, opens a dropdown menu containing [otherAnswers]. */
@Composable
fun QuestForm(
    questType: QuestType,
    modifier: Modifier = Modifier,
    title: String = stringResource(questType.title),
    subtitle: AnnotatedString? = null,
    hintText: String? = questType.hint?.let { stringResource(it) },
    hintImages: List<DrawableResource> = questType.hintImages,
    note: String? = null,
    answers: List<Answer> = emptyList(),
    otherAnswers: List<Answer> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    val elevation = 16.dp
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SpeechBubble(
            elevation = elevation,
            arrowDirection = SpeechBubbleArrowDirection.Top,
            arrowPlacementBias = 0.1f,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            QuestHeader(
                title = title,
                subtitle = subtitle,
                hintText = hintText,
                hintImages = hintImages,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (note != null) {
            NoteBubble(
                text = note,
                elevation = elevation,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            )
        }

        QuestAnswerBubble(
            modifier = Modifier.fillMaxWidth(),
            elevation = elevation,
            answers = answers,
            otherAnswers = otherAnswers,
            contentPadding = contentPadding,
            content = content,
        )
    }
}

@Composable
private fun NoteBubble(
    text: String,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = elevation,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(Res.string.note_for_object),
                style = MaterialTheme.typography.titleSmall
            )
            SelectionContainer {
                Text(
                    text = text.annotateLinks(MaterialTheme.typography.defaultTextLinkStyles()),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.alpha(ContentAlpha.medium)
                )
            }
        }
    }
}

@Preview
@Composable
private fun QuestFormPreview() {
    QuestForm(
        questType = object : QuestType {
            override val icon = 0
            override val title = Res.string.quest_streetName_title
            override val wikiLink = null
            override val achievements = emptyList<EditTypeAchievement>()
            override val hint = Res.string.quest_streetName_hint
        },
        subtitle = AnnotatedString("Tertiary Road"),
        note = "unpaved",
        answers = listOf(
            Answer("Yes") {},
            Answer("No") {},
            Answer("Perhaps") {},
        ),
        otherAnswers = listOf(
            Answer("Can't say") {},
            Answer("Can say") {},
        )
    ) {
        Text("Some content")
    }
}
