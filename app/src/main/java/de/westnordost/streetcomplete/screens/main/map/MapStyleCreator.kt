package de.westnordost.streetcomplete.screens.main.map

private const val isPoint = """["==", ["geometry-type"], "Point"]"""
private const val isLines = """["==", ["geometry-type"], "LineString"]"""
private const val isPolygon = """["==", ["geometry-type"], "Polygon"]"""

private fun byZoom(vararg n: Pair<Double, Double>): String = byZoom(n.toList())

private fun byZoom(n: Iterable<Pair<Double, Double>>): String {
    val values = n.flatMap { (z, v) -> listOf(z, v) }.joinToString()
    return """["interpolate", ["exponential", 2], ["zoom"], $values]"""
}

private fun tagIs(key: String, value: Any) = """["==", ["get", "$key"], ${ if (value is String) "\"$value\"" else value }]"""
private fun tagIsNot(key: String, value: Any) = """["!=", ["get", "$key"], ${ if (value is String) "\"$value\"" else value }]"""

private fun tagIn(key: String, vararg values: String) =
    """["in", ["get", "$key"], ["literal", [${ values.joinToString { "\"$it\"" } }]]]"""

private fun tagNotIn(key: String, vararg values: String) =
    """["!", ${tagIn(key, *values)}]"""

private data class Layer(
    val id: String,
    val src: String,
    val filter: List<String> = emptyList(),
    val minZoom: Double? = null,
    val maxZoom: Double? = null,
    val paint: Paint
) {
    fun toJson() = "{ " +
        listOfNotNull(
            "\"id\": \"$id\"",
            "\"source\": \"jawg-streets\"",
            "\"source-layer\": \"$src\"",
            minZoom?.let { "\"minzoom\": $it" },
            maxZoom?.let { "\"maxzoom\": $it" },
            when (filter.size) {
                0 ->    null
                1 ->    "\"filter\": " + filter.single()
                else -> "\"filter\": [\"all\", " + filter.joinToString() + "]"
            },
            paint.toJson(),
        ).joinToString() +
        " }"
}

private interface Paint {
    fun toJson(): String
}

private data class Fill(
    val color: String,
    val opacity: String? = null,
    val antialias: Boolean? = null
): Paint {
    override fun toJson() = listOf(
        "\"type\": \"fill\"",
        "\"paint\": { " +
            listOfNotNull(
                "\"fill-color\": \"$color\"",
                opacity?.let { "\"fill-opacity\": $it" },
                antialias?.let { "\"fill-antialias\": $it" },
            ).joinToString() +
            "}"
    ).joinToString()
}

private data class FillExtrusion(
    val color: String,
    val base: String,
    val height: String,
    val opacity: String? = null,
) : Paint {
    override fun toJson() = listOf(
        "\"type\": \"fill-extrusion\"",
        "\"paint\": { " +
            listOfNotNull(
                "\"fill-extrusion-color\": \"$color\"",
                "\"fill-extrusion-height\": $height",
                "\"fill-extrusion-base\": $base",
                opacity?.let { "\"fill-extrusion-opacity\": $it" },
            ).joinToString() +
            "}"
    ).joinToString()
}

private data class Line(
    val color: String,
    val width: String,
    val gapWidth: String? = null,
    val offset: String? = null,
    val blur: String? = null,
    val opacity: String? = null,
    val miterLimit: Number? = null,
    val dashes: String? = null,
    val cap: String? = null,
    val join: String? = null
): Paint {
    override fun toJson() = listOfNotNull(
        "\"type\": \"line\"",
        "\"paint\": {" +
            listOfNotNull(
                "\"line-color\": \"$color\"",
                "\"line-width\": $width",
                gapWidth?.let  { "\"line-gap-width\": $it" },
                offset?.let  { "\"line-offset\": $it" },
                blur?.let  { "\"line-blur\": $it" },
                dashes?.let { "\"line-dasharray\": $it" },
                opacity?.let   { "\"line-opacity\": $it" },
            ).joinToString() +
            "}",
        if (cap != null || join != null || miterLimit != null) {
            "\"layout\": {" +
                listOfNotNull(
                    cap?.let  { "\"line-cap\": \"$it\"" },
                    join?.let { "\"line-join\": \"$it\"" },
                    miterLimit?.let  { "\"line-miter-limit\": $it" },
                ).joinToString() +
                "}"
        } else null
    ).joinToString(",")
}

