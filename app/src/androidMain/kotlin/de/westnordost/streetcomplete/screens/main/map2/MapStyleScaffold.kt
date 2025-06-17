package de.westnordost.streetcomplete.screens.main.map2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sargunv.maplibrecompose.compose.MaplibreComposable
import dev.sargunv.maplibrecompose.compose.layer.BackgroundLayer
import dev.sargunv.maplibrecompose.compose.layer.CircleLayer
import dev.sargunv.maplibrecompose.compose.layer.FillExtrusionLayer
import dev.sargunv.maplibrecompose.compose.layer.FillLayer
import dev.sargunv.maplibrecompose.compose.layer.LineLayer
import dev.sargunv.maplibrecompose.compose.layer.SymbolLayer
import dev.sargunv.maplibrecompose.core.source.Source
import dev.sargunv.maplibrecompose.expressions.ast.Expression
import dev.sargunv.maplibrecompose.expressions.dsl.Feature
import dev.sargunv.maplibrecompose.expressions.dsl.all
import dev.sargunv.maplibrecompose.expressions.dsl.asNumber
import dev.sargunv.maplibrecompose.expressions.dsl.asString
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.feature
import dev.sargunv.maplibrecompose.expressions.dsl.image
import dev.sargunv.maplibrecompose.expressions.dsl.nil
import dev.sargunv.maplibrecompose.expressions.dsl.not
import dev.sargunv.maplibrecompose.expressions.value.BooleanValue
import dev.sargunv.maplibrecompose.expressions.value.IconRotationAlignment
import dev.sargunv.maplibrecompose.expressions.value.LineCap
import dev.sargunv.maplibrecompose.expressions.value.LineJoin
import dev.sargunv.maplibrecompose.expressions.value.SymbolPlacement
import kotlin.math.max

/**
 * StreetComplete background map style using the tile schema from JawgMaps as defined in
 * https://www.jawg.io/docs/apidocs/maps/streets-v2/
 *
 * It allows to inject more content at the following points:
 * - [belowRoadsContent]: directly below roads and above landscape, buildings
 * - [belowRoadsOnBridgeContent]: same as above but below road bridges
 * - [belowLabelsContent]: above everything but labels
 * - [aboveLabelsContent]: above everything
 * */
