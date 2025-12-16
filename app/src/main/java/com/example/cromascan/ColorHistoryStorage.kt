package com.example.cromascan

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// 히스토리에 저장되는 한 건의 데이터
data class ColorHistoryItem(
    val baseHex: String,     // 기준 색상 HEX (#RRGGBB)
    val matchColor1: Int,    // 추천 색 1
    val matchColor2: Int,    // 추천 색 2
    val matchColor3: Int,    // 추천 색 3
    val timestamp: Long      // 저장 시간 (ms)
)

object ColorHistoryStorage {

    private const val PREF_NAME = "color_history_pref"
    private const val KEY_HISTORY = "color_history_list"

    // 히스토리 전체 불러오기
    fun loadHistory(context: Context): MutableList<ColorHistoryItem> {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = sp.getString(KEY_HISTORY, null) ?: return mutableListOf()

        val list = mutableListOf<ColorHistoryItem>()
        val arr = JSONArray(json)

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val item = ColorHistoryItem(
                baseHex = obj.getString("baseHex"),
                matchColor1 = obj.getInt("matchColor1"),
                matchColor2 = obj.getInt("matchColor2"),
                matchColor3 = obj.getInt("matchColor3"),
                timestamp = obj.getLong("timestamp")
            )
            list.add(item)
        }

        return list
    }

    // 한 건 저장 (기존 리스트 맨 앞에 추가)
    fun saveColorHistory(context: Context, item: ColorHistoryItem) {
        val list = loadHistory(context)

        // 새 기록을 맨 앞에 넣기
        list.add(0, item)

        // 너무 많이 쌓이면 앞의 50개만 유지
        if (list.size > 50) {
            list.subList(50, list.size).clear()
        }

        // JSON 배열로 다시 저장
        val arr = JSONArray()
        for (entry in list) {
            val obj = JSONObject().apply {
                put("baseHex", entry.baseHex)
                put("matchColor1", entry.matchColor1)
                put("matchColor2", entry.matchColor2)
                put("matchColor3", entry.matchColor3)
                put("timestamp", entry.timestamp)
            }
            arr.put(obj)
        }

        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_HISTORY, arr.toString())
            .apply()
    }
}
