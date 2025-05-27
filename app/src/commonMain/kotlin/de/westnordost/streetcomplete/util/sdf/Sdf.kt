package de.westnordost.streetcomplete.util.sdf

import kotlin.math.max
import kotlin.math.round
import kotlin.math.sqrt

// initially copied from https://github.com/mapbox/tiny-sdf/blob/main/index.js and then made to
// work with a simple ARGB IntArray and simplified

private const val INF = 1e20

fun convertToSdf(
    bitmap: IntArray,
    width: Int,
    radius: Double = 8.0,
    cutoff: Double = 0.25
) {
    val height = bitmap.size / width

    val gridOuter = DoubleArray(bitmap.size) { i ->
        when (val a = bitmap[i].ushr(24)) {
            0 -> INF
            255 -> 0.0
            else -> {
                val d = 0.5f - (a.toDouble() / 255.0)
                if (d > 0) d * d else 0.0
            }
        }
    }

    val gridInner = DoubleArray(bitmap.size) { i ->
        when (val a = bitmap[i].ushr(24)) {
            0 -> 0.0
            255 -> INF
            else -> {
                val d = 0.5f - (a.toDouble() / 255.0)
                if (d < 0) d * d else 0.0
            }
        }
    }
    val size = max(width, height)
    val f = DoubleArray(size)
    val z = DoubleArray(size + 1)
    val v = IntArray(size)

    edt(gridOuter, width, height, f, v, z)
    edt(gridInner, width, height, f, v, z)

    for (i in bitmap.indices) {
        val dist = sqrt(gridOuter[i]) - sqrt(gridInner[i])
        bitmap[i] = round(255 - 255 * (dist / radius + cutoff)).toInt().coerceIn(0, 255).shl(24)
    }
}

// 2D Euclidean squared distance transform by Felzenszwalb & Huttenlocher https://cs.brown.edu/~pff/papers/dt-final.pdf
private fun edt(data: DoubleArray, width: Int, height: Int, f: DoubleArray, v: IntArray, z: DoubleArray) {
    for (x in 0..<width)  edt1d(data, x, width, height, f, v, z)
    for (y in 0..<height) edt1d(data, y * width, 1, width, f, v, z)
}

// 1D squared distance transform
private fun edt1d(grid: DoubleArray, offset: Int, stride: Int, length: Int, f: DoubleArray, v: IntArray, z: DoubleArray) {
    v[0] = 0
    z[0] = -INF
    z[1] = INF
    f[0] = grid[offset]

    var k = 0
    for (q in 1..<length) {
        f[q] = grid[offset + q * stride]
        val q2 = q * q
        var s: Double
        do {
            val r = v[k]
            s = (f[q] - f[r] + q2 - r * r) / (q - r) / 2.0
        } while (s <= z[k] && --k > -1)

        k++
        v[k] = q
        z[k] = s
        z[k + 1] = INF
    }

    k = 0
    for (q in 0..<length) {
        while (z[k + 1] < q) k++
        val r = v[k]
        val qr = q - r
        grid[offset + q * stride] = f[r] + qr * qr
    }
}
