package com.example.batterysticker

import android.content.Context
import android.content.SharedPreferences

class StickerPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("battery_sticker_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_ENABLED = "key_enabled"
        const val KEY_CHARACTER = "key_character"
        const val KEY_OFFSET_X = "key_offset_x"
        const val KEY_OFFSET_Y = "key_offset_y"
        const val KEY_SIZE = "key_size"

        const val CHAR_OGGY = "oggy"
        const val CHAR_FROG = "frog"
        const val CHAR_BEAR = "bear"
    }

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var character: String
        get() = prefs.getString(KEY_CHARACTER, CHAR_OGGY) ?: CHAR_OGGY
        set(value) = prefs.edit().putString(KEY_CHARACTER, value).apply()

    var offsetX: Int
        get() = prefs.getInt(KEY_OFFSET_X, 20)
        set(value) = prefs.edit().putInt(KEY_OFFSET_X, value).apply()

    var offsetY: Int
        get() = prefs.getInt(KEY_OFFSET_Y, 0)
        set(value) = prefs.edit().putInt(KEY_OFFSET_Y, value).apply()

    var sizeDp: Int
        get() = prefs.getInt(KEY_SIZE, 45)
        set(value) = prefs.edit().putInt(KEY_SIZE, value).apply()
}
