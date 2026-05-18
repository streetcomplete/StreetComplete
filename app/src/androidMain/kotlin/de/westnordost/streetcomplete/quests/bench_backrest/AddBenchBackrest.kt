package de.westnordost.streetcomplete.quests.bench_backrest

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddBenchBackrest : OsmFilterQuestType<BenchBackrestAnswer>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bench
          and (!area or area = no)
          and !backrest
          and !bench:type
          and (!seasonal or seasonal = no)
          and access !~ private|no
    """
    override val changesetComment = "Survey whether benches have backrests"
    override val wikiLink = "Tag:amenity=bench"
    override val icon = R.drawable.quest_bench_poi
    override val title = Res.string.quest_bench_backrest_title
    override val achievements = listOf(PEDESTRIAN, OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = bench or leisure = picnic_table")

    @Composable
    override fun Form(onAnswer: (BenchBackrestAnswer) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            answers = listOf(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(NO) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(YES) }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_bench_answer_picnic_table)) { onAnswer(PICNIC_TABLE) }
            )
        )
    }

    override fun applyAnswerTo(answer: BenchBackrestAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            PICNIC_TABLE -> {
                tags["leisure"] = "picnic_table"
                tags.remove("amenity")
            }
            YES -> tags["backrest"] = "yes"
            NO -> tags["backrest"] = "no"
        }
    }
}
