package com.krisna.groomy.data.ml

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LabelLoader(private val context: Context) {
    fun loadLabels(fileName: String): List<String> {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
