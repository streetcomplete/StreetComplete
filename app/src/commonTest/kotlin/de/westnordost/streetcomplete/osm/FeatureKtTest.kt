package de.westnordost.streetcomplete.osm

import de.westnordost.osmfeatures.BaseFeature
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import kotlin.test.Test
import kotlin.test.assertEquals

class FeatureKtTest {

    @Test
    fun `adds tags`() {
        assertEquals(
            setOf(StringMapEntryAdd("a", "b")),
            feature(addTags = mapOf("a" to "b")).appliedTo(mapOf())
        )

        assertEquals(
            setOf(StringMapEntryAdd("a", "yes")),
            feature(addTagKeys = setOf("a")).appliedTo(mapOf())
        )
    }

    @Test
    fun `overwrites tags`() {
        assertEquals(
            setOf(StringMapEntryModify("a", "x", "b")),
            feature(addTags = mapOf("a" to "b")).appliedTo(mapOf("a" to "x"))
        )
        assertEquals(
            setOf(StringMapEntryModify("a", "x", "yes")),
            feature(addTagKeys = setOf("a")).appliedTo(mapOf("a" to "x"))
        )
    }

    @Test
    fun `preserve tags has no effect on adding tags`() {
        assertEquals(
            setOf(StringMapEntryAdd("a", "b")),
            feature(
                addTags = mapOf("a" to "b"),
                preserveTags = listOf(Regex("a"))
            ).appliedTo(mapOf())
        )

        assertEquals(
            setOf(StringMapEntryAdd("a", "yes")),
            feature(
                addTagKeys = setOf("a"),
                preserveTags = listOf(Regex("a"))
            ).appliedTo(mapOf())
        )
    }

    @Test
    fun `preserve tags does not allow overwriting tags`() {
        assertEquals(
            setOf(),
            feature(
                addTags = mapOf("a" to "b"),
                preserveTags = listOf(Regex("a"))
            ).appliedTo(mapOf("a" to "keep this"))
        )

        assertEquals(
            setOf(),
            feature(
                addTagKeys = setOf("a"),
                preserveTags = listOf(Regex("a"))
            ).appliedTo(mapOf("a" to "keep this"))
        )
    }

    @Test
    fun `removes tags of previous feature`() {
        val previousFeature = feature(
            removeTags = mapOf("a" to "x", "b" to "y"),
            removeTagKeys = setOf("c")
        )

        assertEquals(
            setOf(
                StringMapEntryDelete("a", "x"),
                StringMapEntryDelete("c", "anything"),
                // not "b", because it has a different value than expected
            ),
            feature().appliedTo(
                tags = mapOf("a" to "x", "b" to "different", "c" to "anything"),
                previousFeature = previousFeature
            )
        )
    }
}

private fun feature(
    addTags: Map<String, String> = mapOf(),
    removeTags: Map<String, String> = addTags,
    preserveTags: List<Regex> = listOf(),
    addTagKeys: Set<String> = setOf(),
    removeTagKeys: Set<String> = addTagKeys
): Feature = BaseFeature(
    id = "id",
    names = listOf("name"),
    geometry = listOf(GeometryType.POINT),
    tags = addTags,
    addTags = addTags,
    removeTags = removeTags,
    preserveTags = preserveTags,
    tagKeys = addTagKeys,
    addTagKeys = addTagKeys,
    removeTagKeys = removeTagKeys
)

private fun Feature.appliedTo(
    tags: Map<String, String>,
    previousFeature: Feature? = null
): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb, previousFeature)
    return cb.create().changes
}