@Composable @MaplibreComposable
fun MapStyleScaffold(
    source: Source,
    colors: MapColors,
    languages: List<String>,
    belowRoadsContent: @Composable @MaplibreComposable () -> Unit = {},
    belowRoadsOnBridgeContent: @Composable @MaplibreComposable () -> Unit = {},
    belowLabelsContent: @Composable @MaplibreComposable () -> Unit = {},
    aboveLabelsContent: @Composable @MaplibreComposable () -> Unit = {},
) {

    val paths = remember(colors) {
        RoadType(
            id = "paths",
            minZoom = 15f,
            filters = feature.inClass("path"),
            color = colors.path,
            colorOutline = colors.path,
            widthStops = listOf(14.0 to 0.5.dp, 16.0 to 1.0.dp, 24.0 to 256.0.dp)  // ~1m
        )
    }
    val pedestrian = remember(colors) {
        RoadType(
            id = "pedestrian",
            minZoom = 14f,
            filters = all(feature.inClass("street_limited"), feature.inType("pedestrian")),
            color = colors.pedestrian,
            colorOutline = colors.roadOutline,
            widthStops = listOf(13 to 1.5.dp, 16 to 4.dp, 24 to 1024.dp), // ~4m
        )
    }
    val serviceRoads = remember(colors) {
        RoadType(
            id = "roads-service",
            minZoom = 14f,
            filters = feature.inClass("service", "driveway"),
            color = colors.road,
            colorOutline = colors.roadOutline,
            widthStops = listOf(13 to 0.5.dp, 16 to 3.dp, 24 to 768.dp), // ~3m
        )
    }
    val minorRoads = remember(colors) {
        RoadType(
            id = "roads-minor",
            minZoom = 12f,
            filters = all(feature.inClass("street", "street_limited"), !feature.inType("pedestrian")),
            color = colors.road,
            colorOutline = colors.roadOutline,
            widthStops = listOf(13 to 1.5.dp, 16 to 4.dp, 24 to 1024.dp), // ~4m
        )
    }
    val majorRoads = remember(colors) {
        RoadType(
            id = "roads-major",
            minZoom = 5f,
            filters = feature.inClass("main"),
            color = colors.road,
            colorOutline = colors.roadOutline,
            widthStops = listOf(9 to 1.dp, 16 to 6.dp, 24 to 1536.dp), // ~6m
        )
    }
    val motorways = remember(colors) {
        RoadType(
            id = "motorways",
            minZoom = 5f,
            filters = feature.inClass("motorway"),
            color = colors.motorway,
            colorOutline = colors.motorwayOutline,
            widthStops = listOf(8 to 1.dp, 16 to 8.dp, 24 to 2048.dp), // ~8m
        )
    }
    val motorwayLinks = remember(colors) {
        RoadType(
            id = "motorway-links",
            minZoom = 5f,
            filters = feature.inClass("motorway_link"),
            color = colors.motorway,
            colorOutline = colors.motorwayOutline,
            widthStops = listOf(11 to 1.dp, 16 to 4.dp, 24 to 1024.dp), // ~4m
        )
    }
    val roads = listOf(pedestrian, serviceRoads, minorRoads, majorRoads, motorways, motorwayLinks)

    LandLayers(source, colors)
    HillshadeLayers(source, colors)
    WaterLayers(source, colors, Structure.None)
    AerowaysLayer(source, colors)
    BuildingLayers(source, colors)

    RoadLayers(source, colors, roads, paths, serviceRoads, Structure.Tunnel)

    PedestrianAreaLayers(source, colors, Structure.None)

    belowRoadsContent()

    RoadLayers(source, colors, roads, paths, serviceRoads, Structure.None)
    RailwayLayer(source, colors, Structure.None)

    BarriersLayers(source, colors)

    BridgeAreasLayers(source, colors)

    WaterLayers(source, colors, Structure.Bridge)
    PedestrianAreaLayers(source, colors, Structure.Bridge)

    belowRoadsOnBridgeContent()

    RoadLayers(source, colors, roads, paths, serviceRoads, Structure.Bridge)
    RailwayLayer(source, colors, Structure.Bridge)

    OnewayArrowsLayer(source, colors)
    BoundaryLayer(source, colors)

    belowLabelsContent()

    LabelLayers(source, colors, languages)

    aboveLabelsContent()

    // I don't know, kind of does not look good due to missing extrusion outline.
    //BuildingExtrudeLayer(source, colors)
}

@Composable @MaplibreComposable
private fun LandLayers(source: Source, colors: MapColors) {
    BackgroundLayer(
        id = "background",
        color = const(colors.earth)
    )
    FillLayer(
        id = "landuse-town",
        source = source,
        sourceLayer = "landuse",
        minZoom = 11f,
        filter = !feature.inClass("pitch", "park", "grass", "cemetery", "wood", "scrub", "national_park"),
        opacity = fadeInAtZoom(11f),
        color = const(colors.town),
    )
    FillLayer(
        id = "landuse-green",
        source = source,
        sourceLayer = "landuse",
        minZoom = 5f,
        filter = feature.inClass("pitch", "park", "grass", "cemetery"),
        opacity = fadeInAtZoom(5f),
        color = const(colors.green),
    )
    FillLayer(
        id = "landuse-forest",
        source = source,
        sourceLayer = "landuse",
        minZoom = 5f,
        filter = feature.inClass("class", "wood", "scrub"),
        opacity = fadeInAtZoom(5f),
        color = const(colors.forest),
    )
    LineLayer(
        id = "landuse-pitch-outline",
        source = source,
        sourceLayer = "landuse",
        minZoom = 16f,
        filter = feature.inClass("pitch"),
        color = const(colors.earth),
        width = byZoom(16 to 1.dp, 24 to 128.dp),
        offset = byZoom(16 to 0.5.dp, 24 to 64.dp)
    )
}

