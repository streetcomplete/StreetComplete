package de.westnordost.streetcomplete.quests.bench_backrest

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer.NO
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer.PICNIC_TABLE
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer.YES
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddBenchBackrestForm : AbstractOsmQuestForm<BenchBackrestAnswer>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) {
                    applyAnswer(NO)
                },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) {
                    applyAnswer(YES)
                }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_bench_answer_picnic_table)) {
                    applyAnswer(PICNIC_TABLE)
                }
            )
        )
    }
}
