package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import org.jetbrains.compose.resources.stringResource

private sealed interface TreeStep<out T> {
    data class Question<T>(
        val question: String,
        val selectedAnswer: Boolean?,
        val stepIndex: Int,
    ) : TreeStep<T>

    data class Leaf<T>(
        val value: T,
    ) : TreeStep<T>

    data class NoNextNode<T>(
        val question: String,
        val answer: Boolean,
    ) : TreeStep<T>
}

private fun <T> getTreeSteps(root: TreeNode<T>, answers: List<Boolean>): Pair<List<TreeStep<T>>, T?> {
    val steps = mutableListOf<TreeStep<T>>()
    var currentNode: TreeNode<T>? = root
    var stepIndex = 0
    var finalValue: T? = null

    while (currentNode != null) {
        if (currentNode.isLeaf) {
            val value = currentNode.value!!
            steps.add(TreeStep.Leaf(value))
            finalValue = value
            break
        }

        val question = currentNode.question ?: ""
        if (stepIndex < answers.size) {
            val answer = answers[stepIndex]
            steps.add(TreeStep.Question(question = question, selectedAnswer = answer, stepIndex = stepIndex))
            val nextNode = if (answer) currentNode.yes else currentNode.no
            if (nextNode == null) {
                steps.add(TreeStep.NoNextNode(question = question, answer = answer))
                break
            }
            currentNode = nextNode
            stepIndex++
        } else {
            steps.add(TreeStep.Question(question = question, selectedAnswer = null, stepIndex = stepIndex))
            break
        }
    }

    return Pair(steps, finalValue)
}

/**
 * Quest form that guides the user through a binary decision tree of yes/no questions to determine a final answer.
 *
 * Questions are presented sequentially. Once a question is answered, it collapses into a summary row
 * displaying the selected choice, which can be tapped to expand and change the answer. Reaching a leaf
 * node displays the final answer item and allows submitting the form. If a branch has no next node,
 * the user is prompted to leave a note instead.
 */
@Composable
fun <T> TreeQuestForm(
    on: (QuestAction<T>) -> Unit,
    items: TreeNode<T>,
    modifier: Modifier = Modifier,
    title: String = stringResource(LocalQuestType.current!!.title),
    itemContent: @Composable (item: T) -> Unit = { Text(it.toString()) },
    otherAnswers: @Composable (() -> List<AnswerItem>) = { emptyList() },
) {
    var answers by rememberSaveable { mutableStateOf(emptyList<Boolean>()) }
    var userExpandedSteps by rememberSaveable { mutableStateOf(setOf<Int>()) }

    val (steps, finalValue) = remember(items, answers) {
        getTreeSteps(items, answers)
    }

    val isComplete = finalValue != null

    QuestForm(
        on = on,
        isComplete = isComplete,
        onClickOk = { finalValue?.let { on(Answer(it)) } },
        modifier = modifier,
        title = title,
        otherAnswers = otherAnswers,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            for (step in steps) {
                when (step) {
                    is TreeStep.Question -> {
                        val isAnswered = step.selectedAnswer != null
                        val isExpanded = !isAnswered || (step.stepIndex in userExpandedSteps)

                        QuestionStepItem(
                            step = step,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                userExpandedSteps = if (step.stepIndex in userExpandedSteps) {
                                    userExpandedSteps - step.stepIndex
                                } else {
                                    userExpandedSteps + step.stepIndex
                                }
                            },
                            onSelectAnswer = { answer ->
                                answers = answers.take(step.stepIndex) + answer
                                userExpandedSteps = userExpandedSteps.filter { it < step.stepIndex }.toSet()
                            },
                        )
                    }
                    is TreeStep.Leaf -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            itemContent(step.value)
                        }
                    }
                    is TreeStep.NoNextNode -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CompositionLocalProvider(
                                LocalContentAlpha provides ContentAlpha.medium,
                                LocalTextStyle provides MaterialTheme.typography.body2,
                            ) {
                                Text(stringResource(Res.string.quest_leave_new_note_title))
                            }
                            TextButton(
                                onClick = { on(Action.CantSay) },
                            ) {
                                Text(stringResource(Res.string.leave_note))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> QuestionStepItem(
    step: TreeStep.Question<T>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelectAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (step.selectedAnswer != null && !isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onToggleExpand() }
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = step.question,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colors.primary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Text(
                        text = if (step.selectedAnswer) {
                            stringResource(Res.string.quest_generic_hasFeature_yes)
                        } else {
                            stringResource(Res.string.quest_generic_hasFeature_no)
                        },
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        } else {
            if (step.selectedAnswer != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onToggleExpand() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = step.question,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Text(
                    text = step.question,
                    style = MaterialTheme.typography.body1,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button2(
                    onClick = { onSelectAnswer(true) },
                    modifier = Modifier.weight(1f),
                    style = if (step.selectedAnswer == true) ButtonStyle.Default else ButtonStyle.Outlined,
                ) {
                    Text(stringResource(Res.string.quest_generic_hasFeature_yes))
                }
                Button2(
                    onClick = { onSelectAnswer(false) },
                    modifier = Modifier.weight(1f),
                    style = if (step.selectedAnswer == false) ButtonStyle.Default else ButtonStyle.Outlined,
                ) {
                    Text(stringResource(Res.string.quest_generic_hasFeature_no))
                }
            }
        }
    }
}
