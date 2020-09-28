package de.westnordost.streetcomplete.quests

import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.data.meta.toCheckDate
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.quests.traffic_signals_blind.AddTrafficSignalsBlindFeatures
import de.westnordost.streetcomplete.quests.traffic_signals_blind.TrafficSignalsBlindFeaturesAnswer
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import java.util.*

class AddTrafficSignalsBlindFeaturesTest {

    private lateinit var questType: AddTrafficSignalsBlindFeatures

    @Before fun setUp() {
        val r: ResurveyIntervalsStore = mock()
        on(r.times(ArgumentMatchers.anyInt())).thenAnswer { (it.arguments[0] as Int).toDouble() }
        on(r.times(ArgumentMatchers.anyDouble())).thenAnswer { (it.arguments[0] as Double) }
        questType = AddTrafficSignalsBlindFeatures(mock(), r)
    }

    @Test fun `apply only sound answer`() {
        questType.verifyAnswer(
            TrafficSignalsBlindFeaturesAnswer(sound = true, vibration = false, arrow = false),
            StringMapEntryAdd("traffic_signals:sound", "yes"),
            StringMapEntryAdd("traffic_signals:vibration", "no"),
            StringMapEntryAdd("traffic_signals:arrow", "no")
        )
    }

    @Test fun `apply only vibration answer`() {
        questType.verifyAnswer(
            TrafficSignalsBlindFeaturesAnswer(sound = false, vibration = true, arrow = false),
            StringMapEntryAdd("traffic_signals:sound", "no"),
            StringMapEntryAdd("traffic_signals:vibration", "yes"),
            StringMapEntryAdd("traffic_signals:arrow", "no")
        )
    }

    @Test fun `apply only arrow answer`() {
        questType.verifyAnswer(
            TrafficSignalsBlindFeaturesAnswer(sound = false, vibration = false, arrow = true),
            StringMapEntryAdd("traffic_signals:sound", "no"),
            StringMapEntryAdd("traffic_signals:vibration", "no"),
            StringMapEntryAdd("traffic_signals:arrow", "yes")
        )
    }

    @Test fun `apply none answer`() {
        questType.verifyAnswer(
            TrafficSignalsBlindFeaturesAnswer(sound = false, vibration = false, arrow = false),
            StringMapEntryAdd("traffic_signals:sound", "no"),
            StringMapEntryAdd("traffic_signals:vibration", "no"),
            StringMapEntryAdd("traffic_signals:arrow", "no")
        )
    }

    @Test fun `apply updated answer`() {
        questType.verifyAnswer(
            mapOf("traffic_signals:sound" to "yes"),
            TrafficSignalsBlindFeaturesAnswer(sound = false, vibration = true, arrow = true),
            StringMapEntryModify("traffic_signals:sound", "yes", "no"),
            StringMapEntryAdd("traffic_signals:vibration", "yes"),
            StringMapEntryAdd("traffic_signals:arrow", "yes")
        )
    }

    @Test fun `apply updated answer on all three properties`() {
        questType.verifyAnswer(
            mapOf(
                "traffic_signals:sound" to "yes",
                "traffic_signals:vibration" to "no",
                "traffic_signals:arrow" to "no"
            ),
            TrafficSignalsBlindFeaturesAnswer(sound = false, vibration = true, arrow = false),
            StringMapEntryModify("traffic_signals:sound", "yes", "no"),
            StringMapEntryModify("traffic_signals:vibration", "no", "yes"),
            StringMapEntryModify("traffic_signals:arrow", "no", "no")
        )
    }

