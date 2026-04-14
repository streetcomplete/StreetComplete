package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheet
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetState
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubble
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubbleArrowDirection
import de.westnordost.streetcomplete.ui.ktx.isLandscape
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.ui.theme.defaultTextLinkStyles
import de.westnordost.streetcomplete.ui.theme.titleSmall
import de.westnordost.streetcomplete.ui.util.annotateLinks
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

/** A quest form can **either** have an OK button for confirmation **or** a list of button answers
 *  at-a-time */
sealed interface QuestAnswer

data class Answers(val answers: List<Answer>) : QuestAnswer {
    // convenience constructors
    constructor() : this(emptyList())
    constructor(vararg answers: Answer) : this(answers.toList())
}
data class Confirm(
    val isVisible: Boolean,
    val onClick: () -> Unit,
) : QuestAnswer


/** A generic quest form, with a [title], [subtitle], [hintText] and [hintImages] in the
 *  header speech bubble, then an optional [note] by another mapper shown below as another speech
 *  bubble, then finally the speech bubble containing the center-aligned [content] padded with a
 *  [contentPadding] (if there is any content) and below a row oftext buttons showing different
 *  [answers]. At the very start of the text button row, there's a text button labeled "Uh…" that,
 *  when tapped, opens a dropdown menu containing [otherAnswers]. */
@Composable
fun QuestForm(
    questType: QuestType,
    answers: QuestAnswer,
    modifier: Modifier = Modifier,
    title: String = stringResource(questType.title),
    subtitle: AnnotatedString? = null,
    hintText: String? = questType.hint?.let { stringResource(it) },
    hintImages: List<DrawableResource> = questType.hintImages,
    note: String? = null,
    otherAnswers: Answers = Answers(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    val windowInfo = LocalWindowInfo.current

    val initialState =
        if (LocalWindowInfo.current.isLandscape) BottomSheetState.Expanded
        else BottomSheetState.Collapsed

    val elevation = 4.dp

    Box(modifier = modifier.sizeIn(maxWidth = Dimensions.getMaxQuestFormWidth(windowInfo))
    ) {
        BottomSheet(
            initialState = initialState,
            peekHeight = Dimensions.QuestFormPeekHeight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeechBubble(
                    elevation = elevation,
                    arrowDirection = SpeechBubbleArrowDirection.Top,
                    arrowPlacementBias = 0.1f,
                    modifier = Modifier.padding(horizontal = 8.dp)
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
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                    )
                }

                QuestAnswerBubble(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = elevation,
                    answers = (answers as? Answers)?.answers ?: emptyList(),
                    otherAnswers = otherAnswers.answers,
                    contentPadding = contentPadding,
                    content = content,
                )
            }
        }
        if (answers is Confirm) {
            FloatingOkButton(
                visible = answers.isVisible,
                onClick = answers.onClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .safeDrawingPadding()
                    .padding(8.dp)
            )
        }
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
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
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
    Box(Modifier.fillMaxSize().background(Color.Green)) {
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
        answers = Confirm(true, onClick = {}),
        otherAnswers = Answers(
            Answer("Can't say") {},
            Answer("Can say") {},
        )
    ) {
        Text(LoremIpsum(500).values.joinToString(" "))
    }
    }
}
