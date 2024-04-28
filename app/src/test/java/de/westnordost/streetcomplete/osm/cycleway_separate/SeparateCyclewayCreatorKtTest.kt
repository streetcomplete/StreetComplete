package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals

class SeparateCyclewayCreatorKtTest {
    @Test fun `apply none`() {
        assertEquals(
            setOf(StringMapEntryAdd("bicycle", "no")),
            NOT_ALLOWED.appliedTo(mapOf("highway" to "footway"))
        )
    }

    @Test fun `apply none does not change bicycle=dismount`() {
        assertEquals(
            setOf(StringMapEntryModify("highway", "cycleway", "path"),),
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "cycleway",
                "bicycle" to "dismount"
            ))
        )
    }

    @Test fun `apply none re-tags cycleway and adds foot=yes if foot was no before`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "no"),
                StringMapEntryModify("highway", "cycleway", "path"),
                StringMapEntryModify("foot", "no", "yes")
            ),
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "no"
            ))
        )
    }

    @Test fun `apply none re-tags cycleway to footway if foot is designated`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "no"),
                StringMapEntryModify("highway", "cycleway", "footway")
            ),
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "designated"
            ))
        )
    }

    @Test fun `apply none removes sidewalk and segregated tags`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "no"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk", "both"),
            ),
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk" to "both",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "no"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk:both", "yes"),
            ),
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk:both" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "no"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
            ),
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            ))
        )
    }

    @Test fun `apply allowed`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "yes"),
                StringMapEntryModify("highway", "footway", "footway")
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("bicycle", "no", "yes"),
                StringMapEntryModify("highway", "footway", "footway")
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "bicycle" to "no"
            ))
        )
    }

    @Test fun `apply allowed does not re-tag bicycle=permissive etc`() {
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "footway", "footway"),
                StringMapEntryAdd("check_date:bicycle", nowAsCheckDateString())
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "bicycle" to "permissive"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "footway", "footway"),
                StringMapEntryAdd("check_date:bicycle", nowAsCheckDateString())
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "bicycle" to "private"
            ))
        )
    }

    @Test fun `apply allowed re-tags cycleway and adds foot=yes if foot was not yes before`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "yes"),
                StringMapEntryModify("highway", "cycleway", "footway"),
                StringMapEntryModify("foot", "no", "designated")
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "no"
            ))
        )
    }

    @Test fun `apply allowed re-tags cycleway to footway if foot is designated`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "yes"),
                StringMapEntryModify("highway", "cycleway", "footway"),
                StringMapEntryModify("foot", "designated", "designated"),
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "designated"
            ))
        )
    }

    @Test fun `apply allowed removes sidewalk and segregated tags`() {
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "footway", "footway"),
                StringMapEntryAdd("bicycle", "yes"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk", "both"),
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk" to "both",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "footway", "footway"),
                StringMapEntryAdd("bicycle", "yes"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk:both", "yes"),
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk:both" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "footway", "footway"),
                StringMapEntryAdd("bicycle", "yes"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            ))
        )
    }

    @Test fun `apply non-designated`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("check_date:bicycle", nowAsCheckDateString()),
                StringMapEntryModify("highway", "footway", "footway"),
            ),
            NON_DESIGNATED_ON_FOOTWAY.appliedTo(mapOf("highway" to "footway"))
        )
    }

    @Test fun `apply non-designated does not change bicycle tag unless it is designated`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("bicycle", "designated"),
                StringMapEntryModify("highway", "footway", "footway"),
            ),
            NON_DESIGNATED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "bicycle" to "designated"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "footway", "footway"),
                StringMapEntryAdd("check_date:bicycle", nowAsCheckDateString())
            ),
            NON_DESIGNATED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "bicycle" to "yes"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "footway", "footway"),
                StringMapEntryAdd("check_date:bicycle", nowAsCheckDateString())
            ),
            NON_DESIGNATED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "bicycle" to "no"
            ))
        )
    }

    @Test fun `apply non-designated re-tags cycleway and adds foot=yes if foot was not yes before`() {
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "cycleway", "footway"),
                StringMapEntryModify("foot", "no", "designated")
            ),
            NON_DESIGNATED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "no"
            ))
        )
    }

    @Test fun `apply non-designated re-tags cycleway to footway if foot is designated`() {
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "cycleway", "footway"),
                StringMapEntryModify("foot", "designated", "designated"),
            ),
            NON_DESIGNATED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "designated"
            )),
        )
    }

    @Test fun `apply non-designated removes sidewalk and segregated tags`() {
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "footway", "footway"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk", "both"),
            ),
            NON_DESIGNATED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk" to "both",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "footway", "footway"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
            ),
            NON_DESIGNATED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "footway", "footway"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk:both", "yes"),
            ),
            NON_DESIGNATED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk:both" to "yes",
            ))
        )
    }

    @Test fun `apply non-segregated`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "designated"),
                StringMapEntryAdd("segregated", "no"),
            ),
            NON_SEGREGATED.appliedTo(mapOf("highway" to "footway"))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "designated"),
                StringMapEntryAdd("segregated", "no"),
            ),
            NON_SEGREGATED.appliedTo(mapOf(
                "highway" to "footway",
                "foot" to "yes"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("foot", "designated"),
                StringMapEntryAdd("segregated", "no"),
            ),
            NON_SEGREGATED.appliedTo(mapOf("highway" to "cycleway"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("bicycle", "yes", "designated"),
                StringMapEntryAdd("foot", "designated"),
                StringMapEntryAdd("segregated", "no"),
            ),
            NON_SEGREGATED.appliedTo(mapOf(
                "highway" to "cycleway",
                "bicycle" to "yes"
            ))
        )
    }

    @Test fun `apply non-segregated does not re-tag any yes-like value`() {
        assertEquals(
            setOf(StringMapEntryAdd("segregated", "no")),
            NON_SEGREGATED.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "yes"
            ))
        )
        assertEquals(
            setOf(StringMapEntryAdd("segregated", "no"),),
            NON_SEGREGATED.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "permissive"
            ))
        )
    }

    @Test fun `apply non-segregated removes sidewalk tags`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "designated"),
                StringMapEntryAdd("segregated", "no"),
                StringMapEntryDelete("sidewalk", "both"),
            ),
            NON_SEGREGATED.appliedTo(mapOf(
                "highway" to "footway",
                "sidewalk" to "both",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "designated"),
                StringMapEntryAdd("segregated", "no"),
                StringMapEntryDelete("sidewalk:both", "yes"),
            ),
            NON_SEGREGATED.appliedTo(mapOf(
                "highway" to "footway",
                "sidewalk:both" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "designated"),
                StringMapEntryAdd("segregated", "no"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
            ),
            NON_SEGREGATED.appliedTo(mapOf(
                "highway" to "footway",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            ))
        )
    }

    @Test fun `apply segregated`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "designated"),
                StringMapEntryAdd("segregated", "yes"),
            ),
            SEGREGATED.appliedTo(mapOf("highway" to "footway"))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("foot", "designated"),
                StringMapEntryAdd("segregated", "yes"),
            ),
            SEGREGATED.appliedTo(mapOf("highway" to "cycleway"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("bicycle", "yes", "designated"),
                StringMapEntryAdd("foot", "designated"),
                StringMapEntryAdd("segregated", "yes"),
            ),
            SEGREGATED.appliedTo(mapOf("highway" to "cycleway", "bicycle" to "yes"))
        )
    }

    @Test fun `apply segregated removes sidewalk tags`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "designated"),
                StringMapEntryAdd("segregated", "yes"),
                StringMapEntryDelete("sidewalk", "both"),
            ),
            SEGREGATED.appliedTo(mapOf(
                "highway" to "footway",
                "sidewalk" to "both",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "designated"),
                StringMapEntryAdd("segregated", "yes"),
                StringMapEntryDelete("sidewalk:both", "yes"),
            ),
            SEGREGATED.appliedTo(mapOf(
                "highway" to "footway",
                "sidewalk:both" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "designated"),
                StringMapEntryAdd("segregated", "yes"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
            ),
            SEGREGATED.appliedTo(mapOf(
                "highway" to "footway",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            ))
        )
    }

    @Test fun `apply exclusive`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("foot", "no"),
                StringMapEntryModify("highway", "cycleway", "cycleway"),
            ),
            EXCLUSIVE.appliedTo(mapOf("highway" to "cycleway"))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("foot", "no"),
                StringMapEntryModify("highway", "footway", "cycleway"),
            ),
            EXCLUSIVE.appliedTo(mapOf("highway" to "footway"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("foot", "yes", "no"),
                StringMapEntryModify("highway", "path", "cycleway"),
            ),
            EXCLUSIVE.appliedTo(mapOf(
                "highway" to "path",
                "foot" to "yes"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("foot", "no"),
                StringMapEntryModify("bicycle", "yes", "designated"),
                StringMapEntryModify("highway", "cycleway", "cycleway"),
            ),
            EXCLUSIVE.appliedTo(mapOf(
                "highway" to "cycleway",
                "bicycle" to "yes"
            ))
        )
    }

    @Test fun `apply exclusive removes sidewalk and segregated tags`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("foot", "no"),
                StringMapEntryModify("highway", "cycleway", "cycleway"),
                StringMapEntryDelete("sidewalk", "both"),
                StringMapEntryDelete("segregated", "yes"),
            ),
            EXCLUSIVE.appliedTo(mapOf(
                "highway" to "cycleway",
                "sidewalk" to "both",
                "segregated" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("foot", "no"),
                StringMapEntryModify("highway", "cycleway", "cycleway"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk:both", "yes"),
            ),
            EXCLUSIVE.appliedTo(mapOf(
                "highway" to "cycleway",
                "segregated" to "yes",
                "sidewalk:both" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("foot", "no"),
                StringMapEntryModify("highway", "cycleway", "cycleway"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
            ),
            EXCLUSIVE.appliedTo(mapOf(
                "highway" to "cycleway",
                "segregated" to "yes",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            ))
        )
    }

    @Test fun `apply exclusive with sidewalk`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk", "yes"),
                StringMapEntryModify("highway", "cycleway", "cycleway"),
            ),
            EXCLUSIVE_WITH_SIDEWALK.appliedTo(mapOf("highway" to "cycleway"))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk", "yes"),
                StringMapEntryDelete("foot", "yes"),
                StringMapEntryModify("highway", "cycleway", "cycleway"),
            ),
            EXCLUSIVE_WITH_SIDEWALK.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "yes"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk", "yes"),
                StringMapEntryModify("highway", "footway", "cycleway"),
            ),
            EXCLUSIVE_WITH_SIDEWALK.appliedTo(mapOf("highway" to "footway"))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk", "yes"),
                StringMapEntryModify("highway", "path", "cycleway"),
            ),
            EXCLUSIVE_WITH_SIDEWALK.appliedTo(mapOf("highway" to "path"))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("sidewalk", "yes"),
                StringMapEntryModify("bicycle", "yes", "designated"),
                StringMapEntryModify("highway", "path", "cycleway"),
            ),
            EXCLUSIVE_WITH_SIDEWALK.appliedTo(mapOf(
                "highway" to "path",
                "bicycle" to "yes"
            ))
        )
    }

    @Test fun `apply exclusive with sidewalk does not touch sidewalk tags if a sidewalk is already tagged`() {
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "cycleway", "cycleway"),
                StringMapEntryAdd("check_date:bicycle", nowAsCheckDateString())
            ),
            EXCLUSIVE_WITH_SIDEWALK.appliedTo(mapOf(
                "highway" to "cycleway",
                "sidewalk" to "both"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "cycleway", "cycleway"),
                StringMapEntryAdd("check_date:bicycle", nowAsCheckDateString())
            ),
            EXCLUSIVE_WITH_SIDEWALK.appliedTo(mapOf(
                "highway" to "cycleway",
                "sidewalk:left" to "yes"
            ))
        )
    }

    @Test fun `apply exclusive with sidewalk removes segregated tags`() {
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "cycleway", "cycleway"),
                StringMapEntryDelete("segregated", "yes"),
            ),
            EXCLUSIVE_WITH_SIDEWALK.appliedTo(mapOf(
                "highway" to "cycleway",
                "sidewalk" to "both",
                "segregated" to "yes"
            ))
        )
    }

    @Test fun `apply path changes highway tag`() {
        assertEquals(
            setOf(StringMapEntryModify("highway", "cycleway", "path"),),
            PATH.appliedTo(mapOf("highway" to "cycleway")),
        )
    }

    @Test fun `apply path sets foot and bicycle if set`() {
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "cycleway", "path"),
                StringMapEntryModify("foot", "designated", "yes"),
                StringMapEntryModify("bicycle", "no", "yes"),
            ),
            PATH.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "designated",
                "bicycle" to "no",
            ))
        )
    }

    @Test fun `apply path sets foot and bicycle only if not a value like permissive, private etc is set`() {
        assertEquals(
            setOf(StringMapEntryModify("highway", "cycleway", "path")),
            PATH.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "private",
                "bicycle" to "permissive",
            ))
        )
        assertEquals(
            setOf(StringMapEntryModify("highway", "cycleway", "path"),),
            PATH.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "destination",
                "bicycle" to "customers",
            ))
        )
    }

    @Test fun `apply path removes segregated and sidewalk`() {
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "cycleway", "path"),
                StringMapEntryDelete("sidewalk", "both"),
                StringMapEntryDelete("segregated", "yes"),
            ),
            PATH.appliedTo(mapOf(
                "highway" to "cycleway",
                "sidewalk" to "both",
                "segregated" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "cycleway", "path"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk:both", "yes"),
            ),
            PATH.appliedTo(mapOf(
                "highway" to "cycleway",
                "segregated" to "yes",
                "sidewalk:both" to "yes",
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("highway", "cycleway", "path"),
                StringMapEntryDelete("segregated", "yes"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
            ),
            PATH.appliedTo(mapOf(
                "highway" to "cycleway",
                "segregated" to "yes",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            ))
        )
    }
}

private fun SeparateCycleway.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