@Composable @MaplibreComposable
private fun HillshadeLayers(source: Source, colors: MapColors) {
    for (i in 1..2) {
        FillLayer(
            id = "hillshade-highlight-$i",
            source = source,
            sourceLayer = "hillshade",
            maxZoom = 16f,
            filter = feature.has("highlight", i),
            opacity = fadeOutAtZoom(12f, range = 4f, startOpacity = 0.12f),
            color = const(colors.hillshadeLight),
            antialias = const(false),
        )
    }
    for (i in 1..4) {
        FillLayer(
            id = "hillshade-shadow-$i",
            source = source,
            sourceLayer = "hillshade",
            maxZoom = 16f,
            filter = feature.has("shadow", i),
            opacity = fadeOutAtZoom(12f, range = 4f, startOpacity = 0.05f),
            color = const(colors.hillshadeShadow),
            antialias = const(false),
        )
    }
}

@Composable @MaplibreComposable
private fun WaterLayers(source: Source, colors: MapColors, structure: Structure) {
    FillLayer(
        id = listOfNotNull("water-areas", structure.id).joinToString("-"),
        source = source,
        sourceLayer = "water",
        filter = feature.isStructure(structure),
        color = const(colors.water)
    )
    // drawing these lines on bridges looks weird
    if (structure == Structure.None) {
        LineLayer(
            id = listOfNotNull("water-shore-lines", structure.id).joinToString("-"),
            source = source,
            sourceLayer = "water",
            minZoom = 15f,
            filter = feature.isStructure(structure),
            opacity = fadeInAtZoom(15f, range = 3f),
            color = const(colors.waterShore),
            width = byZoom(15 to 1.dp, 18 to 4.dp, 24 to 256.dp),
            offset = byZoom(15 to 1.dp, 18 to 4.dp, 24 to 256.dp),
            miterLimit = const(6)
        )
    }
    LineLayer(
        id = listOfNotNull("rivers", structure.id).joinToString("-"),
        source = source,
        sourceLayer = "waterway",
        minZoom = 10f,
        filter = all(
            feature.inClass("river", "canal"),
            feature.isStructure(structure),
            feature.isLines()
        ),
        color = const(colors.water),
        width = byZoom(10 to 1.dp, 16 to 3.dp, 24 to 768.dp),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round)
    )
    LineLayer(
        id = listOfNotNull("streams", structure.id).joinToString("-"),
        source = source,
        sourceLayer = "waterway",
        minZoom = 10f,
        filter = all(
            feature.inClass("class", "stream", "ditch", "drain"),
            feature.isStructure(structure),
            feature.isLines()
        ),
        color = const(colors.water),
        width = byZoom(16 to 1.dp, 24 to 256.dp),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round)
    )
}

@Composable @MaplibreComposable
private fun AerowaysLayer(source: Source, colors: MapColors) {
    LineLayer(
        id = "aeroways",
        source = source,
        sourceLayer = "aeroway",
        filter = feature.isLines(),
        color = const(colors.aeroway),
        width = byZoom(10 to 1.dp, 24 to 8192.dp),
        join = const(LineJoin.Round)
    )
}

@Composable @MaplibreComposable
private fun BuildingLayers(source: Source, colors: MapColors) {
    FillLayer(
        id = "buildings",
        source = source,
        sourceLayer = "building",
        minZoom = 15f,
        opacity = fadeInAtZoom(15f),
        color = const(colors.building)
    )
    LineLayer(
        id = "buildings-outline",
        source = source,
        sourceLayer = "building",
        minZoom = 15f,
        opacity = fadeInAtZoom(15.5f, range = 0.5f),
        color = const(colors.buildingOutline),
        width = byZoom(16 to 1.dp, 24 to 128.dp)
    )
}

