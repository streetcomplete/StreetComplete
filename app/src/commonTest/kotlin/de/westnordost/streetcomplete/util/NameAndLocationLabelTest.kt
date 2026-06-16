package de.westnordost.streetcomplete.util

import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class NameAndLocationLabelTest {
    private val baseDir = "src/commonMain/composeResources/files/"
    private val featureDictionary = FeatureDictionary.create(
        fileSystem = SystemFileSystem,
        presetsBasePath = baseDir + "osmfeatures/default",
        brandPresetsBasePath = baseDir + "osmfeatures/brands",
    )
    /*
    TODO Compose Multiplatform upstream
    Unfortunately, ResourceEnvironment's constructor is internal, so we cannot use this
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
            node(tags = tags),
            featureDictionary
        )?.toString()

    private suspend fun getQuestLabelForWay(tags: Map<String, String>): String? =
        getNameAndLocationLabel(
            resourceEnvironment,
            LayoutDirection.Ltr,
            way(tags = tags),
            featureDictionary
        )?.toString()
*/
}
