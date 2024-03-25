package uk.co.adamchaplin.countrycounter.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import uk.co.adamchaplin.countrycounter.Continent

object JsonUtils {

    @JvmStatic
    fun extractVisitedCountriesFromJson(settings: JsonObject): MutableMap<Continent, Set<String>> {
        val visitedCountriesPerContinent: MutableMap<Continent, Set<String>> = mutableMapOf()
        Continent.basicContinents.forEach {continent ->
            visitedCountriesPerContinent[continent] = extractVisitedCountriesForContinentFromJson(settings, continent)
        }
        return visitedCountriesPerContinent
    }

    @JvmStatic
    fun extractVisitedCountriesForContinentFromJson(visited: JsonObject, continent: Continent): Set<String> {
        try {
            val continentVisited = visited.get(continent.value).asString
            val setType = object : TypeToken<MutableSet<String>>() { }.type
            return Gson().fromJson(continentVisited, setType)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Utils", "Error retrieving ${continent.value} countries from visited file")
        }
        return setOf()
    }

    @JvmStatic
    fun extractSettingsFromJson(settings: JsonObject): MutableMap<Continent, Set<String>> {
        val settingsPerContinent: MutableMap<Continent, Set<String>> = mutableMapOf()
        Continent.basicContinents.forEach {continent ->
            settingsPerContinent[continent] = extractSettingsForContinentFromJson(settings, continent)
        }
        return settingsPerContinent
    }

    @JvmStatic
    private fun extractSettingsForContinentFromJson(settings: JsonObject, continent: Continent) : MutableSet<String> {
        try {
            val countrySettings = settings.get(continent.value).asString
            val setType = object : TypeToken<MutableSet<String>>() { }.type
            return Gson().fromJson(countrySettings, setType)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Utils", "Error retrieving ${continent.value} settings from settings file")
        }
        return mutableSetOf()
    }
}