@Composable @MaplibreComposable
private fun PedestrianAreaLayers(source: Source, colors: MapColors, structure: Structure) {
    val filter = all(
        feature.inClass("path", "street_limited"),
        feature.isArea(),
        feature.isStructure(structure),
    )

    LineLayer(
        id = listOfNotNull("pedestrian-areas-casing", structure.id).joinToString("-"),
        source = source,
        sourceLayer = "road",
        minZoom = 16f,
        filter = filter,
        opacity = fadeInAtZoom(16f),
        color = const(colors.roadOutline),
        width = byZoom(16 to 1.dp, 24 to 128.dp),
        offset = byZoom(16 to 0.dp, 17 to 1.dp),
    )
    FillLayer(
        id = listOfNotNull("pedestrian-areas", structure.id).joinToString("-"),
        source = source,
        sourceLayer = "road",
        minZoom = 15f,
        filter = filter,
        opacity = fadeInAtZoom(15f),
        color = const(colors.pedestrian)
    )
}

@Composable @MaplibreComposable
private fun RoadLayers(
    source: Source,
    colors: MapColors,
    roads: List<RoadType>,
    paths: RoadType,
    serviceRoads: RoadType,
    structure: Structure
) {
    // for roads, first draw the casing (= outline) of all roads
    for (road in roads) {
        RoadCasingLayer(road, source, structure)
    }
    // , then draw the road color...
    RoadLayer(paths, source, structure)
    StepsOverlayLayer(source, colors, structure)
    for (road in roads) {
        RoadLayer(road, source, structure)
    }
    RoadPrivateOverlayLayer(paths, source, colors, structure)
    RoadPrivateOverlayLayer(serviceRoads, source, colors, structure)
}

@Composable @MaplibreComposable
private fun BarriersLayers(source: Source, colors: MapColors) {
    LineLayer(
        id = "barriers-large",
        source = source,
        sourceLayer = "structure",
        minZoom = 16f,
        filter = feature.inType("city_wall", "dam", "cliff"),
        color = const(colors.buildingOutline),
        width = byZoom(16 to 4.dp, 24 to 768.dp),
    )
    LineLayer(
        id = "barriers-wall",
        source = source,
        sourceLayer = "structure",
        minZoom = 16f,
        filter = all(
            feature.inClass("fence"),
            !feature.inType("city_wall"),
        ),
        color = const(colors.buildingOutline),
        width = byZoom(16 to 1.dp, 24 to 256.dp),
    )
    LineLayer(
        id = "barriers-hedges",
        source = source,
        sourceLayer = "structure",
        minZoom = 16f,
        filter = feature.inClass("hedge"),
        color = const(colors.forest),
        width = byZoom(16 to 1.dp, 24 to 256.dp),
    )
    CircleLayer(
        id = "point-barriers",
        source = source,
        sourceLayer = "structure",
        minZoom = 17f,
        filter = feature.isPoint(),
        color = const(colors.pointBarrier),
        radius = byZoom(17 to 2.dp, 24 to 256.dp),
    )
}

@Composable @MaplibreComposable
private fun BridgeAreasLayers(source: Source, colors: MapColors) {
    FillLayer(
        id = "bridge-areas",
        source = source,
        sourceLayer = "structure",
        filter = all(feature.isArea(), feature.inClass("bridge")),
        opacity = const(0.8f),
        color = const(colors.building)
    )
    LineLayer(
        id = "bridge-lines",
        source = source,
        sourceLayer = "structure",
        filter = all(feature.isLines(), feature.inClass("bridge")),
        opacity = const(0.8f),
        color = const(colors.building),
        width = byZoom(16 to 4.dp, 24 to 512.dp),
    )
}

@Composable @MaplibreComposable
private fun OnewayArrowsLayer(source: Source, colors: MapColors) {
    SymbolLayer(
        id = "oneway-arrows",
        source = source,
        sourceLayer = "road",
        minZoom = 17f,
        filter = all(feature.isLines(), feature.has("oneway", true)),
        placement = const(SymbolPlacement.LineCenter),
        spacing = byZoom(17 to 200.dp, 24 to 25600.dp),
        iconImage = image("oneway-arrow"),
        iconColor = const(colors.onewayArrow),
        iconSize = byZoom(17 to 0.25f, 24 to 16.0f),
        iconPadding = const(5.dp),
        iconRotate = const(90),
        iconRotationAlignment = const(IconRotationAlignment.Map)
    )
}

