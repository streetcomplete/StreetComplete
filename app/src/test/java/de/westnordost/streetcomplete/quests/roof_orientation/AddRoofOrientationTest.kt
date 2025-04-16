package de.westnordost.streetcomplete.quests.roof_orientation

import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals

class AddRoofOrientationTest {
    private val questType = AddRoofOrientation()
    private val gabledRoofTags = mapOf("roof:shape" to "gabled", "building" to "yes")

    @Test fun `not applicable to way with wrong roof shape`() {
        val roof = way(tags = mapOf("roof:shape" to "skillion", "building" to "yes"))

        assertEquals(false, questType.isApplicableTo(roof))
        assertEquals(0, getApplicableElementsCount(roof, emptyList()))
    }

    @Test fun `not applicable to rectangular open way with 4 points`() {
        // https://osm.org/way/128296730
        val nodes = listOf(
            node(1, p(48.1035587, 11.7906178)),
            node(2, p(48.1035966, 11.7905417)),
            node(3, p(48.1036687, 11.7906222)),
            node(4, p(48.1036308, 11.7906984)),
        )
        val roof = way(
            tags = gabledRoofTags,
            nodes = listOf(1, 2, 3, 4),
        )

        assertEquals(null, questType.isApplicableTo(roof))
        assertEquals(0, getApplicableElementsCount(roof, nodes))
    }

    @Test fun `not applicable to way with 3 points`() {
        // https://osm.org/way/926313667
        val nodes = listOf(
            node(1, p(-33.9308156, 18.4423625)),
            node(2, p(-33.9309294, 18.4422969)),
            node(3, p(-33.9308989, 18.4422201)),
        )
        val roof = getGabledRoofWay(nodes)

        assertEquals(null, questType.isApplicableTo(roof))
        assertEquals(0, getApplicableElementsCount(roof, nodes))
    }

    @Test fun `applicable to rectangular way with 4 points`() {
        // https://osm.org/way/128296730
        val nodes = listOf(
            node(1, p(48.1035587, 11.7906178)),
            node(2, p(48.1035966, 11.7905417)),
            node(3, p(48.1036687, 11.7906222)),
            node(4, p(48.1036308, 11.7906984)),
        )
        val roof = getGabledRoofWay(nodes)

        assertEquals(null, questType.isApplicableTo(roof))
        assertEquals(1, getApplicableElementsCount(roof, nodes))
    }

    @Test fun `applicable to nearly rectangular way with 4 points`() {
        // https://osm.org/way/525058074
        val nodes = listOf(
            node(1, p(16.7519640, -93.1121874)),
            node(2, p(16.7518947, -93.1117797)),
            node(3, p(16.7516789, -93.1118173)),
            node(4, p(16.7517123, -93.1122384)),
        )
        val roof = getGabledRoofWay(nodes)

        assertEquals(null, questType.isApplicableTo(roof))
        assertEquals(1, getApplicableElementsCount(roof, nodes))
    }

    @Test fun `applicable to nearly rectangular way with 10 points`() {
        // https://osm.org/way/95462050
        val nodes = listOf(
            node(1, p(63.4345980, 10.3963050)),
            node(2, p(63.4348260, 10.3962940)),
            node(3, p(63.4348250, 10.3962860)),
            node(4, p(63.4348700, 10.3962830)),
            node(5, p(63.4348460, 10.3959370)),
            node(6, p(63.4345790, 10.3959740)),
            node(7, p(63.4345790, 10.3959620)),
            node(8, p(63.4345570, 10.3959660)),
            node(9, p(63.4345750, 10.3963040)),
        )
        val roof = getGabledRoofWay(nodes)

        assertEquals(null, questType.isApplicableTo(roof))
        assertEquals(1, getApplicableElementsCount(roof, nodes))
    }

    @Test fun `applicable to nearly rectangular way with 19 points`() {
        // https://osm.org/way/485537214
        val nodes = listOf(
            node(1, p(48.1387378, 11.7027654)),
            node(2, p(48.1383987, 11.7027700)),
            node(3, p(48.1381105, 11.7027740)),
            node(4, p(48.1378080, 11.7027781)),
            node(5, p(48.1374445, 11.7027831)),
            node(6, p(48.1374442, 11.7027269)),
            node(7, p(48.1374433, 11.7025754)),
            node(8, p(48.1374397, 11.7019867)),
            node(9, p(48.1374390, 11.7018660)),
            node(10, p(48.1374385, 11.7017975)),
            node(11, p(48.1374751, 11.7017970)),
            node(12, p(48.1375431, 11.7017961)),
            node(13, p(48.1378044, 11.7017925)),
            node(14, p(48.1381033, 11.7017884)),
            node(15, p(48.1383915, 11.7017845)),
            node(16, p(48.1386619, 11.7017808)),
            node(17, p(48.1387318, 11.7017799)),
            node(18, p(48.1387322, 11.7018418)),
            node(19, p(48.1387340, 11.7021518)),
        )
        val roof = getGabledRoofWay(nodes)

        assertEquals(null, questType.isApplicableTo(roof))
        assertEquals(1, getApplicableElementsCount(roof, nodes))
    }

