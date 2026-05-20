package com.example.myapplication // Твой пакет

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Создаем само хранилище DataStore (оно будет жить в памяти телефона)
val Context.dataStore by preferencesDataStore(name = "decision_forge_data")

class ReportRepository(private val context: Context) {

    private val gson = Gson()
    // Ключ, по которому мы будем искать наши отчеты в памяти
    private val REPORTS_KEY = stringPreferencesKey("weekly_reports")

    // 2. Функция для ЧТЕНИЯ данных.
    // Flow означает, что как только данные обновятся, графики сами перерисуются!
    fun getReportsFlow(): Flow<List<WeeklyReport>> {
        return context.dataStore.data.map { preferences ->
            // Достаем JSON строку. Если ее нет, берем пустой список "[]"
            val jsonString = preferences[REPORTS_KEY] ?: "[]"
            // Объясняем Gson'у, во что превратить текст
            val type = object : TypeToken<List<WeeklyReport>>() {}.type
            gson.fromJson(jsonString, type)
        }
    }

    // 3. Функция для СОХРАНЕНИЯ нового отчета (из Excel)
    suspend fun addReport(newReport: WeeklyReport) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[REPORTS_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<WeeklyReport>>() {}.type
            val currentList: MutableList<WeeklyReport> = gson.fromJson(currentJson, type)

            // Если отчет за эту дату уже есть, удаляем старый (обновляем)
            currentList.removeIf { it.dateId == newReport.dateId }
            // Добавляем новый отчет в копилку
            currentList.add(newReport)

            // Превращаем список обратно в текст и сохраняем
            preferences[REPORTS_KEY] = gson.toJson(currentList)
        }
    }
}