@Composable @MaplibreComposable
private fun BoundaryLayer(source: Source, colors: MapColors) {
    LineLayer(
        id = "boundaries",
        source = source,
        sourceLayer = "admin",
        filter = all(
            !feature.has("maritime", true),
            feature.has("admin_level", 2),
        ),
        color = const(colors.adminBoundary),
        width = const(1.dp),
        dasharray = const(listOf(1, 2))
    )
}

@Composable @MaplibreComposable
private fun LabelLayers(source: Source, colors: MapColors, languages: List<String>) {
    val lineCenter = const(SymbolPlacement.LineCenter)
    val localizedName = feature.localizedName(languages)

    TextLayer(
        id = "labels-country",
        source = source,
        sourceLayer = "place_label",
        filter = feature.inClass("country"),
        sortKey = feature.get("scalerank").asNumber(),
        text = localizedName,
        color = const(colors.text),
        haloColor = const(colors.textOutline),
    )
    TextLayer(
        id = "labels-localities",
        source = source,
        sourceLayer = "place_label",
        filter = feature.inClass("locality"),
        sortKey = feature.get("scalerank").asNumber(),
        text = localizedName,
        color = const(colors.text),
        haloColor = const(colors.textOutline),
    )
    TextLayer(
        id = "labels-housenumbers",
        source = source,
        sourceLayer = "housenum_label",
        minZoom = 18f,
        text = feature.get("house_num").asString(),
        color = const(colors.text),
        haloColor = const(colors.textOutline),
    )
    TextLayer(
        id = "labels-road",
        source = source,
        sourceLayer = "road",
        minZoom = 14f,
        filter = feature.isLines(),
        placement = lineCenter,
        text = localizedName,
        color = const(colors.text),
        haloColor = const(colors.textOutline),
    )
    TextLayer(
        id = "labels-rivers",
        source = source,
        sourceLayer = "waterway",
        minZoom = 14f,
        filter = all(!feature.isTunnel(), feature.inClass("stream", "river", "canal")),
        placement = lineCenter,
        text = localizedName,
        color = const(colors.textWater),
        haloColor = const(colors.textWaterOutline),
    )
    TextLayer(
        id = "labels-streams",
        source = source,
        sourceLayer = "waterway",
        minZoom = 16f,
        filter = all(!feature.isTunnel(), feature.inClass("stream", "ditch", "drain")),
        placement = lineCenter,
        text = localizedName,
        color = const(colors.textWater),
        haloColor = const(colors.textWaterOutline),
    )
}

@Composable @MaplibreComposable
private fun BuildingExtrudeLayer(source: Source, colors: MapColors) {
    FillExtrusionLayer(
        id = "buildings-extrude",
        source = source,
        sourceLayer = "building",
        minZoom = 15f,
        maxZoom = 19f,
        filter = feature.has("extrude", true),
        opacity = byZoom(15 to 0f, 16 to 0.8f, 18 to 0.8f, 19 to 0f),
        color = const(colors.building),
        base = feature.get("min_height").asNumber(),
        height = feature.get("height").asNumber()
    )
}

@Composable @MaplibreComposable
private fun RoadLayer(road: RoadType, source: Source, structure: Structure) {
    LineLayer(
        id = listOfNotNull(road.id, structure.id).joinToString("-"),
        source = source,
        sourceLayer = "road",
        minZoom = road.minZoom,
        filter = all(
            feature.isLines(),
            feature.isStructure(structure),
            road.filters,
        ),
        opacity = when {
            structure == Structure.Tunnel -> const(0.25f)
            road.minZoom != 0f -> fadeInAtZoom(road.minZoom)
            else -> const(1f)
        },
        color = const(road.color),
        width = byZoom(*road.widthStops.toTypedArray()),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round),
    )
}

@Composable @MaplibreComposable
private fun RoadCasingLayer(road: RoadType, source: Source, structure: Structure) {
    LineLayer(
        id = listOfNotNull(road.id, structure.id, "casing").joinToString("-"),
        source = source,
        sourceLayer = "road",
        minZoom = max(15.5f, road.minZoom),
        filter = all(
            feature.isLines(),
            feature.isStructure(structure),
            road.filters
        ),
        opacity = fadeInAtZoom(15f),
        color = const(road.colorOutline),
        dasharray = if (structure == Structure.Tunnel) const(listOf(4, 4)) else nil(),
        width = byZoom(16 to 1.dp, 24 to 128.dp),
        gapWidth = byZoom(*road.widthStops.toTypedArray()),
        // cap must not be round for bridges so that the casing is not drawn on top of normal roads
        cap = if (structure == Structure.None) const(LineCap.Round) else const(LineCap.Butt),
        join = const(LineJoin.Round)
    )
}