private data class Circle(
    val color: String,
    val radius: String,
    val opacity: String? = null,
): Paint {
    override fun toJson() = listOfNotNull(
        "\"type\": \"circle\"",
        "\"paint\": {" +
            listOfNotNull(
                "\"circle-color\": \"$color\"",
                "\"circle-radius\": $radius",
                opacity?.let   { "\"circle-opacity\": $it" },
            ).joinToString() + "}",
    ).joinToString()
}

private data class Symbol(
    val image: String,
    val color: String? = null,
    val padding: Number? = null,
    val placement: String? = null,
    val spacing: String? = null,
    val opacity: String? = null,
    val size: String? = null,
    val rotate: Number? = null,
    val rotationAlignment: String? = null,
) : Paint {
    override fun toJson() = listOfNotNull(
        "\"type\": \"symbol\"",
        "\"paint\": {" +
            listOfNotNull(
                color?.let { "\"icon-color\": \"$it\"" },
                opacity?.let { "\"icon-opacity\": $it" },
            ).joinToString() +
            "}",
        "\"layout\": {" +
            listOfNotNull(
                "\"icon-image\": \"$image\"",
                size?.let { "\"icon-size\": $it" },
                spacing?.let { "\"symbol-spacing\": $it" },
                placement?.let { "\"symbol-placement\": \"$it\"" },
                padding?.let { "\"icon-padding\": $it" },
                rotate?.let { "\"icon-rotate\": $it"  },
                rotationAlignment?.let { "\"icon-rotation-alignment\": \"$it\""  },
            ).joinToString() +
            "}",
    ).joinToString()
}

private data class Text(
    val text: String,
    val size: String,
    val color: String,
    val fonts: List<String>,
    val wrap: Number? = null,
    val padding: Number? = null,
    val outlineColor: String? = null,
    val outlineWidth: Number? = null,
    val placement: String? = null,
    val opacity: String? = null,
    val sortKey: String? = null
) : Paint {
    override fun toJson() = listOfNotNull(
        "\"type\": \"symbol\"",
        "\"paint\": {" +
            listOfNotNull(
                "\"text-color\": \"$color\"",
                outlineColor?.let { "\"text-halo-color\": \"$it\"" },
                outlineWidth?.let { "\"text-halo-width\": $it" },
                opacity?.let { "\"text-opacity\": $it" },
            ).joinToString() +
            "}",
        "\"layout\": {" +
            listOfNotNull(
                "\"text-field\": $text",
                "\"text-size\": $size",
                "\"text-font\": [${fonts.joinToString { "\"$it\""}}]",
                placement?.let { "\"symbol-placement\": \"$it\"" },
                padding?.let { "\"text-padding\": $it" },
                wrap?.let { "\"text-max-width\": $it" },
                sortKey?.let { "\"symbol-sort-key\": $it" },
            ).joinToString() +
            "}",
    ).joinToString()
}

