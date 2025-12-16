package com.example.cromascan

import android.graphics.Color

/**
 * 기준 색 하나를 받아서
 * - 보색
 * - 유사색 2개
 * 이런 식으로 3개의 매칭 색을 만들어 주는 간단한 헬퍼.
 */
object ColorMatchCalculator {

    fun calculateMatches(baseColor: Int): IntArray {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)

        val baseHue = hsv[0]
        val s = hsv[1]
        val v = hsv[2]

        // 1) 보색 (Hue + 180)
        val compHue = (baseHue + 180f) % 360f

        // 2) 유사색 (Hue ± 30)
        val analogHue1 = (baseHue + 30f) % 360f
        val analogHue2 = (baseHue + 330f) % 360f   // -30 과 동일

        val compColor = hsvToColor(compHue, s, v)
        val analog1 = hsvToColor(analogHue1, s, v)
        val analog2 = hsvToColor(analogHue2, s, v)

        return intArrayOf(compColor, analog1, analog2)
    }

    private fun hsvToColor(h: Float, s: Float, v: Float): Int {
        val hsv = floatArrayOf(h, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))
        return Color.HSVToColor(hsv)
    }
}