    @Test fun `apply update removes check date if something changed`() {
        questType.verifyAnswer(
            mapOf(
                "traffic_signals:sound" to "no",
                "traffic_signals:vibration" to "no",
                "traffic_signals:arrow" to "no",
                "traffic_signals:check_date" to "2000-11-01",
                "check_date:traffic_signals" to "2000-11-02",
                "traffic_signals:lastcheck" to "2000-11-03",
                "lastcheck:traffic_signals" to "2000-11-04",
                "traffic_signals:last_checked" to "2000-11-05",
                "last_checked:traffic_signals" to "2000-11-06"
            ),
            TrafficSignalsBlindFeaturesAnswer(sound = true, vibration = false, arrow = false),
            StringMapEntryModify("traffic_signals:sound", "no", "yes"),
            StringMapEntryModify("traffic_signals:vibration", "no", "no"),
            StringMapEntryModify("traffic_signals:arrow", "no", "no"),
            StringMapEntryDelete("traffic_signals:check_date", "2000-11-01"),
            StringMapEntryDelete("check_date:traffic_signals", "2000-11-02"),
            StringMapEntryDelete("traffic_signals:lastcheck", "2000-11-03"),
            StringMapEntryDelete("lastcheck:traffic_signals", "2000-11-04"),
            StringMapEntryDelete("traffic_signals:last_checked", "2000-11-05"),
            StringMapEntryDelete("last_checked:traffic_signals", "2000-11-06")
        )
    }

    @Test fun `apply updated answer but nothing changed`() {
        questType.verifyAnswer(
            mapOf(
                "traffic_signals:sound" to "no",
                "traffic_signals:vibration" to "no",
                "traffic_signals:arrow" to "no"
            ),
            TrafficSignalsBlindFeaturesAnswer(sound = false, vibration = false, arrow = false),
            StringMapEntryModify("traffic_signals:sound", "no", "no"),
            StringMapEntryModify("traffic_signals:vibration", "no", "no"),
            StringMapEntryModify("traffic_signals:arrow", "no", "no"),
            StringMapEntryAdd("check_date:traffic_signals", Date().toCheckDateString())
        )
    }

    @Test fun `apply updated answer but nothing changed with preexisting check date`() {
        questType.verifyAnswer(
            mapOf(
                "traffic_signals:sound" to "no",
                "traffic_signals:vibration" to "no",
                "traffic_signals:arrow" to "no",
                "check_date:traffic_signals" to "somedate"
            ),
            TrafficSignalsBlindFeaturesAnswer(sound = false, vibration = false, arrow = false),
            StringMapEntryModify("traffic_signals:sound", "no", "no"),
            StringMapEntryModify("traffic_signals:vibration", "no", "no"),
            StringMapEntryModify("traffic_signals:arrow", "no", "no"),
            StringMapEntryModify("check_date:traffic_signals", "somedate", Date().toCheckDateString())
        )
    }

    @Test fun `isApplicableTo returns true for crossings without sound, signal and vibration`() {
        assertTrue(questType.isApplicableTo(create(mapOf(
            "highway" to "crossing",
            "crossing" to "traffic_signals"
        ))))

        assertTrue(questType.isApplicableTo(create(mapOf(
            "highway" to "crossing",
            "crossing" to "traffic_signals",
            "traffic_signals:sound" to "something",
            "traffic_signals:vibration" to "something"
        ))))
    }

    @Test fun `isApplicableTo returns false for crossings with all of sound, arrow and vibration`() {
        assertFalse(questType.isApplicableTo(create(mapOf(
            "highway" to "crossing",
            "crossing" to "traffic_signals",
            "traffic_signals:sound" to "something",
            "traffic_signals:vibration" to "something",
            "traffic_signals:arrow" to "something"
        ))))
    }

    @Test fun `isApplicableTo returns true for old crossings`() {
        assertTrue(questType.isApplicableTo(create(mapOf(
            "highway" to "crossing",
            "crossing" to "traffic_signals",
            "traffic_signals:sound" to "something",
            "traffic_signals:vibration" to "something",
            "traffic_signals:arrow" to "something"
        ), "2001-01-01".toCheckDate())))

        assertTrue(questType.isApplicableTo(create(mapOf(
            "highway" to "crossing",
            "crossing" to "traffic_signals",
            "traffic_signals:sound" to "something",
            "traffic_signals:vibration" to "something",
            "traffic_signals:arrow" to "something",
            "check_date:traffic_signals" to "2001-01-01"
        ))))
    }

    private fun create(tags: Map<String, String>, date: Date? = Date()): Node =
        OsmNode(1, 1, OsmLatLon(0.0, 0.0), tags, null, date)
}