fun createMapStyle(name: String, accessToken: String, languages: List<String>, colors: MapColors, rasterSource: String? = null): String {

    val pathWidth = listOf(14.0 to 0.5, 16.0 to 1.0, 24.0 to 256.0)  // ~1m

    fun coalesceName() =
        "[" +
            listOf(
                "\"coalesce\"",
                *languages.map { "[\"get\", \"name_$it\"]" }.toTypedArray(),
                "[\"get\", \"name\"]"
            ).joinToString() +
            "]"

    val defaultTextStyle = Text(
        text = coalesceName(),
        size = byZoom(1.0 to 13.0, 24.0 to 64.0),
        fonts = listOf("Roboto Regular"),
        color = colors.text,
        outlineColor = colors.textOutline,
        outlineWidth = 2.5,
        padding = 12,
        sortKey = "[\"get\", \"scalerank\"]"
    )

    val waterTextStyle = defaultTextStyle.copy(
        color = colors.textWater,
        outlineColor = colors.textWaterOutline
    )

    val rivers = Waterway("rivers",
        filters = listOf(tagIn("class", "river", "canal")),
        color = colors.water,
        width = listOf(10.0 to 1.0, 16.0 to 3.0, 24.0 to 768.0),
        minZoom = 10.0
    )

    val streams = Waterway("streams",
        filters = listOf(tagIn("class", "stream", "ditch", "drain")),
        color = colors.water,
        width = listOf(16.0 to 1.0, 24.0 to 256.0),
        minZoom = 10.0
    )

    val paths = Road("paths",
        filters = listOf(tagIs("class", "path")),
        color = colors.path,
        colorOutline = colors.path,
        width = pathWidth,
        minZoom = 15.0
    )
    val pedestrian = Road("pedestrian",
        filters = listOf(tagIs("class", "street_limited"), tagIs("type", "pedestrian")),
        color = colors.pedestrian,
        colorOutline = colors.roadOutline,
        width = listOf(13.0 to 1.5, 16.0 to 4.0, 24.0 to 1024.0), // ~4m
        minZoom = 14.0
    )
    val serviceRoads = Road("roads-service",
        filters = listOf(tagIn("class", "service", "driveway")),
        color = colors.road,
        colorOutline = colors.roadOutline,
        width = listOf(13.0 to 0.5, 16.0 to 3.0, 24.0 to 768.0), // ~3m
        minZoom = 14.0
    )
    val minorRoads = Road("roads-minor",
        filters = listOf(tagIn("class", "street", "street_limited"), tagIsNot("type", "pedestrian")),
        color = colors.road,
        colorOutline = colors.roadOutline,
        width = listOf(11.0 to 0.5, 16.0 to 4.0, 24.0 to 1024.0), // ~4m
        minZoom = 12.0
    )
    val majorRoads = Road("roads-major",
        filters = listOf(tagIs("class", "main")),
        color = colors.road,
        colorOutline = colors.roadOutline,
        width = listOf(9.0 to 1.0, 16.0 to 6.0, 24.0 to 1536.0), // ~6m
        minZoom = 5.0,
    )
    val motorways = Road("motorways",
        filters = listOf(tagIs("class", "motorway")),
        color = colors.motorway,
        colorOutline = colors.motorwayOutline,
        width = listOf(8.0 to 1.0, 16.0 to 8.0, 24.0 to 2048.0), // ~8m
        minZoom = 5.0,
    )
    val motorwayLinks = Road("motorway-links",
        filters = listOf(tagIs("class", "motorway_link")),
        color = colors.motorway,
        colorOutline = colors.motorwayOutline,
        width = listOf(11.0 to 1.0, 16.0 to 4.0, 24.0 to 1024.0), // ~4m
    )

    val roads = listOf(pedestrian, serviceRoads, minorRoads, majorRoads, motorways, motorwayLinks)

    fun stepsOverlayLayer(structure: Structure) = Layer(
        id = listOfNotNull("steps", structure.id).joinToString("-"),
        src = "road",
        filter = listOf(tagIn("class", "path"), tagIn("type", "steps"), isLines, structure.filter),
        paint = Line(
            color = colors.pedestrian,
            width = byZoom(pathWidth.map { (z, w) -> z to w * 0.7 }),
            opacity = if (structure == Structure.Tunnel) "0.25" else null,
            dashes = "[0.6, 0.4]"
        )
    )

    fun railwayLayer(structure: Structure) = Layer(
        id = listOfNotNull("railways", structure.id).joinToString("-"),
        src = "road",
        filter = listOf(tagIn("class", "major_rail", "minor_rail"), isLines, structure.filter),
        paint = Line(
            color = colors.railway,
            // at zoom 17, the line spits up into two lines, to mimic the two tracks of a railway
            width = byZoom(12.0 to 0.75, 13.0 to 2.0, 16.999 to 4.0, 17.0 to 2.0, 24.0 to 128.0),
            gapWidth = byZoom(12.0 to 0.0, 17.0 to 0.0, 24.0 to 256.0),
            join = "round",
            opacity = byZoom(12.0 to 0.0, 13.0 to 1.0)
        )
    )

    fun pedestrianAreaLayer(structure: Structure) = Layer(
        id = listOfNotNull("pedestrian-areas", structure.id).joinToString("-"),
        src = "road",
        filter = listOf(tagIn("class", "path", "street_limited"), isPolygon, structure.filter),
        minZoom = 15.0,
        paint = Fill(
            color = colors.pedestrian,
            opacity = byZoom(15.0 to 0.0, 16.0 to 1.0),
        )
    )

    fun pedestrianAreaCasingLayer(structure: Structure) = Layer(
        id = listOfNotNull("pedestrian-areas-casing", structure.id).joinToString("-"),
        src = "road",
        filter = listOf(tagIn("class", "path", "street_limited"), isPolygon, structure.filter),
        minZoom = 16.0,
        paint = Line(
            color = colors.path,
            width = byZoom(16.0 to 1.0, 24.0 to 128.0),
            offset = byZoom(16.0 to -0.5, 24.0 to -64.0),
            opacity = byZoom(16.0 to 0.0, 17.0 to 1.0),
            dashes = if (structure == Structure.Tunnel) "[4, 4]" else null,
        )
    )



    fun allRoadLayers(structure: Structure) = listOfNotNull(
        // for roads, first draw the casing (= outline) of all roads

        *roads.map { it.toCasingLayer(structure) }.toTypedArray(),
        // pedestrian area tunnels are not drawn
        if (structure != Structure.Tunnel) pedestrianAreaCasingLayer(structure) else null,

        // , then draw the road color...

        // roads and pedestrian areas should be drawn on top of paths, as paths on
        // these are kind of "virtual", do only exist for connectivity
        paths.toLayer(structure), // paths do not have a casing
        stepsOverlayLayer(structure),
        if (structure != Structure.Tunnel)  pedestrianAreaLayer(structure) else null,
        *roads.map { it.toLayer(structure) }.toTypedArray(),
        // pedestrian area tunnels are not drawn

        paths.toLayerPrivateOverlay(structure, colors.privateOverlay),
        serviceRoads.toLayerPrivateOverlay(structure, colors.privateOverlay),

        // railway tunnels are not drawn
        // railways are drawn last because e.g. trams should appear on top of roads
        if (structure != Structure.Tunnel) railwayLayer(structure) else null,
    )

    val layers = listOf<Layer>(

        Layer("landuse-town",
            src = "landuse",
            filter = listOf(
                tagNotIn("class", "pitch", "park", "grass", "cemetery", "wood", "scrub", "national_park")
            ),
            minZoom = 11.0,
            paint = Fill(color = colors.town, opacity = byZoom(11.0 to 0.0, 12.0 to 1.0))
        ),
        Layer("landuse-green",
            src = "landuse",
            filter = listOf(tagIn("class", "pitch", "park", "grass", "cemetery")),
            minZoom = 5.0,
            paint = Fill(color = colors.green, opacity = byZoom(5.0 to 0.0, 6.0 to 1.0))
        ),
        Layer("landuse-pitch-outline",
            src = "landuse",
            filter = listOf(tagIs("class", "pitch")),
            minZoom = 16.0,
            paint = Line(
                color = colors.earth,
                width = byZoom(16.0 to 1.0, 24.0 to 128.0),
                offset = byZoom(16.0 to 0.5, 24.0 to 64.0)
            )
        ),
        Layer("landuse-forest",
            src = "landuse",
            filter = listOf(tagIn("class", "wood", "scrub")),
            minZoom = 5.0,
            paint = Fill(color = colors.forest, opacity = byZoom(5.0 to 0.0, 6.0 to 1.0))
        ),

        *(1..2).map { i ->
            Layer("hillshade-highlight-$i",
                src = "hillshade",
                filter = listOf(tagIs("highlight", i)),
                maxZoom = 16.0,
                paint = Fill(
                    color = colors.hillshadeLight,
                    antialias = false,
                    opacity = byZoom(12.0 to 0.12, 16.0 to 0.0)
                )
            )
        }.toTypedArray(),

        *(1..4).map { i ->
            Layer("hillshade-shadow-$i",
                src = "hillshade",
                filter = listOf(tagIs("shadow", i)),
                maxZoom = 16.0,
                paint = Fill(
                    color = colors.hillshadeShadow,
                    antialias = false,
                    opacity = byZoom(12.0 to 0.05, 16.0 to 0.0)
                )
            )
        }.toTypedArray(),

        Layer("water-areas",
            src = "water",
            filter = listOf(Structure.None.filter),
            paint = Fill(colors.water)
        ),
        Layer("water-shore-lines",
            src = "water",
            filter = listOf(Structure.None.filter),
            minZoom = 15.0,
            paint = Line(
                color = colors.waterShore,
                width = byZoom(15.0 to 1.0, 18.0 to 4.0, 24.0 to 256.0),
                offset = byZoom(15.0 to 1.0, 18.0 to 4.0, 24.0 to 256.0),
                opacity = byZoom(15.0 to 0.0, 18.0 to 1.0),
                miterLimit = 6,
            )
        ),
        rivers.toLayer(Structure.None),
        streams.toLayer(Structure.None),

        Layer("aeroways",
            src = "aeroway",
            filter = listOf(isLines),
            paint = Line(
                color = colors.aeroway,
                width = byZoom(10.0 to 1.0, 24.0 to 8192.0),
                join = "round"
            )
        ),

        Layer("buildings",
            src = "building",
            minZoom = 15.0,
            paint = Fill(color = colors.building, opacity = byZoom(15.0 to 0.0, 16.0 to 1.0))
        ),

        Layer("buildings-outline",
            src = "building",
            minZoom = 15.5,
            paint = Line(
                color = colors.buildingOutline,
                width = byZoom(16.0 to 1.0, 24.0 to 128.0),
                opacity = byZoom(15.5 to 0.0, 16.0 to 1.0)
            )
        ),

        *allRoadLayers(Structure.Tunnel).toTypedArray(),

        *allRoadLayers(Structure.None).toTypedArray(),

        Layer("barriers-large",
            src = "structure",
            filter = listOf(tagIn("type", "city_wall", "dam", "cliff")),
            minZoom = 16.0,
            paint = Line(width = byZoom(16.0 to 4.0, 24.0 to 768.0), color = colors.buildingOutline)
        ),
        Layer("barriers-wall",
            src = "structure",
            filter = listOf(tagIs("class", "fence"), tagIsNot("type", "city_wall")),
            minZoom = 16.0,
            paint = Line(width = byZoom(16.0 to 1.0, 24.0 to 256.0), color = colors.buildingOutline)
        ),
        Layer("barriers-hedges",
            src = "structure",
            filter = listOf(tagIs("class", "hedge")),
            minZoom = 16.0,
            paint = Line(width = byZoom(16.0 to 1.0, 24.0 to 512.0), color = colors.forest)
        ),

        Layer("point-barriers",
            src = "structure",
            filter = listOf(isPoint),
            minZoom = 17.0,
            paint = Circle(color = colors.pointBarrier, radius = byZoom(17.0 to 2.0, 24.0 to 256.0))
        ),

        Layer("bridge-areas",
            src = "structure",
            filter = listOf(isPolygon, tagIs("class", "bridge")),
            paint = Fill(color = colors.building, opacity = "0.8")
        ),
        Layer("bridge-lines",
            src = "structure",
            filter = listOf(isLines, tagIs("class", "bridge")),
            paint = Line(color = colors.building, width = byZoom(16.0 to 4.0, 24.0 to 512.0), opacity = "0.8")
        ),

        Layer("water-areas-bridge",
            src = "water",
            filter = listOf(Structure.Bridge.filter),
            paint = Fill(colors.water)
        ),
        rivers.toLayer(Structure.Bridge),
        streams.toLayer(Structure.Bridge),

        *allRoadLayers(Structure.Bridge).toTypedArray(),

        Layer("oneway-arrows",
            src = "road",
            filter = listOf(isLines, tagIs("oneway", true)),
            minZoom = 17.0,
            paint = Symbol(
                image = "oneway-arrow",
                size = byZoom(17.0 to 0.25, 24.0 to 16.0),
                color = colors.onewayArrow,
                padding = 5,
                placement = "line",
                spacing = byZoom(17.0 to 200.0, 24.0 to 25600.0),
                rotate = 90,
                rotationAlignment = "map"
            )
        ),

        Layer("boundaries",
            src = "admin",
            filter = listOf(tagIs("admin_level", 2), tagIsNot("maritime", true)),
            paint = Line(color = colors.adminBoundary, width = "1", dashes = "[1, 2]")
        ),

        Layer("labels-country",
            src = "place_label",
            filter = listOf(tagIs("class", "country")),
            paint = defaultTextStyle.copy(fonts = listOf("Roboto Bold"))
        ),

        Layer("labels-localities",
            src = "place_label",
            filter = listOf(tagIs("class", "locality")),
            paint = defaultTextStyle
        ),

        Layer("labels-housenumbers",
            src = "housenum_label",
            minZoom = 19.0,
            paint = defaultTextStyle.copy(
                text = "[\"get\", \"house_num\"]",
                sortKey = "15",
            )
        ),

        Layer("labels-road",
            src = "road",
            minZoom = 14.0,
            filter = listOf(isLines),
            paint = defaultTextStyle.copy(
                wrap = 25,
                placement = "line-center",
                sortKey = "10",
            )
        ),

        Layer("labels-rivers",
            src = "waterway",
            minZoom = 14.0,
            filter = listOf(
                tagIsNot("structure", "tunnel"),
                tagIn("class", "river", "canal")
            ),
            paint = waterTextStyle.copy(
                placement = "line-center",
                sortKey = "11",
            )
        ),

        Layer("labels-streams",
            src = "waterway",
            minZoom = 16.0,
            filter = listOf(
                tagIsNot("structure", "tunnel"),
                tagIn("class", "stream", "ditch", "drain")
            ),
            paint = waterTextStyle.copy(
                placement = "line-center",
                sortKey = "12"
            )
        ),

        /*
        // I don't know, kind of does not look good. Maybe it would look better if roofs were rendered?

        Layer("buildings-extrude",
            src = "building",
            filter = listOf(tagIs("extrude", true)),
            minZoom = 15.0,
            maxZoom = 19.0,
            paint = FillExtrusion(
                color = colors.building,
                base = """["get", "min_height"]""",
                height = """["get", "height"]""",
                opacity = byZoom(15, 0, 16, 0.8, 18, 0.8, 19, 0),
            )
        ),

         */
    )

    return """${partBeforeLayers(name, accessToken, rasterSource)}
    { "id": "background", "type": "background", "paint": {"background-color": "${colors.earth}"}},
    ${layers.joinToString(",\n    ") { it.toJson() }}
  ]
}
"""
}

