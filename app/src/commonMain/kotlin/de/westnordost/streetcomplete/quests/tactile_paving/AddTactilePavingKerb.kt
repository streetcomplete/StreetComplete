package de.westnordost.streetcomplete.quests.tactile_paving

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.kerb.couldBeAKerb
import de.westnordost.streetcomplete.osm.kerb.findAllKerbNodes
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddTactilePavingKerb : OsmElementQuestType<Boolean> {

    override val changesetComment = "Specify whether kerbs have tactile paving"
    override val wikiLink = "Key:tactile_paving"
    override val icon = Res.drawable.quest_kerb_tactile_paving
    override val title = Res.string.quest_tactile_paving_kerb_title
    override val enabledInCountries = COUNTRIES_WHERE_TACTILE_PAVING_IS_COMMON
    override val achievements = listOf(BLIND)
    override val hint = Res.string.quest_generic_looks_like_this
    override val hintImages = listOf(
        Res.drawable.tactile_paving1,
        Res.drawable.tactile_paving2,
        Res.drawable.tactile_paving3
    )

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // The crossing quest covers both ends unless its value is no, partial, or incorrect.
        val coveredByCrossingNode = mapData.findCrosswalkEndNodeIdsCoveredByCrossingNode()
        return mapData.findAllKerbNodes().filter {
            it.id !in coveredByCrossingNode && kerbsWithUnknownTactilePavingFilter.matches(it)
        }
    }

    /* Editing a crossing may make its individual kerb quests applicable. */
    override fun mayAffectNearbyQuests(element: Element): Boolean =
        tactilePavingCrossingsFilter.matches(element)

    override fun isApplicableTo(element: Element): Boolean? =
        if (!kerbsWithUnknownTactilePavingFilter.matches(element) || element !is Node || !element.couldBeAKerb()) {
            false
        } else {
            null
        }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("tactile_paving", answer.toYesNo())
        if (tags["kerb"] != "no") {
            tags["barrier"] = "kerb"
        }
    }
}
