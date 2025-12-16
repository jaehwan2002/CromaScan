package com.example.cromascan

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.min

object KMeansColorExtractor {

    fun extractDominantColors(
        bitmap: Bitmap,
        k: Int = 8,
        maxIterations: Int = 10
    ): List<Int> {
        val scaled = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        val width = scaled.width
        val height = scaled.height
        if (width == 0 || height == 0) return emptyList()

        val pixelCount = width * height
        val pixels = IntArray(pixelCount)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        val points = ArrayList<FloatArray>(pixels.size)
        val hsv = FloatArray(3)

        for (color in pixels) {
            Color.colorToHSV(color, hsv)
            val hScaled = (hsv[0] / 360f) * 255f
            val sScaled = hsv[1] * 255f
            val vScaled = hsv[2] * 255f
            points.add(floatArrayOf(hScaled, sScaled, vScaled))
        }

        if (points.isEmpty()) return emptyList()

        val clusterCount = min(k, points.size)
        val centers = MutableList(clusterCount) { i ->
            points[i * points.size / clusterCount].clone()
        }
        val assignments = IntArray(points.size)

        repeat(maxIterations) {
            for (idx in points.indices) {
                val p = points[idx]
                var bestCluster = 0
                var bestDist = Float.MAX_VALUE
                for (c in 0 until clusterCount) {
                    val center = centers[c]
                    val dx = p[0] - center[0]
                    val dy = p[1] - center[1]
                    val dz = p[2] - center[2]
                    val dist = dx * dx + dy * dy + dz * dz
                    if (dist < bestDist) {
                        bestDist = dist
                        bestCluster = c
                    }
                }
                assignments[idx] = bestCluster
            }

            val sum = Array(clusterCount) { floatArrayOf(0f, 0f, 0f) }
            val count = IntArray(clusterCount)

            for (idx in points.indices) {
                val cluster = assignments[idx]
                val p = points[idx]
                sum[cluster][0] += p[0]
                sum[cluster][1] += p[1]
                sum[cluster][2] += p[2]
                count[cluster]++
            }

            for (c in 0 until clusterCount) {
                if (count[c] == 0) continue
                centers[c][0] = sum[c][0] / count[c]
                centers[c][1] = sum[c][1] / count[c]
                centers[c][2] = sum[c][2] / count[c]
            }
        }

        val clusterCounts = IntArray(clusterCount)
        for (cluster in assignments) {
            clusterCounts[cluster]++
        }

        val results = mutableListOf<Pair<Int, Int>>()
        for (c in 0 until clusterCount) {
            val center = centers[c]
            val hScaled = center[0].coerceIn(0f, 255f)
            val sScaled = center[1].coerceIn(0f, 255f)
            val vScaled = center[2].coerceIn(0f, 255f)

            val hsvCenter = floatArrayOf(
                (hScaled / 255f) * 360f,
                sScaled / 255f,
                vScaled / 255f
            )
            val colorInt = Color.HSVToColor(hsvCenter)
            results.add(colorInt to clusterCounts[c])
        }

        return results
            .sortedByDescending { it.second }
            .map { it.first }
    }
}
