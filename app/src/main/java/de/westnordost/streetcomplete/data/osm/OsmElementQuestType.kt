package de.westnordost.streetcomplete.data.osm

import android.os.Bundle

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler

interface OsmElementQuestType : QuestType {

	/** the commit message to be used for this quest type */
	val commitMessage: String

	// the below could also go up into QuestType interface, but then they should be accounted for
	// in the respective download/upload classes as well

	/** in which countries the quest should be shown */
	val enabledForCountries: Countries get() = Countries.ALL

	/** @return whether the markers should be at the ends instead of the center
	 */
	val hasMarkersAtEnds: Boolean get() = false

	/** @return title resource for when the element has the specified tags. The tags are unmodifiable */
	fun getTitle(tags: Map<String, String>): Int

	override val title: Int get() = getTitle(emptyMap())

	/** Downloads map data for this quest type
	 *
	 * @param bbox the area in which it should be downloaded
	 * @param handler called for each element for which this quest type applies
	 * @return true if successful (false if interrupted)
	 */
	fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean

	/** whether a quest of this quest type could be created out of the given element. If the
	 * element alone does not suffice to find this out, this should return null  */
	fun isApplicableTo(element: Element): Boolean?

	/** applies the data from answer to the given element  */
	fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder)

	/** The quest type can clean it's metadata here, if any  */
	fun cleanMetadata() {}
}
