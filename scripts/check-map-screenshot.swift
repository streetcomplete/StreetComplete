#!/usr/bin/env swift

import AppKit
import Foundation

guard CommandLine.arguments.count == 2 || CommandLine.arguments.count == 3 else {
    fputs("usage: check-map-screenshot.swift <screenshot.png> [--require-markers]\n", stderr)
    exit(2)
}

let path = CommandLine.arguments[1]
let requireMarkers = CommandLine.arguments.dropFirst(2).contains("--require-markers")
guard let image = NSImage(contentsOfFile: path),
      let representation = NSBitmapImageRep(data: image.tiffRepresentation ?? Data()) else {
    fputs("Could not decode map screenshot: \(path)\n", stderr)
    exit(1)
}

// Ignore the status/diagnostic area and sample the map itself. A missing or blank native surface
// has very few quantized colors; the production vector map consistently has hundreds.
let startY = representation.pixelsHigh / 4
let stepX = max(1, representation.pixelsWide / 120)
let stepY = max(1, (representation.pixelsHigh - startY) / 180)
var colorBins = Set<Int>()
var markerPixels = 0

for y in stride(from: startY, to: representation.pixelsHigh, by: stepY) {
    for x in stride(from: 0, to: representation.pixelsWide, by: stepX) {
        guard let color = representation.colorAt(x: x, y: y)?.usingColorSpace(.deviceRGB) else {
            continue
        }
        let red = min(15, max(0, Int(color.redComponent * 16)))
        let green = min(15, max(0, Int(color.greenComponent * 16)))
        let blue = min(15, max(0, Int(color.blueComponent * 16)))
        colorBins.insert((red << 8) | (green << 4) | blue)
        if color.redComponent > 0.55 && color.blueComponent > 0.5 &&
            color.greenComponent < 0.5 && color.redComponent - color.greenComponent > 0.2 &&
            color.blueComponent - color.greenComponent > 0.15 {
            markerPixels += 1
        }
    }
}

guard colorBins.count >= 48 else {
    fputs("Map screenshot appears blank: only \(colorBins.count) sampled color bins in \(path)\n", stderr)
    exit(1)
}

if requireMarkers && markerPixels < 12 {
    fputs("Selected markers are missing: only \(markerPixels) sampled marker pixels in \(path)\n", stderr)
    exit(1)
}

print(
    "Map screenshot content passed: \(colorBins.count) sampled color bins, " +
        "\(markerPixels) marker pixels in \(path)"
)