@Composable @MaplibreComposable
private fun RoadPrivateOverlayLayer(road: RoadType, source: Source, colors: MapColors, structure: Structure) {
    LineLayer(
        id = listOfNotNull(road.id, structure.id, "private").joinToString("-"),
        source = source,
        sourceLayer = "road",
        minZoom = road.minZoom,
        filter = all(
            feature.isLines(),
            feature.hasAny("access", listOf("no", "private", "destination", "customers", "delivery", "agricultural", "forestry", "emergency")),
            feature.isStructure(structure),
            road.filters
        ),
        color = const(colors.privateOverlay),
        dasharray = const(listOf(1, 2)),
        width = byZoom(*road.widthStops.map { (z, w) -> z to (w * 0.5f) }.toTypedArray()),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round),
    )
}

@Composable @MaplibreComposable
private fun RailwayLayer(source: Source, colors: MapColors, structure: Structure) {
    LineLayer(
        id = listOfNotNull("railways", structure.id).joinToString("-"),
        source = source,
        sourceLayer = "road",
        minZoom = 12f,
        filter = all(
            feature.inClass("major_rail", "minor_rail"),
            feature.isLines(),
            feature.isStructure(structure)
        ),
        opacity = fadeInAtZoom(12f),
        color = const(colors.railway),
        // at zoom 17, the line spits up into two lines, to mimic the two tracks of a railway
        width = byZoom(12 to 0.75.dp, 13 to 2.dp, 16.999 to 4.dp, 17.0 to 2.dp, 24.0 to 128.dp),
        gapWidth = byZoom(12 to 0.dp, 17 to 0.dp, 24 to 256.dp),
        join = const(LineJoin.Round)
    )
}

@Composable @MaplibreComposable
private fun StepsOverlayLayer(source: Source, colors: MapColors, structure: Structure) {
    LineLayer(
        id = listOfNotNull("steps", structure.id).joinToString("-"),
        source = source,
        sourceLayer = "road",
        minZoom = 15f,
        filter = all(
            feature.inClass("path"),
            feature.inType("steps"),
            feature.isLines(),
            feature.isStructure(structure)
        ),
        opacity = if (structure == Structure.Tunnel) const(0.25f) else const(1f),
        color = const(colors.pedestrian),
        dasharray = const(listOf(0.6f, 0.4f)),
        // 75% of path width
        width = byZoom(14 to 0.375.dp, 16 to 0.75.dp, 24 to 192.dp)
    )
}

@Immutable
private data class RoadType(
    val id: String,
    val minZoom: Float = 0.0f,
    val filters: Expression<BooleanValue>,
    val color: Color,
    val colorOutline: Color,
    val widthStops: List<Pair<Number, Dp>>,
)

private enum class Structure(val id: String?) { Bridge("bridge"), Tunnel("tunnel"), None(null) }

private fun Feature.isStructure(structure: Structure) = when (structure) {
    Structure.Bridge -> isBridge()
    Structure.Tunnel -> isTunnel()
    Structure.None -> isOnGround()
}
private fun Feature.isBridge() = feature.has("structure", "bridge")
private fun Feature.isTunnel() = feature.has("structure", "tunnel")
private fun Feature.isOnGround() = !feature.hasAny("structure", listOf("bridge", "tunnel"))

private fun Feature.inClass(vararg values: String) = hasAny("class", values.toList())
private fun Feature.inType(vararg values: String) = hasAny("type", values.toList())

private fun Feature.localizedName(languages: List<String>) = localizedName(
    languages = languages,
    nameKey = "name",
    localizedNameKey = { "name_$it" },
    extraLocalizedNameKeys = listOf("name_ltn")
)