private fun partBeforeLayers(name: String, accessToken: String, rasterSource: String?) = """{
  "version": 8,
  "name": "$name",
  "sources": {
    "jawg-streets": {
      "type": "vector",
      "tiles": ["https://tile.jawg.io/streets-v2+hillshade-v1/{z}/{x}/{y}.pbf?access-token=$accessToken"],
      "attribution": "<a href='https://www.openstreetmap.org/copyright' title='OpenStreetMap is open data licensed under ODbL' target='_blank' class='osm-attrib'>&copy; OSM contributors</a> | <a href='https://jawg.io?utm_medium=map&utm_source=attribution' title='Tiles Courtesy of Jawg Maps' target='_blank' class='jawg-attrib'>&copy; <b>Jawg</b>Maps</a>",
      "maxzoom": 16
    ${if (rasterSource == null) "}" else """    },
    "raster-source": {
      "type": "raster",
      "tiles": ["$rasterSource"],
      "maxzoom": 16
    }"""}
  },
  "transition": { "duration": 300, "delay": 0 },
  "light": { "intensity": 0.2 },
  "glyphs": "asset://map_theme/glyphs/{fontstack}/{range}.pbf",
  "sprite": "asset://map_theme/sprites",
  "layers": [${if (rasterSource == null) "" else "\n"+"""{ "id": "raster-layer", "source": "raster-source", "type": "raster" },"""}"""

