package de.westnordost.streetcomplete.util

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.platform.app.InstrumentationRegistry
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.create
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.DensityQualifier
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.ThemeQualifier
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class NameAndLocationLabelTest {
    private val featureDictionary = FeatureDictionary.create(
        assetManager = InstrumentationRegistry.getInstrumentation().targetContext.assets,
        presetsBasePath = "osmfeatures/default",
        brandPresetsBasePath = "osmfeatures/brands"
    )
    /*
    TODO
    Unfortunately, ResourceEnviornment's constructor is internal, so we cannot use this
    see https://youtrack.jetbrains.com/issue/CMP-9959/Access-resources-in-specific-language-outside-of-composition
    Without using specifically English resources, most tests will fail

    @OptIn(InternalResourceApi::class)
    private val resourceEnvironment = ResourceEnvironment(
        language = LanguageQualifier("en"),
        region = RegionQualifier(""),
        theme = ThemeQualifier.LIGHT,
        density = DensityQualifier.MDPI,
    )

    // https://github.com/streetcomplete/StreetComplete/issues/2512
    @Test fun newspaperVendingMachineWithName() = runBlocking {
        assertEquals(
            "Bild (Newspaper\u00A0Vending\u00A0Machine)",
            getQuestLabelForNode(mapOf(
                "amenity" to "vending_machine",
                "vending" to "newspapers",
                "name" to "Bild",
            ))
        )
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2512
    @Test fun newspaperVendingMachineWithBrand() = runBlocking {
        assertEquals(
            "Abendzeitung (Newspaper\u00A0Vending\u00A0Machine)",
            getQuestLabelForNode(mapOf(
                "amenity" to "vending_machine",
                "vending" to "newspapers",
                "brand" to "Abendzeitung",
            ))
        )
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2640
    @Test fun postBox() = runBlocking {
        assertEquals(
            "Deutsche\u00A0Post (Mail\u00A0Drop\u00A0Box)",
            getQuestLabelForNode(mapOf(
                "amenity" to "post_box",
                "brand" to "Deutsche Post",
                "operator" to "Deutsche Post AG",
                "ref" to "Hauptsmoorstr. 101, 96052 Bamberg",
            ))
        )
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2806
    @Test fun namedBench() = runBlocking {
        assertEquals(
            "Sergey's\u00A0Seat (Bench)",
            getQuestLabelForNode(mapOf(
                "amenity" to "bench",
                "name" to "Sergey's Seat",
                "ref" to "600913",
                "brand" to "Google",
                "operator" to "Google RESTful",
            ))
        )
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2806
    @Test fun unnamedBench() = runBlocking {
        assertEquals(
            "Bench",
            getQuestLabelForNode(mapOf("amenity" to "bench"))
        )
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2840#issuecomment-831245075
    @Test fun schoki() = runBlocking {
        assertEquals(
            "Schoko\u00A0Lädchen\u00A0[3680] (Vending\u00A0Machine)",
            getQuestLabelForNode(mapOf(
                "amenity" to "vending_machine",
                "ref" to "3680",
                "operator" to "Schoko Lädchen",
            ))
        )
    }

    // https://github.com/streetcomplete/StreetComplete/issues/5549
    @Test fun pointNotVertex() = runBlocking {
        assertEquals(
            "Bollard",
            getQuestLabelForNode(mapOf(
                "barrier" to "bollard",
            ))
        )
    }

    // https://github.com/streetcomplete/StreetComplete/issues/5427
    @Test fun roadWithName() = runBlocking {
        assertEquals(
            "Main\u00A0Street (Residential\u00A0Road)",
            getQuestLabelForWay(mapOf(
                "highway" to "residential",
                "name" to "Main Street",
                "operator" to "Road Agency",
            ))
        )
    }

    @Test fun roadWitRef() = runBlocking {
        assertEquals(
            "A1 (Residential\u00A0Road)",
            getQuestLabelForWay(mapOf(
                "highway" to "residential",
                "ref" to "A1",
                "operator" to "Road Agency",
            ))
        )
    }

    @Test fun roadWithNameAndRef() = runBlocking {
        assertEquals(
            "Main\u00A0Street\u00A0[A1] (Residential\u00A0Road)",
            getQuestLabelForWay(mapOf(
                "highway" to "residential",
                "name" to "Main Street",
                "ref" to "A1",
                "operator" to "Road Agency",
            ))
        )
    }

    private suspend fun getQuestLabelForNode(tags: Map<String, String>): String? =
        getNameAndLocationLabel(
            resourceEnvironment,
            LayoutDirection.Ltr,
            Node(0, LatLon(0.0, 0.0), tags),
            featureDictionary
        )?.toString()

    private suspend fun getQuestLabelForWay(tags: Map<String, String>): String? =
        getNameAndLocationLabel(
            resourceEnvironment,
            LayoutDirection.Ltr,
            Way(0, listOf(), tags),
            featureDictionary
        )?.toString()
    */
}
