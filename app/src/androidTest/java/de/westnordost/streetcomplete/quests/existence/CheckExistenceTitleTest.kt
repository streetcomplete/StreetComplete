package de.westnordost.streetcomplete.quests.existence

import android.content.res.Configuration
import android.content.res.Resources
import androidx.test.platform.app.InstrumentationRegistry
import de.westnordost.osmfeatures.AndroidFeatureDictionary
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.quests.getQuestTitle
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale
import java.util.concurrent.FutureTask

class CheckExistenceTitleTest {
    private var featureDictionaryFuture: FutureTask<FeatureDictionary>
    private var englishResources: Resources
    private var questType: CheckExistence

    init {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        featureDictionaryFuture = FutureTask { AndroidFeatureDictionary.create(context.assets, "osmfeatures/default", "osmfeatures/brands") }
        featureDictionaryFuture.run()

        val conf = Configuration(context.resources.configuration)
        conf.setLocale(Locale.ENGLISH)
        val localizedContext = context.createConfigurationContext(conf)
        englishResources = localizedContext.resources

        questType = CheckExistence(featureDictionaryFuture)
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2512
    @Test fun issue2512() {
        val newspaperVendingMachineWithName = getQuestTitle(mapOf(
            "amenity" to "vending_machine",
            "vending" to "newspapers",
            "name" to "Bild",
        ))
        assertEquals(newspaperVendingMachineWithName, "Is Bild (Newspaper Vending Machine) still here?")

        val newspaperVendingMachineWithBrand = getQuestTitle(mapOf(
            "amenity" to "vending_machine",
            "vending" to "newspapers",
            "brand" to "Abendzeitung",
        ))
        assertEquals(newspaperVendingMachineWithBrand, "Is Abendzeitung (Newspaper Vending Machine) still here?")
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2640
    @Test fun issue2640() {
        val postBox = getQuestTitle(mapOf(
            "amenity" to "post_box",
            "brand" to "Deutsche Post",
            "operator" to "Deutsche Post AG",
            "ref" to "Hauptsmoorstr. 101, 96052 Bamberg",
        ))
        assertEquals(postBox, "Is Deutsche Post (Mail Drop Box) still here?")
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2806
    @Test fun issue2806() {
        val namedBench = getQuestTitle(mapOf(
            "amenity" to "bench",
            "name" to "Sergey's Seat",
            "ref" to "600913",
            "brand" to "Google",
            "operator" to "Google RESTful",
        ))
        assertEquals(namedBench, "Is Sergey's Seat (Bench) still here?")

        val unnamedBench = getQuestTitle(mapOf(
            "amenity" to "bench",
        ))
        assertEquals(unnamedBench, "Is this still here? (Bench)")
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2840#issuecomment-831245075
    @Test fun issue2840() {
        val schoki = getQuestTitle(mapOf(
            "amenity" to "vending_machine",
            "ref" to "3680",
            "operator" to "Schoko Lädchen",
        ))
        assertEquals(schoki, "Is Schoko Lädchen 3680 (Vending Machine) still here?")
    }

    private fun getQuestTitle(tags: Map<String, String>): String {
        val element = Node(1, LatLon(0.0, 0.0), tags)
        return englishResources.getQuestTitle(questType, element, featureDictionaryFuture)
    }
}