data class Waterway(
    val id: String,
    val filters: List<String>,
    val color: String,
    val width: List<Pair<Double, Double>>,
    val minZoom: Double? = null,
)

private fun Waterway.toLayer(structure: Structure) = Layer(
    id = listOfNotNull(id, structure.id).joinToString("-"),
    src = "waterway",
    filter = filters + listOf(isLines, structure.filter),
    minZoom = minZoom,
    paint = Line(
        color = color,
        width = byZoom(width.map { (z, w) -> z to w }),
        join = "round",
        cap = "round",
    )
)

private data class Road(
    val id: String,
    val filters: List<String>,
    val color: String,
    val colorOutline: String,
    val width: List<Pair<Double, Double>>,
    val minZoom: Double? = null,
)

private fun Road.toLayer(structure: Structure) = Layer(
    id = listOfNotNull(id, structure.id).joinToString("-"),
    src = "road",
    filter = filters + listOf(isLines, structure.filter),
    paint = Line(
        color = color,
        width = byZoom(width.map { (z, w) -> z to w }),
        join = "round",
        cap = "round",
        opacity = when {
            structure == Structure.Tunnel -> "0.25"
            minZoom != null -> byZoom(minZoom to 0.0, minZoom + 1.0 to 1.0)
            else -> null
        }
    )
)

