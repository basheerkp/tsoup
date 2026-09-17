package com.ahmed.tsoup

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

data class DomainItem(
    val domain: String, val enabled: Boolean, val querySize: Int
)

fun saveAddress(addresses: List<DomainItem>, prefs: SharedPreferences) {
    val json = Gson().toJson(addresses)
    prefs.edit { putString("addresses", json) }
}

fun loadAddress(prefs: SharedPreferences): List<DomainItem> {
    val json = prefs.getString("addresses", "[]")
    val type = object : TypeToken<List<DomainItem>>() {}.type
    return Gson().fromJson(json, type)
}