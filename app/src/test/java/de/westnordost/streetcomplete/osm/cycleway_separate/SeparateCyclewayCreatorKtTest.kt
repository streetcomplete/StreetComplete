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
import kotlin.test.assertTrue

class SeparateCyclewayCreatorKtTest {
    @Test fun `apply not allowed`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "no"),
                StringMapEntryAdd("bicycle:signed", "yes"),
                StringMapEntryModify("highway", "cycleway", "path")
            ),
            NOT_ALLOWED.appliedTo(mapOf("highway" to "cycleway"))
        )
    }

    @Test fun `apply not allowed does not change bicycle=dismount`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle:signed", "yes"),
                StringMapEntryModify("highway", "cycleway", "path")
            ),
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "cycleway",
                "bicycle" to "dismount"
            ))
        )
    }

    @Test fun `apply not allowed re-tags cycleway and adds foot=yes if foot was no before`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "no"),
                StringMapEntryAdd("bicycle:signed", "yes"),
                StringMapEntryModify("highway", "cycleway", "path"),
                StringMapEntryModify("foot", "no", "yes")
            ),
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "no"
            ))
        )
    }

    @Test fun `apply not allowed re-tags cycleway to footway if foot is designated`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "no"),
                StringMapEntryAdd("bicycle:signed", "yes"),
                StringMapEntryModify("highway", "cycleway", "footway")
            ),
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "designated"
            ))
        )
    }

    @Test fun `apply not allowed removes sidewalk and segregated tags`() {
        assertTrue(
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk" to "both",
            )).containsAll(
                setOf(
                    StringMapEntryAdd("bicycle", "no"),
                    StringMapEntryDelete("segregated", "yes"),
                    StringMapEntryDelete("sidewalk", "both"),
                )
            )
        )
        assertTrue(
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk:both" to "yes",
            )).containsAll(
                setOf(
                    StringMapEntryAdd("bicycle", "no"),
                    StringMapEntryDelete("segregated", "yes"),
                    StringMapEntryDelete("sidewalk:both", "yes"),
                )
            )
        )
        assertTrue(
            NOT_ALLOWED.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            )).containsAll(
                setOf(
                    StringMapEntryAdd("bicycle", "no"),
                    StringMapEntryDelete("segregated", "yes"),
                    StringMapEntryDelete("sidewalk:left", "yes"),
                    StringMapEntryDelete("sidewalk:right", "yes"),
                )
            )
        )
    }

    @Test fun `apply allowed`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("bicycle", "yes"),
                StringMapEntryAdd("bicycle:signed", "yes"),
                StringMapEntryModify("highway", "footway", "footway")
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("bicycle", "no", "yes"),
                StringMapEntryAdd("bicycle:signed", "yes"),
                StringMapEntryModify("highway", "footway", "footway")
            ),
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "bicycle" to "no"
            ))
        )
    }

    @Test fun `apply allowed does not re-tag bicycle=permissive etc`() {
        assertTrue(
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "bicycle" to "permissive"
            )).containsAll(
                setOf(
                    StringMapEntryModify("bicycle", "permissive", "yes"),
                    StringMapEntryAdd("bicycle:signed", "yes")
                )
            )
        )
        assertTrue(
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "bicycle" to "private"
            )).containsAll(
                setOf(
                    StringMapEntryModify("bicycle", "private", "yes"),
                    StringMapEntryAdd("bicycle:signed", "yes")
                )
            )
        )
    }

    @Test fun `apply allowed re-tags cycleway and adds foot=yes if foot was not yes before`() {
        assertTrue(
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "no"
            )).containsAll(
                setOf(
                    StringMapEntryModify("highway", "cycleway", "footway"),
                    StringMapEntryModify("foot", "no", "designated")
                )
            )
        )
    }

    @Test fun `apply allowed re-tags cycleway to footway if foot is designated`() {
        assertTrue(
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "cycleway",
                "foot" to "designated"
            )).containsAll(
                setOf(
                    StringMapEntryModify("highway", "cycleway", "footway"),
                    StringMapEntryModify("foot", "designated", "designated")
                )
            )
        )
    }

    @Test fun `apply allowed removes sidewalk and segregated tags`() {
        assertTrue(
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk" to "both",
            )).containsAll(
                setOf(
                    StringMapEntryModify("highway", "footway", "footway"),
                    StringMapEntryDelete("segregated", "yes"),
                    StringMapEntryDelete("sidewalk", "both"),
                )
            )
        )
        assertTrue(
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk:both" to "yes",
            )).containsAll(
                setOf(
                    StringMapEntryModify("highway", "footway", "footway"),
                    StringMapEntryDelete("segregated", "yes"),
                    StringMapEntryDelete("sidewalk:both", "yes"),
                )
            )
        )
        assertTrue(
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "footway",
                "segregated" to "yes",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            )).containsAll(
                setOf(
                    StringMapEntryModify("highway", "footway", "footway"),
                    StringMapEntryDelete("segregated", "yes"),
                    StringMapEntryDelete("sidewalk:left", "yes"),
                    StringMapEntryDelete("sidewalk:right", "yes"),
                )
            )
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
                StringMapEntryModify("foot", "yes", "designated")
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
            setOf(StringMapEntryModify("highway", "cycleway", "path")),
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
            setOf(StringMapEntryModify("highway", "cycleway", "path")),
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

    @Test fun `apply allowed on footway changes highway to footway if it isnt already`() {
        assertTrue(
            ALLOWED_ON_FOOTWAY.appliedTo(mapOf(
                "highway" to "path",
            )).contains(
                StringMapEntryModify("highway", "path", "footway")
            )
        )
    }

    @Test fun `apply path removes bicycle signed tag`() {
        assertTrue(
            PATH.appliedTo(
                mapOf(
                    "highway" to "cycleway",
                    "bicycle:signed" to "yes",
                )
            ).contains(
                StringMapEntryDelete("bicycle:signed", "yes")
            )
        )
    }

    @Test fun `apply non-designated on footway also removes bicycle signed tag`() {
        assertTrue(
            NON_DESIGNATED_ON_FOOTWAY.appliedTo(
                mapOf(
                    "highway" to "footway",
                    "bicycle:signed" to "yes",
                )
            ).contains(
                StringMapEntryDelete("bicycle:signed", "yes")
            )
        )
    }

    @Test fun `apply allowed on footway adds the bicycle signed tag`() {
        assertTrue(
            ALLOWED_ON_FOOTWAY.appliedTo(
                mapOf(
                    "highway" to "footway",
                )
            ).contains(
                StringMapEntryAdd("bicycle:signed", "yes")
            )
        )
    }

    @Test
    fun `apply disallowed adds bicycle signed tag`() {
        assertTrue(
            NOT_ALLOWED.appliedTo(
                mapOf(
                    "highway" to "path",
                )
            ).contains(
                StringMapEntryAdd("bicycle:signed", "yes")
            )
        )
    }

    @Test
    fun `apply exclusive with sidewalk sets bicycle signed to yes if its currently no`() {
        assertTrue(
            EXCLUSIVE_WITH_SIDEWALK.appliedTo(
                mapOf(
                    "highway" to "cycleway",
                    "bicycle:signed" to "no",
                )
            ).contains(
                StringMapEntryModify("bicycle:signed", "no", "yes")
            )
        )
    }
}

private fun SeparateCycleway.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
