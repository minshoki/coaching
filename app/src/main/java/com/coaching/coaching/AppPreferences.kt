package com.coaching.coaching

import android.content.Context
import android.util.Log
import androidx.core.content.edit

object AppPreferences {

    private const val KEY_PUSH_TOKEN = "push_token"

    private fun makePreferences(context: Context) = context.getSharedPreferences("coaching_prf", Context.MODE_PRIVATE)


    fun getPushToken(context: Context): String? {
        val token = makePreferences(context).getString(KEY_PUSH_TOKEN, null)
        Log.e("debug-log", "getPushToken $token")
        return token
    }

    fun setPushToken(context: Context, token: String) {
        Log.e("debug-log", "setPushToken $token")
        makePreferences(context).edit {
            putString(KEY_PUSH_TOKEN, token)
        }
    }
}