private fun Road.toCasingLayer(structure: Structure) = Layer(
    id = listOfNotNull(id, structure.id, "casing").joinToString("-"),
    src = "road",
    filter = filters + listOf(isLines, structure.filter),
    minZoom = 15.5,
    paint = Line(
        color = colorOutline,
        width = byZoom(16.0 to 1.0, 24.0 to 128.0),
        join = "round",
        opacity = byZoom(15.0 to 0.0, 16.0 to 1.0),
        // cap must not be round for bridges so that the casing is not drawn on top of normal roads
        cap = if (structure == Structure.None) "round" else "butt",
        dashes = if (structure == Structure.Tunnel) "[4, 4]" else null,
        gapWidth = byZoom(width.map { (z, w) -> z to w })
    )
)

private fun Road.toLayerPrivateOverlay(structure: Structure, privateColor: String) = Layer(
    id = listOfNotNull(id, structure.id, "private").joinToString("-"),
    src = "road",
    filter = filters + listOf(
        isLines,
        tagIn("access", "no", "private", "destination", "customers", "delivery", "agricultural", "forestry", "emergency"),
        structure.filter
    ),
    paint = Line(
        color = privateColor,
        width = byZoom(width.map { (z, w) -> z to w * 0.5 }),
        join = "round",
        cap = "round",
        dashes = "[1, 2]",
    )
)

private enum class Structure { Bridge, Tunnel, None }

private val Structure.filter get() = when (this) {
    Structure.Bridge -> tagIs("structure", "bridge")
    Structure.Tunnel -> tagIs("structure", "tunnel")
    Structure.None -> tagNotIn("structure", "bridge", "tunnel")
}

private val Structure.id get() = when (this) {
    Structure.Bridge -> "bridge"
    Structure.Tunnel -> "tunnel"
    Structure.None -> null
}

data class MapColors(
    val earth: String,
    val water: String,
    val waterShore: String,
    val green: String,
    val forest: String,
    val town: String,
    val building: String,
    val buildingOutline: String,
    val pointBarrier: String,
    val adminBoundary: String,
    val railway: String,
    val aeroway: String,
    val path: String,
    val road: String,
    val roadOutline: String,
    val pedestrian: String,
    val motorway: String,
    val motorwayOutline: String,
    val text: String,
    val textOutline: String,
    val textWater: String,
    val textWaterOutline: String,
    val privateOverlay: String,
    val hillshadeLight: String,
    val hillshadeShadow: String,
    val onewayArrow: String,
)
