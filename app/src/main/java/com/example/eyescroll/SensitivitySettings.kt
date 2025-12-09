package com.example.eyescroll

import android.content.Context

object SensitivitySettings {
    private const val PREFS = "eyescroll_prefs"
    private const val KEY_SENS = "sensitivity"

    fun setSensitivity(ctx: Context, value: Int) {
        val clamped = value.coerceIn(0, 100)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_SENS, clamped).apply()
    }

    fun getSensitivity(ctx: Context): Int {
        val def = 50
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_SENS, def)
    }
}