    @Test fun `not applicable to way with 20 points`() {
        // exclude overly complex ways for performance reasons, see https://github.com/Helium314/SCEE/pull/733#issuecomment-2629260126

        // https://osm.org/way/17744441
        val nodes = listOf(
            node(1, p(48.1372581, 11.6972367)),
            node(2, p(48.1372652, 11.6972366)),
            node(3, p(48.1372652, 11.6972450)),
            node(4, p(48.1373350, 11.6972440)),
            node(5, p(48.1373353, 11.6972937)),
            node(6, p(48.1373361, 11.6974319)),
            node(7, p(48.1373395, 11.6980166)),
            node(8, p(48.1373403, 11.6981528)),
            node(9, p(48.1373406, 11.6982100)),
            node(10, p(48.1372714, 11.6982109)),
            node(11, p(48.1372715, 11.6982213)),
            node(12, p(48.1372639, 11.6982214)),
            node(13, p(48.1371946, 11.6982223)),
            node(14, p(48.1359528, 11.6982384)),
            node(15, p(48.1358848, 11.6982393)),
            node(16, p(48.1358790, 11.6972547)),
            node(17, p(48.1359450, 11.6972538)),
            node(18, p(48.1362410, 11.6972500)),
            node(19, p(48.1368952, 11.6972414)),
            node(20, p(48.1371937, 11.6972375)),
        )
        val roof = getGabledRoofWay(nodes)

        assertEquals(null, questType.isApplicableTo(roof))
        assertEquals(0, getApplicableElementsCount(roof, nodes))
    }

    @Test fun `not applicable to nearly square way`() {
        // https://osm.org/way/93389455
        val nodes = listOf(
            node(1, p(63.4337422, 10.3921189)),
            node(2, p(63.4337378, 10.3921049)),
            node(3, p(63.4336683, 10.3922129)),
            node(4, p(63.4337977, 10.3926291)),
            node(5, p(63.4339881, 10.3923332)),
            node(6, p(63.4338594, 10.3919193)),
            node(7, p(63.4338185, 10.3919829)),
            node(8, p(63.4338221, 10.3919947)),
        )
        val roof = getGabledRoofWay(nodes)

        assertEquals(null, questType.isApplicableTo(roof))
        assertEquals(0, getApplicableElementsCount(roof, nodes))
    }

    @Test fun `not applicable to L-shaped way`() {
        // https://osm.org/way/790940999
        val nodes = listOf(
            node(1, p(-46.4108772, 168.3546656)),
            node(2, p(-46.4112776, 168.3546657)),
            node(3, p(-46.4112776, 168.3549029)),
            node(4, p(-46.4112213, 168.3549029)),
            node(5, p(-46.4112214, 168.3547473)),
            node(6, p(-46.4108771, 168.3547473)),
        )
        val roof = getGabledRoofWay(nodes)

        assertEquals(null, questType.isApplicableTo(roof))
        assertEquals(0, getApplicableElementsCount(roof, nodes))
    }

    @Test fun `not applicable to weirdly shaped way`() {
        // https://osm.org/way/128115590
        val nodes = listOf(
            node(1, p(48.1037946, 11.7917596)),
            node(2, p(48.1038334, 11.7916651)),
            node(3, p(48.1038606, 11.7916902)),
            node(4, p(48.1038969, 11.7916021)),
            node(5, p(48.1039727, 11.7916720)),
            node(6, p(48.1039533, 11.7917191)),
            node(7, p(48.1039284, 11.7916961)),
            node(8, p(48.1039075, 11.7917470)),
            node(9, p(48.1039445, 11.7917811)),
            node(10, p(48.1039097, 11.7918657)),
        )
        val roof = getGabledRoofWay(nodes)

        assertEquals(null, questType.isApplicableTo(roof))
        assertEquals(0, getApplicableElementsCount(roof, nodes))
    }

    private fun getGabledRoofWay(nodes: List<Node>) = way(
        tags = gabledRoofTags,
        nodes = nodes.map { it.id } + nodes.first().id
    )

    private fun getApplicableElementsCount(roof: Way, nodes: List<Node>): Int {
        val mapData = TestMapDataWithGeometry(nodes + roof)
        return questType.getApplicableElements(mapData).toList().size
    }
}
