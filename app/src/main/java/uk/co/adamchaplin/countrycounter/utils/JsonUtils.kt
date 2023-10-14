package uk.co.adamchaplin.countrycounter.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonObject
import uk.co.adamchaplin.countrycounter.Continent

object JsonUtils {
    @JvmStatic
    fun extractVisitedCountriesForContinentFromJson(visited: JsonObject, continent: Continent): Set<String> {
        try {
            val continentVisited = visited.get(continent.value).asString
            return Gson().fromJson(continentVisited, MutableSet::class.java) as Set<String>
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Utils", "Error retrieving ${continent.value} countries from visited file")
        }
        return setOf()
    }

    @JvmStatic
    fun extractSettingsFromJson(settings: JsonObject): MutableMap<Continent, Set<String>> {
        val coreContinents: Set<Continent> = setOf(
            Continent.AFRICA,
            Continent.ANTARCTICA,
            Continent.ASIA,
            Continent.EUROPE,
            Continent.NORTH_AMERICA,
            Continent.OCEANIA,
            Continent.SOUTH_AMERICA
        )
        val settingsPerContinent: MutableMap<Continent, Set<String>> = mutableMapOf()
        coreContinents.forEach {continent ->
            settingsPerContinent[continent] = extractSettingsForContinentFromJson(settings, continent)
        }
        return settingsPerContinent
    }

    @JvmStatic
    private fun extractSettingsForContinentFromJson(settings: JsonObject, continent: Continent) : MutableSet<String> {
        try {
            val countrySettings = settings.get(continent.value).asString
            return Gson().fromJson(countrySettings, MutableSet::class.java) as MutableSet<String>
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Utils", "Error retrieving ${continent.value} settings from settings file")
        }
        return mutableSetOf()
    }
}