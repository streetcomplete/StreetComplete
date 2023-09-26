package de.westnordost.streetcomplete.util

import android.content.res.Configuration
import android.content.res.Resources
import androidx.test.platform.app.InstrumentationRegistry
import de.westnordost.osmfeatures.AndroidFeatureDictionary
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class NameAndLocationLabelTest {
    private var featureDictionary: FeatureDictionary
    private var englishResources: Resources

    init {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        featureDictionary = AndroidFeatureDictionary.create(context.assets, "osmfeatures/default", "osmfeatures/brands")

        val conf = Configuration(context.resources.configuration)
        conf.setLocale(Locale.ENGLISH)
        val localizedContext = context.createConfigurationContext(conf)
        englishResources = localizedContext.resources
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2512
    @Test fun newspaperVendingMachineWithName() {
        assertEquals("Bild (Newspaper Vending Machine)", getQuestLabel(mapOf(
            "amenity" to "vending_machine",
            "vending" to "newspapers",
            "name" to "Bild",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2512
    @Test fun newspaperVendingMachineWithBrand() {
        assertEquals("Abendzeitung (Newspaper Vending Machine)", getQuestLabel(mapOf(
            "amenity" to "vending_machine",
            "vending" to "newspapers",
            "brand" to "Abendzeitung",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2640
    @Test fun postBox() {
        assertEquals("Deutsche Post (Mail Drop Box)", getQuestLabel(mapOf(
            "amenity" to "post_box",
            "brand" to "Deutsche Post",
            "operator" to "Deutsche Post AG",
            "ref" to "Hauptsmoorstr. 101, 96052 Bamberg",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2806
    @Test fun namedBench() {
        assertEquals("Sergey's Seat (Bench)", getQuestLabel(mapOf(
            "amenity" to "bench",
            "name" to "Sergey's Seat",
            "ref" to "600913",
            "brand" to "Google",
            "operator" to "Google RESTful",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2806
    @Test fun unnamedBench() {
        assertEquals("Bench", getQuestLabel(mapOf(
            "amenity" to "bench",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2840#issuecomment-831245075
    @Test fun schoki() {
        assertEquals("Schoko Lädchen [3680] (Vending Machine)", getQuestLabel(mapOf(
            "amenity" to "vending_machine",
            "ref" to "3680",
            "operator" to "Schoko Lädchen",
        )))
    }

    private fun getQuestLabel(tags: Map<String, String>): String? =
        getNameAndLocationLabel(
            Node(0, LatLon(0.0, 0.0), tags),
            englishResources,
            featureDictionary
        )?.toString()
}
