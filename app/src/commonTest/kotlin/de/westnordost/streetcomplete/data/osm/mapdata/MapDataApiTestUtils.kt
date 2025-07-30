package de.westnordost.streetcomplete.data.osm.mapdata

import kotlinx.datetime.Instant

fun nodesOsm(c: Long): String = """
        <node id="122" version="2" changeset="$c" timestamp="2019-03-15T01:52:26Z" lat="53.0098761" lon="9.0065254" />
        <node id="123" version="1" changeset="$c" timestamp="2019-03-15T01:52:25Z" lat="53.009876" lon="9.0065253">
        <tag k="emergency" v="fire_hydrant" />
        <tag k="fire_hydrant:type" v="pillar" />
        </node>
    """

fun waysOsm(c: Long): String = """
        <way id="336145990" version="20" changeset="$c" timestamp="2018-10-17T06:39:01Z" />
        <way id="47076097" version="2" changeset="$c" timestamp="2012-08-12T22:14:39Z">
        <nd ref="600397018" />
        <nd ref="600397019" />
        <nd ref="600397020" />
        <tag k="landuse" v="farmland" />
        <tag k="name" v="Hippiefarm" />
        </way>
    """

fun relationsOsm(c: Long): String = """
        <relation id="55555" version="3" changeset="$c" timestamp="2021-05-08T14:14:51Z" />
        <relation id="8379313" version="21" changeset="$c" timestamp="2023-05-08T14:14:51Z">
        <member type="node" ref="123" role="something" />
        <member type="way" ref="234" role="" />
        <member type="relation" ref="345" role="connection" />
        <tag k="network" v="rcn" />
        <tag k="route" v="bicycle" />
        </relation>
    """

val nodesList = listOf(
    Node(
        id = 122,
        position = LatLon(53.0098761, 9.0065254),
        tags = emptyMap(),
        version = 2,
        timestampEdited = Instant.parse("2019-03-15T01:52:26Z").toEpochMilliseconds()
    ),
    Node(
        id = 123,
        position = LatLon(53.0098760, 9.0065253),
        tags = mapOf("emergency" to "fire_hydrant", "fire_hydrant:type" to "pillar"),
        version = 1,
        timestampEdited = Instant.parse("2019-03-15T01:52:25Z").toEpochMilliseconds()
    ),
)

val waysList = listOf(
    Way(
        id = 336145990,
        nodeIds = emptyList(),
        tags = emptyMap(),
        version = 20,
        timestampEdited = Instant.parse("2018-10-17T06:39:01Z").toEpochMilliseconds()
    ),
    Way(
        id = 47076097,
        nodeIds = listOf(600397018, 600397019, 600397020),
        tags = mapOf("landuse" to "farmland", "name" to "Hippiefarm"),
        version = 2,
        timestampEdited = Instant.parse("2012-08-12T22:14:39Z").toEpochMilliseconds()
    ),
)

val relationsList = listOf(
    Relation(
        id = 55555,
        members = emptyList(),
        tags = emptyMap(),
        version = 3,
        timestampEdited = Instant.parse("2021-05-08T14:14:51Z").toEpochMilliseconds()
    ),
    Relation(
        id = 8379313,
        members = listOf(
            RelationMember(ElementType.NODE, 123, "something"),
            RelationMember(ElementType.WAY, 234, ""),
            RelationMember(ElementType.RELATION, 345, "connection"),
        ),
        tags = mapOf("network" to "rcn", "route" to "bicycle"),
        version = 21,
        timestampEdited = Instant.parse("2023-05-08T14:14:51Z").toEpochMilliseconds()
    )
)
