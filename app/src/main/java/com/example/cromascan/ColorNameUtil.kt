package com.example.cromascan

import android.graphics.Color
import kotlin.math.abs

object ColorNameUtil {

    private data class NamedColor(val name: String, val color: Int)

    private val colors = listOf(
        NamedColor("빨강", Color.parseColor("#F44336")),
        NamedColor("주황", Color.parseColor("#FF9800")),
        NamedColor("노랑", Color.parseColor("#FFEB3B")),
        NamedColor("연두", Color.parseColor("#8BC34A")),
        NamedColor("초록", Color.parseColor("#4CAF50")),
        NamedColor("청록", Color.parseColor("#009688")),
        NamedColor("하늘", Color.parseColor("#03A9F4")),
        NamedColor("파랑", Color.parseColor("#2196F3")),
        NamedColor("네이비", Color.parseColor("#1A237E")),
        NamedColor("보라", Color.parseColor("#9C27B0")),
        NamedColor("연보라", Color.parseColor("#CE93D8")),
        NamedColor("핑크", Color.parseColor("#E91E63")),
        NamedColor("갈색", Color.parseColor("#795548")),
        NamedColor("회색", Color.parseColor("#9E9E9E")),
        NamedColor("검정", Color.parseColor("#000000")),
        NamedColor("흰색", Color.parseColor("#FFFFFF"))
    )

    fun getColorName(color: Int): String {
        var minDistance = Int.MAX_VALUE
        var closest = "색상"

        for (c in colors) {
            val d =
                abs(Color.red(color) - Color.red(c.color)) +
                        abs(Color.green(color) - Color.green(c.color)) +
                        abs(Color.blue(color) - Color.blue(c.color))

            if (d < minDistance) {
                minDistance = d
                closest = c.name
            }
        }
        return closest
    }
}
