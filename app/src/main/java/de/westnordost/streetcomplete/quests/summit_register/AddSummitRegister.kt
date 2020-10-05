package de.westnordost.streetcomplete.quests.summit_register

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.elementfilter.getQuestPrintStatement
import de.westnordost.streetcomplete.data.elementfilter.toGlobalOverpassBBox
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddSummitRegister(
    private val overpassMapDataApi: OverpassMapDataAndGeometryApi,
    private val r: ResurveyIntervalsStore
) : OsmElementQuestType<Boolean> {

    override val commitMessage = "Add whether summit register is present"
    override val wikiLink = "Key:summit:register"
    override val icon = R.drawable.ic_quest_peak

    override val enabledInCountries = NoCountriesExcept(
        // regions gathered in
        // https://github.com/westnordost/StreetComplete/issues/561#issuecomment-325623974

        // Europe
        "AT", "DE", "CZ", "ES", "IT", "FR", "GR", "SI", "CH", "RO", "SK",

        //Americas
        "US", "AR", "PE"
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_summit_register_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassMapDataApi.query(getOverpassQuery(bbox), handler)
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer) {
            changes.updateWithCheckDate("summit:register", "yes")
        } else {
            changes.updateWithCheckDate("summit:register", "no")
        }
    }

    private fun getOverpassQuery(bbox: BoundingBox) = """
        ${bbox.toGlobalOverpassBBox()}

        (
            relation["route"="hiking"];
        )->.hiking;
        node(around.hiking:10)[natural=peak][!"summit:register"][name] -> .summits_with_unknown_status;
        node(around.hiking:10)["summit:register"][name]${olderThan(4).toOverpassQLString()} -> .summits_with_old_status;

        (.summits_with_unknown_status; .summits_with_old_status;);

        ${getQuestPrintStatement()}
        """.trimIndent()

    private fun olderThan(years: Int) =
        TagOlderThan("summit:register", RelativeDate(-(r * 365 * years).toFloat()))

}
