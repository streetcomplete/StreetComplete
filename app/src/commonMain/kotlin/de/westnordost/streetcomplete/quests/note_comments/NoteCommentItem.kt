package de.westnordost.streetcomplete.quests.note_comments

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubble
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubbleArrowDirection
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubbleNoArrow
import de.westnordost.streetcomplete.ui.util.annotateLinks
import de.westnordost.streetcomplete.ui.util.formatAnnotated
import de.westnordost.streetcomplete.util.ktx.toLocalDateTime
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalDateTimeFormatter
import io.ktor.http.encodeURLParameter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/** displays a single note comment. The text is selectable, links within that text are clickable and
 *  the username (if not anonymous) is clickable. If available, the avatar of the user is shown,
 *  otherwise, a placeholder is shown. */
@Composable
fun NoteCommentItem(
    noteComment: NoteComment,
    avatarPainter: Painter?,
    modifier: Modifier = Modifier,
    elevation: Dp = 16.dp,
    textLinkStyles: TextLinkStyles? = null
) {
    val annotatedUserName = buildAnnotatedString {
        val name = noteComment.user?.displayName
        if (name != null) {
            val url = "https://www.openstreetmap.org/user/${name.encodeURLParameter()}"
            withLink(LinkAnnotation.Url(url, textLinkStyles)) { append(name) }
        } else {
            append(stringResource(Res.string.quest_noteDiscussion_anonymous))
        }
    }

    val annotatedCommentText = noteComment.text?.annotateLinks(textLinkStyles)

    val dateTimeFormatter = LocalDateTimeFormatter(
        dateStyle = DateTimeFormatStyle.Short,
        timeStyle = DateTimeFormatStyle.Short
    )
    val dateTime = Instant.fromEpochMilliseconds(noteComment.timestamp).toLocalDateTime()
    val dateText = dateTimeFormatter.format(dateTime)

    val actionTextResource = when (noteComment.action) {
        NoteComment.Action.CLOSED -> Res.string.quest_noteDiscussion_closed2
        NoteComment.Action.REOPENED -> Res.string.quest_noteDiscussion_reopen2
        NoteComment.Action.HIDDEN -> Res.string.quest_noteDiscussion_hide2
        else -> null
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (annotatedCommentText != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    elevation = elevation,
                    shape = CircleShape,
                ) {
                    Image(
                        painter = avatarPainter ?: painterResource(Res.drawable.avatar_osm_anonymous),
                        contentDescription = annotatedUserName.text
                    )
                }

                SpeechBubble(
                    arrowDirection = SpeechBubbleArrowDirection.Start,
                    elevation = elevation,
                ) {
                    SelectionContainer {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(annotatedCommentText)
                            Divider()
                            Text(
                                text = stringResource(Res.string.quest_noteDiscussion_comment2)
                                    .formatAnnotated(annotatedUserName, dateText),
                                modifier = Modifier.alpha(ContentAlpha.medium)
                            )
                        }
                    }
                }
            }
        }
        // the action (if anything else than a normal comment) is shown in a separate bubble, just
        // like for example in github ("comment and close")
        if (actionTextResource != null) {
            SpeechBubbleNoArrow(elevation = elevation) {
                Text(
                    text = stringResource(actionTextResource)
                        .formatAnnotated(annotatedUserName, dateText),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .alpha(ContentAlpha.medium),
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}
