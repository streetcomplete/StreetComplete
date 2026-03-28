package de.westnordost.streetcomplete.quests.note_comments

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.SpeechBubbleArrowDirection
import de.westnordost.streetcomplete.ui.common.SpeechBubbleShape
import de.westnordost.streetcomplete.ui.util.annotateLinks
import de.westnordost.streetcomplete.ui.util.formatAnnotated
import de.westnordost.streetcomplete.util.ktx.toLocalDateTime
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalDateTimeFormatter
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** displays a single note comment */
@Composable
fun NoteComment(
    noteComment: NoteComment,
    avatarPainter: Painter?,
    modifier: Modifier = Modifier,
    textLinkStyles: TextLinkStyles? = null
) {
    val annotatedUserName = buildAnnotatedString {
        val name = noteComment.user?.displayName
        if (name != null) {
            val url = "https://www.openstreetmap.org/user/$name"
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (annotatedCommentText != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    // half the size of OSM avatar images in pixels, so, shouldn't be too blurry
                    modifier = Modifier.size(50.dp),
                    elevation = 16.dp,
                    shape = CircleShape
                ) {
                    Image(
                        painter = avatarPainter ?: painterResource(Res.drawable.osm_anon_avatar),
                        contentDescription = annotatedUserName.text
                    )
                }

                val speechBubbleShape = SpeechBubbleShape(
                    arrowDirection = SpeechBubbleArrowDirection.Start,
                )
                Surface(
                    elevation = 16.dp,
                    shape = speechBubbleShape
                ) {
                    Column(
                        modifier = Modifier
                            .padding(speechBubbleShape.contentPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SelectionContainer {
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
        if (actionTextResource != null) {
            Surface(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(actionTextResource)
                        .formatAnnotated(annotatedUserName, dateText),
                    modifier = Modifier.padding(16.dp).alpha(ContentAlpha.medium),
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}
